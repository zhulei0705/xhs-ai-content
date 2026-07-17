package com.xhs.ai.content.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xhs.ai.content.common.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("xhs_ai_review")
public class AiReviewEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long contentId;
    private Integer aiToneScore;
    private Integer titleAttractionScore;
    private Integer authenticityScore;
    private Integer platformFitScore;
    private Boolean exaggeratedIncomeRisk;
    private Boolean sensitiveExpressionRisk;
    private Boolean marketingRisk;
    private String riskLevel;
    private String summary;
    private String suggestionsJson;
    private String provider;
    private String modelName;
    private LocalDateTime reviewedAt;
}
