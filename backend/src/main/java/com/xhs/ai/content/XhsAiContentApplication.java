package com.xhs.ai.content;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan({
        "com.xhs.ai.content.ai.mapper",
        "com.xhs.ai.content.topic.mapper",
        "com.xhs.ai.content.content.mapper",
        "com.xhs.ai.content.review.mapper",
        "com.xhs.ai.content.publish.mapper",
        "com.xhs.ai.content.analysis.mapper"
})
@ConfigurationPropertiesScan
@SpringBootApplication
public class XhsAiContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(XhsAiContentApplication.class, args);
    }
}
