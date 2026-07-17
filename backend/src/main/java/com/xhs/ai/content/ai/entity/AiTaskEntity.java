package com.xhs.ai.content.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xhs.ai.content.common.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("xhs_ai_task")
public class AiTaskEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String taskNo;
    private String taskType;
    private Long bizId;
    private String status;
    private String provider;
    private String modelName;
    private String promptVersion;
    private String requestHash;
    private String resultSummary;
    private Integer promptTokens;
    private Integer completionTokens;
    private Long durationMs;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
