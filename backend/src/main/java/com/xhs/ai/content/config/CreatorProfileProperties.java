package com.xhs.ai.content.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "xhs.profile")
public class CreatorProfileProperties {

    private String identity = "程序员AI副业博主";
    private String targetAudience = "想做副业的人";
    private String contentStyle = "真实的个人成长记录";
    private List<String> coreDirections = List.of("AI副业", "AI工具", "AI Coding", "程序员副业", "个人成长");
}
