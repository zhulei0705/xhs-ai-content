package com.xhs.ai.content.scheduler;

import com.xhs.ai.content.config.ScheduleProperties;
import com.xhs.ai.content.topic.dto.TopicGenerateRequest;
import com.xhs.ai.content.topic.service.TopicService;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "xhs.schedule", name = "topic-enabled", havingValue = "true", matchIfMissing = true)
public class DailyTopicScheduler {

    private static final String LOCK_PREFIX = "xhs:topic:daily:lock:";
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final TopicService topicService;
    private final StringRedisTemplate redisTemplate;
    private final ScheduleProperties properties;

    @Scheduled(cron = "${xhs.schedule.topic-cron:0 0 8 * * ?}")
    public void generateDailyTopics() {
        LocalDate today = LocalDate.now();
        if (topicService.existsGeneratedOn(today)) {
            log.info("Daily topics already exist for {}", today);
            return;
        }
        String key = LOCK_PREFIX + today;
        String token = UUID.randomUUID().toString();
        boolean redisLocked = tryLock(key, token);
        if (!redisLocked) {
            return;
        }
        try {
            if (!topicService.existsGeneratedOn(today)) {
                topicService.generate(new TopicGenerateRequest(properties.getTopicCount(), today));
                log.info("Generated daily topics for {}", today);
            }
        } catch (Exception exception) {
            log.error("Daily topic generation failed for {}", today, exception);
        } finally {
            release(key, token);
        }
    }

    private boolean tryLock(String key, String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, token, Duration.ofMinutes(20)));
        } catch (Exception exception) {
            log.warn("Redis lock unavailable; database idempotency will be used: {}",
                    exception.getClass().getSimpleName());
            return true;
        }
    }

    private void release(String key, String token) {
        try {
            redisTemplate.execute(RELEASE_SCRIPT, Collections.singletonList(key), token);
        } catch (Exception exception) {
            log.warn("Redis lock release skipped: {}", exception.getClass().getSimpleName());
        }
    }
}
