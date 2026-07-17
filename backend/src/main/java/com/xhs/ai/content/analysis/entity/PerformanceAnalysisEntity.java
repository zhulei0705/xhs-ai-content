package com.xhs.ai.content.analysis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xhs.ai.content.common.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("xhs_performance_analysis")
public class PerformanceAnalysisEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long publishRecordId;
    private String overallGrade;
    private String performanceSummary;
    private String topicAnalysis;
    private String titleAnalysis;
    private String favoriteRateAnalysis;
    private String interactionRateAnalysis;
    private String nextTopicSuggestionsJson;
    private String provider;
    private String modelName;
    private LocalDateTime analyzedAt;
}
