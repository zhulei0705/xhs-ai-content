package com.xhs.ai.content.analysis.service;

import com.xhs.ai.content.analysis.dto.AnalysisExecuteRequest;
import com.xhs.ai.content.analysis.dto.DashboardRequest;
import com.xhs.ai.content.analysis.vo.DashboardVO;
import com.xhs.ai.content.analysis.vo.PerformanceAnalysisVO;

public interface AnalysisService {

    PerformanceAnalysisVO execute(AnalysisExecuteRequest request);

    PerformanceAnalysisVO latest(Long publishRecordId);

    DashboardVO dashboard(DashboardRequest request);
}
