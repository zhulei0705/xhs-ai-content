package com.xhs.ai.content.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xhs.ai.content.content.entity.ContentTitleEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface ContentTitleMapper extends BaseMapper<ContentTitleEntity> {

    @Delete("DELETE FROM xhs_content_title WHERE content_id = #{contentId}")
    int deletePhysicalByContentId(@Param("contentId") Long contentId);
}
