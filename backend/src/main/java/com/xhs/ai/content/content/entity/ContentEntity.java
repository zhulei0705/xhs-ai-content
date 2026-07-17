package com.xhs.ai.content.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xhs.ai.content.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("xhs_content")
public class ContentEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long topicId;
    private String selectedTitle;
    private String body;
    private String coverTitle;
    private String coverSubtitle;
    private String tagsJson;
    private String interactionGuide;
    private String status;
    private Long currentReviewId;
}
