package com.xhs.ai.content.analysis.controller;

import com.xhs.ai.content.analysis.dto.AnalysisExecuteRequest;
import com.xhs.ai.content.analysis.dto.DashboardRequest;
import com.xhs.ai.content.analysis.service.AnalysisService;
import com.xhs.ai.content.analysis.vo.DashboardVO;
import com.xhs.ai.content.analysis.vo.PerformanceAnalysisVO;
import com.xhs.ai.content.common.api.Result;
import com.xhs.ai.content.common.dto.IdRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analyses")
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/execute")
    public Result<PerformanceAnalysisVO> execute(@Valid @RequestBody AnalysisExecuteRequest request) {
        return Result.success(analysisService.execute(request));
    }

    @PostMapping("/latest")
    public Result<PerformanceAnalysisVO> latest(@Valid @RequestBody IdRequest request) {
        return Result.success(analysisService.latest(request.id()));
    }

    @PostMapping("/dashboard")
    public Result<DashboardVO> dashboard(@Valid @RequestBody DashboardRequest request) {
        return Result.success(analysisService.dashboard(request));
    }
}
