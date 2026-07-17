package com.xhs.ai.content.content.service;

import com.xhs.ai.content.common.api.PageResult;
import com.xhs.ai.content.content.dto.ContentGenerateRequest;
import com.xhs.ai.content.content.dto.ContentPageRequest;
import com.xhs.ai.content.content.dto.ContentSaveRequest;
import com.xhs.ai.content.content.dto.ContentStatusUpdateRequest;
import com.xhs.ai.content.content.vo.ContentCopyVO;
import com.xhs.ai.content.content.vo.ContentDetailVO;
import com.xhs.ai.content.content.vo.ContentListVO;

public interface ContentService {

    ContentDetailVO generate(ContentGenerateRequest request);

    PageResult<ContentListVO> page(ContentPageRequest request);

    ContentDetailVO detail(Long id);

    ContentDetailVO create(ContentSaveRequest request);

    ContentDetailVO update(ContentSaveRequest request);

    void delete(Long id);

    void updateStatus(ContentStatusUpdateRequest request);

    ContentCopyVO copy(Long id);
}
