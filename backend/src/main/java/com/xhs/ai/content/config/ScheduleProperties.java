package com.xhs.ai.content.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "xhs.schedule")
public class ScheduleProperties {

    private boolean topicEnabled = true;
    private String topicCron = "0 0 8 * * ?";
    private int topicCount = 5;
}
