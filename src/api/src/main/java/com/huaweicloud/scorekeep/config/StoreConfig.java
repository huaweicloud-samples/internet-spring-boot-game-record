package com.huaweicloud.scorekeep.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

/**
 * 宽表存储客户端装配。
 *
 * <p>接入点由实例的负载均衡地址与数据面端口给出，须显式指定；
 * 端口取值随实例部署形态而定，由环境变量注入；
 * 数据面以实例自带的数据库账号做静态签名，区域仅参与请求签名，不参与路由。</p>
 */
@Configuration
public class StoreConfig {

    private static final Logger log = LoggerFactory.getLogger(StoreConfig.class);

    // 数据面端口随实例部署形态而定：集群形态走高可用端口 80，单节点形态走 8000。
    private static final String DEFAULT_PORT = "80";
    private static final String DEFAULT_REGION = "cn-north-4";

    @Bean(destroyMethod = "close")
    public DynamoDbClient storeClient(Environment env) {
        String host = required(env, "GEMINIDB_HOST");
        String port = env.getProperty("GEMINIDB_PORT", DEFAULT_PORT);
        String user = required(env, "GEMINIDB_USER");
        String password = required(env, "GEMINIDB_PASSWORD");
        String region = env.getProperty("HW_REGION", DEFAULT_REGION);

        URI endpoint = URI.create("http://" + host + ":" + port);
        log.info("宽表存储接入点 {}，签名区域 {}", endpoint, region);

        return DynamoDbClient.builder()
                .endpointOverride(endpoint)
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(user, password)))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient storeEnhancedClient(DynamoDbClient storeClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(storeClient)
                .build();
    }

    private static String required(Environment env, String key) {
        String value = env.getProperty(key, "");
        if (value.isBlank()) {
            throw new IllegalStateException("缺少必需的环境变量：" + key);
        }
        return value;
    }
}
