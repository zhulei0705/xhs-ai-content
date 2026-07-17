package com.xhs.ai.content.publish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xhs.ai.content.common.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("xhs_publish_record")
public class PublishRecordEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long contentId;
    private LocalDateTime publishedAt;
    private String noteUrl;
    private Long exposureCount;
    private Long likeCount;
    private Long favoriteCount;
    private Long commentCount;
    private Integer followerGrowth;
}
