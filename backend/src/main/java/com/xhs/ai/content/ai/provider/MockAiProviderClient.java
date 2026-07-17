package com.xhs.ai.content.ai.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xhs.ai.content.ai.dto.AiGenerationRequest;
import com.xhs.ai.content.ai.dto.AiProviderResponse;
import com.xhs.ai.content.ai.dto.ContentPackageAiResponse;
import com.xhs.ai.content.ai.dto.ContentReviewAiResponse;
import com.xhs.ai.content.ai.dto.PerformanceAnalysisAiResponse;
import com.xhs.ai.content.ai.dto.TopicBatchAiResponse;
import com.xhs.ai.content.common.exception.BizException;
import com.xhs.ai.content.common.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** 无密钥环境的本地联调 Provider，不会发起外部请求。 */
@Component
public class MockAiProviderClient implements AiProviderClient {

    private final ObjectMapper objectMapper;

    public MockAiProviderClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(String provider) {
        return provider == null || provider.isBlank() || "mock".equalsIgnoreCase(provider);
    }

    @Override
    public AiProviderResponse chat(AiGenerationRequest request) {
        Object response = switch (request.operation()) {
            case TOPIC_GENERATION -> mockTopics(extractCount(request.userPrompt()));
            case CONTENT_GENERATION -> mockContent();
            case CONTENT_REVIEW -> mockReview();
            case PERFORMANCE_ANALYSIS -> mockAnalysis();
        };
        try {
            return new AiProviderResponse(objectMapper.writeValueAsString(response), 0, 0);
        } catch (JsonProcessingException exception) {
            throw new BizException(ErrorCode.AI_RESPONSE_INVALID, "Mock AI结果序列化失败");
        }
    }

    private TopicBatchAiResponse mockTopics(int count) {
        List<String> names = List.of(
                "下班后用AI做副业，我先跑通了这3个最小步骤",
                "程序员别只盯着接外包：5个AI副业方向实测思路",
                "零基础也能开始的AI工作流：每天省下1小时",
                "我用AI Coding做了一个小工具，完整复盘来了",
                "做副业30天后，我停止追热点的3个原因",
                "普通人如何筛选靠谱AI工具：我的5条标准",
                "从不会表达到账户稳定更新，我的AI内容流程",
                "程序员副业最容易踩的4个时间管理坑",
                "AI不能替你赚钱，但能帮你降低试错成本",
                "一个人做内容，如何搭建轻量AI素材库");
        List<TopicBatchAiResponse.TopicItem> topics = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            int score = 92 - index * 2;
            topics.add(new TopicBatchAiResponse.TopicItem(
                    names.get(index),
                    "用真实尝试过程拆解可执行的小步骤，避免收益承诺",
                    "想利用下班时间尝试AI副业的职场人",
                    index % 2 == 0 ? "成长复盘" : "实操教程",
                    List.of("AI副业", "程序员副业", "个人成长"),
                    Math.max(70, score),
                    Math.max(68, score - 3),
                    Math.max(70, score - 1),
                    "人群明确、痛点具体，并且具备可收藏的步骤型信息。",
                    index + 1));
        }
        return new TopicBatchAiResponse(topics);
    }

    private ContentPackageAiResponse mockContent() {
        List<ContentPackageAiResponse.TitleItem> titles = List.of(
                new ContentPackageAiResponse.TitleItem("下班后做AI副业，我先跑通了这3步", 92),
                new ContentPackageAiResponse.TitleItem("程序员做AI副业，先别急着辞职", 88),
                new ContentPackageAiResponse.TitleItem("我的AI副业最小闭环，终于跑通了", 90),
                new ContentPackageAiResponse.TitleItem("每天1小时，我这样开始AI副业", 86),
                new ContentPackageAiResponse.TitleItem("AI副业不是暴富：这是我的真实起点", 91));
        String body = "我没有一开始就做很大的项目，而是给自己定了一个小目标：用下班后的1小时，跑通一次真实交付。\n\n第一步，选一个自己熟悉的小问题。我从程序员日常里的重复工作入手，不追陌生赛道。\n\n第二步，用AI把需求拆成最小任务，再自己检查结果。AI负责提速，判断和质量仍然由我负责。\n\n第三步，把过程记录下来：花了多久、哪里卡住、用户真正需要什么。即使第一次没有收入，这些记录也会成为下一次迭代的素材。\n\n这套方法不保证收益，但它让我从“收藏很多教程”走到了“完成一次行动”。如果你也想开始，可以先选一个今晚能完成的小问题。";
        List<ContentPackageAiResponse.CardItem> cards = List.of(
                new ContentPackageAiResponse.CardItem(1, "AI副业，从小闭环开始", "不辞职、不押重注，先验证一次真实需求。"),
                new ContentPackageAiResponse.CardItem(2, "01 选熟悉的问题", "从工作和生活中的重复任务找切口，降低学习成本。"),
                new ContentPackageAiResponse.CardItem(3, "02 拆成最小任务", "让AI辅助拆解，你负责判断、核验和交付质量。"),
                new ContentPackageAiResponse.CardItem(4, "03 完成一次交付", "目标不是立刻赚钱，而是走完需求到结果的全过程。"),
                new ContentPackageAiResponse.CardItem(5, "04 记录真实数据", "记录耗时、卡点和反馈，让下一次行动更准确。"),
                new ContentPackageAiResponse.CardItem(6, "今晚就能开始", "写下一个小问题，给自己60分钟做出第一版。"));
        return new ContentPackageAiResponse(
                titles, body, "AI副业先跑通3步", "程序员的下班后真实尝试", cards,
                List.of("AI副业", "程序员副业", "AI工具", "个人成长", "副业探索"),
                "你现在最想用AI解决哪个小问题？可以留在评论区，我也想继续收集真实需求。");
    }

    private ContentReviewAiResponse mockReview() {
        return new ContentReviewAiResponse(
                88, 90, 92, 91, false, false, false, "LOW",
                "表达较自然，包含明确的个人行动过程，没有承诺收益，适合人工确认后发布。",
                List.of("补充一次真实耗时或失败细节，可进一步降低AI感", "发布前再次核对所有个人经历是否真实发生"));
    }

    private PerformanceAnalysisAiResponse mockAnalysis() {
        return new PerformanceAnalysisAiResponse(
                "B+",
                "内容具备稳定互动基础，收藏表现好于点赞，说明步骤型信息对目标用户有效。",
                "具体、低门槛的AI副业实践比泛泛趋势内容更容易获得收藏。",
                "标题中的人群、场景和数字明确，但可以进一步突出真实结果或冲突。",
                "收藏率体现了清单和步骤的长期参考价值，建议继续增加可保存的信息密度。",
                "互动率中等，可把结尾问题改得更具体，降低评论门槛。",
                List.of("程序员下班1小时能完成的3个AI小项目", "第一次AI工具交付失败后，我改了什么", "我筛选AI副业需求的5条标准"));
    }

    private int extractCount(String prompt) {
        for (int count = 10; count >= 3; count--) {
            if (prompt.contains("选题数量：" + count)) {
                return count;
            }
        }
        return 5;
    }
}
