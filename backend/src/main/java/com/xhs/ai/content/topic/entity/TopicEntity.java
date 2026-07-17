package com.xhs.ai.content.topic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xhs.ai.content.common.entity.BaseEntity;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("xhs_topic")
public class TopicEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String topicTitle;
    private String coreAngle;
    private String targetAudience;
    private String contentType;
    private String keywordsJson;
    private Integer targetMatchScore;
    private Integer viralPotentialScore;
    private Integer overallScore;
    private String potentialAnalysis;
    private Integer publishPriority;
    private String sourceType;
    private LocalDate generationDate;
    private String status;
}
