package com.xhs.ai.content.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xhs.ai.content.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("xhs_content_title")
public class ContentTitleEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long contentId;
    private String titleText;
    private Integer sortOrder;
    private Boolean selected;
    private Integer attractionScore;
}
