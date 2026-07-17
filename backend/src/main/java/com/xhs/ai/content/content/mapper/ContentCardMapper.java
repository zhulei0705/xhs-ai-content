package com.xhs.ai.content.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xhs.ai.content.content.entity.ContentCardEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface ContentCardMapper extends BaseMapper<ContentCardEntity> {

    @Delete("DELETE FROM xhs_content_card WHERE content_id = #{contentId}")
    int deletePhysicalByContentId(@Param("contentId") Long contentId);
}
