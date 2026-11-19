package com.huaweicloud.scorekeep.config;

import com.huaweicloud.scorekeep.domain.TableNames;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateGlobalSecondaryIndexAction;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndexDescription;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndexUpdate;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.UpdateTableRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 宽表结构与二级索引的初始化。
 *
 * <p>在应用对外提供服务之前完成建表：仅建主键的表若只靠首次写入自动创建，
 * 二级索引不会被一并创建，按场次、按对局检索的查询会失败，因此需在此显式声明。</p>
 *
 * <p>过程幂等：表已存在时只补齐缺失的索引，不会重复创建。</p>
 */
@Component
public class StoreSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(StoreSchemaInitializer.class);

    private static final long READ_CAPACITY = 5;
    private static final long WRITE_CAPACITY = 5;

    private static final String ATTRIBUTE_ID = "id";

    private final DynamoDbClient client;

    public StoreSchemaInitializer(DynamoDbClient client) {
        this.client = client;
    }

    @PostConstruct
    public void initialize() {
        ensureTable(TableNames.PLAYER, List.of(), List.of());
        ensureTable(TableNames.SESSION, List.of(), List.of());
        ensureTable(TableNames.GAME, List.of(TableNames.GAME_SESSION_INDEX_ATTRIBUTE),
                List.of(new IndexSpec(TableNames.GAME_SESSION_INDEX, TableNames.GAME_SESSION_INDEX_ATTRIBUTE)));
        ensureTable(TableNames.MOVE, List.of(TableNames.GAME_INDEX_ATTRIBUTE),
                List.of(new IndexSpec(TableNames.MOVE_GAME_INDEX, TableNames.GAME_INDEX_ATTRIBUTE)));
        ensureTable(TableNames.STATE, List.of(TableNames.GAME_INDEX_ATTRIBUTE),
                List.of(new IndexSpec(TableNames.STATE_GAME_INDEX, TableNames.GAME_INDEX_ATTRIBUTE)));
    }

    private void ensureTable(String name, List<String> indexedAttributes, List<IndexSpec> indexes) {
        TableDescription existing = describe(name);
        if (existing != null) {
            addMissingIndexes(name, existing, indexes);
            return;
        }

        List<AttributeDefinition> attributes = new ArrayList<>();
        attributes.add(attribute(ATTRIBUTE_ID));
        indexedAttributes.stream().distinct().forEach(attr -> attributes.add(attribute(attr)));

        CreateTableRequest.Builder request = CreateTableRequest.builder()
                .tableName(name)
                .attributeDefinitions(attributes)
                .keySchema(KeySchemaElement.builder()
                        .attributeName(ATTRIBUTE_ID)
                        .keyType(KeyType.HASH)
                        .build())
                .provisionedThroughput(throughput());

        if (!indexes.isEmpty()) {
            request.globalSecondaryIndexes(indexes.stream().map(StoreSchemaInitializer::toIndex).toList());
        }

        client.createTable(request.build());
        log.info("已创建表 {}", name);
        awaitActive(name);
    }

    /**
     * 补齐缺失的二级索引。索引在首次写入自动建出的表上不会被创建，
     * 因此需单独判断并补齐，而不是只看表是否存在。
     */
    private void addMissingIndexes(String name, TableDescription existing, List<IndexSpec> indexes) {
        Set<String> present = existing.hasGlobalSecondaryIndexes()
                ? existing.globalSecondaryIndexes().stream()
                        .map(GlobalSecondaryIndexDescription::indexName)
                        .collect(Collectors.toSet())
                : Set.of();

        List<IndexSpec> missing = indexes.stream()
                .filter(spec -> !present.contains(spec.name()))
                .toList();
        if (missing.isEmpty()) {
            log.info("表 {} 已存在且索引齐备，跳过", name);
            return;
        }

        List<GlobalSecondaryIndexUpdate> updates = new ArrayList<>();
        for (IndexSpec spec : missing) {
            updates.add(GlobalSecondaryIndexUpdate.builder()
                    .create(toCreateAction(spec))
                    .build());
        }

        client.updateTable(UpdateTableRequest.builder()
                .tableName(name)
                .attributeDefinitions(missing.stream()
                        .map(spec -> attribute(spec.partitionAttribute()))
                        .toList())
                .globalSecondaryIndexUpdates(updates)
                .build());
        log.info("已为表 {} 补齐索引 {}", name, missing.stream().map(IndexSpec::name).toList());
        awaitActive(name);
    }

    private TableDescription describe(String name) {
        try {
            return client.describeTable(DescribeTableRequest.builder().tableName(name).build()).table();
        } catch (ResourceNotFoundException e) {
            return null;
        } catch (ResourceInUseException e) {
            log.warn("表 {} 正在变更中，按已存在处理", name);
            return client.describeTable(DescribeTableRequest.builder().tableName(name).build()).table();
        }
    }

    /** 等待表进入可读写状态，超出等待上限时仅记录告警，不阻断启动。 */
    private void awaitActive(String name) {
        long deadline = System.currentTimeMillis() + 90_000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                String status = client.describeTable(DescribeTableRequest.builder().tableName(name).build())
                        .table().tableStatusAsString();
                if ("ACTIVE".equals(status)) {
                    return;
                }
                Thread.sleep(2_000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (RuntimeException e) {
                log.debug("查询表 {} 状态失败：{}", name, e.getMessage());
                return;
            }
        }
        log.warn("等待表 {} 就绪超时，继续启动", name);
    }

    private static AttributeDefinition attribute(String name) {
        return AttributeDefinition.builder()
                .attributeName(name)
                .attributeType(ScalarAttributeType.S)
                .build();
    }

    private static GlobalSecondaryIndex toIndex(IndexSpec spec) {
        return GlobalSecondaryIndex.builder()
                .indexName(spec.name())
                .keySchema(indexKeySchema(spec))
                .projection(projection())
                .provisionedThroughput(throughput())
                .build();
    }

    private static CreateGlobalSecondaryIndexAction toCreateAction(IndexSpec spec) {
        return CreateGlobalSecondaryIndexAction.builder()
                .indexName(spec.name())
                .keySchema(indexKeySchema(spec))
                .projection(projection())
                .provisionedThroughput(throughput())
                .build();
    }

    private static List<KeySchemaElement> indexKeySchema(IndexSpec spec) {
        return List.of(KeySchemaElement.builder()
                .attributeName(spec.partitionAttribute())
                .keyType(KeyType.HASH)
                .build());
    }

    private static Projection projection() {
        return Projection.builder().projectionType(ProjectionType.ALL).build();
    }

    private static ProvisionedThroughput throughput() {
        return ProvisionedThroughput.builder()
                .readCapacityUnits(READ_CAPACITY)
                .writeCapacityUnits(WRITE_CAPACITY)
                .build();
    }

    private record IndexSpec(String name, String partitionAttribute) {
    }
}
