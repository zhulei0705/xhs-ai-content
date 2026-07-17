package com.xhs.ai.content.publish.service;

import com.xhs.ai.content.common.api.PageResult;
import com.xhs.ai.content.publish.dto.PublishRecordPageRequest;
import com.xhs.ai.content.publish.dto.PublishRecordSaveRequest;
import com.xhs.ai.content.publish.vo.PublishRecordVO;

public interface PublishRecordService {

    PublishRecordVO save(PublishRecordSaveRequest request);

    PageResult<PublishRecordVO> page(PublishRecordPageRequest request);

    PublishRecordVO detail(Long id);
}
