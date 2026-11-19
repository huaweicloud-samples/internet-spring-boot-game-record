package com.huaweicloud.scorekeep.notification;

import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.auth.MetadataCredentialProvider;
import com.huaweicloud.sdk.core.region.Region;
import com.huaweicloud.sdk.smn.v2.SmnClient;
import com.huaweicloud.sdk.smn.v2.model.PublishMessageRequest;
import com.huaweicloud.sdk.smn.v2.model.PublishMessageRequestBody;
import com.huaweicloud.sdk.smn.v2.model.PublishMessageResponse;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 业务事件通知。
 *
 * <p>凭据优先取自实例绑定的委托（经实例元数据获取临时凭据），
 * 其次回退到显式提供的长期密钥，两者都不可用时通知功能静默停用，
 * 不影响对局主流程。</p>
 *
 * <p>发布动作在独立线程中执行，避免通知服务的响应时延拖慢接口。</p>
 */
@Component
public class EventNotifier {

    private static final Logger log = LoggerFactory.getLogger(EventNotifier.class);

    private static final String DEFAULT_REGION = "cn-north-4";

    private final String topicUrn;
    private final SmnClient client;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "event-notifier");
        thread.setDaemon(true);
        return thread;
    });

    public EventNotifier(Environment env) {
        this.topicUrn = env.getProperty("SMN_TOPIC_URN", "");
        this.client = buildClient(env);
        if (topicUrn.isBlank()) {
            log.info("未配置通知主题，业务事件通知已停用");
        } else if (client == null) {
            log.warn("未取得可用凭据，业务事件通知已停用");
        } else {
            log.info("业务事件通知已启用，主题 {}", topicUrn);
        }
    }

    /** 发布一条业务事件通知；失败只记录日志，不向调用方抛出。 */
    public void notify(String subject, String message) {
        if (topicUrn.isBlank() || client == null) {
            log.debug("通知未启用，丢弃事件：{}", subject);
            return;
        }
        executor.submit(() -> {
            try {
                PublishMessageResponse response = client.publishMessage(new PublishMessageRequest()
                        .withTopicUrn(topicUrn)
                        .withBody(new PublishMessageRequestBody()
                                .withSubject(subject)
                                .withMessage(message)));
                log.info("事件通知已发布，消息标识 {}", response.getMessageId());
            } catch (Exception e) {
                log.warn("事件通知发布失败：{}", e.getMessage());
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    private static SmnClient buildClient(Environment env) {
        String regionId = env.getProperty("HW_REGION", DEFAULT_REGION);
        ICredential credential = resolveCredential(env);
        if (credential == null) {
            return null;
        }
        return SmnClient.newBuilder()
                .withCredential(credential)
                .withRegion(new Region(regionId, "https://smn." + regionId + ".myhuaweicloud.com"))
                .build();
    }

    private static ICredential resolveCredential(Environment env) {
        String accessKey = env.getProperty("HW_ACCESS_KEY", "");
        String secretKey = env.getProperty("HW_SECRET_KEY", "");
        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            return new BasicCredentials()
                    .withAk(accessKey)
                    .withSk(secretKey);
        }
        try {
            return MetadataCredentialProvider.basic().getCredentials();
        } catch (Exception e) {
            log.warn("未能从实例元数据取得临时凭据：{}", e.getMessage());
            return null;
        }
    }
}
