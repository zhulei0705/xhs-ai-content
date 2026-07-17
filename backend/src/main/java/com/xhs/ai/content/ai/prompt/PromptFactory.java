package com.xhs.ai.content.ai.prompt;

import com.xhs.ai.content.config.CreatorProfileProperties;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromptFactory {

    private final CreatorProfileProperties profile;

    public String systemPrompt() {
        return """
                你是资深小红书内容策划和合规编辑。账号身份：%s；目标用户：%s；内容风格：%s；核心方向：%s。
                必须保持真实克制，不得编造个人经历、收入、产品体验或数据，不得承诺收益，不得使用违规或明显营销表达。
                只输出合法JSON，不要输出Markdown代码块、解释或额外文字。
                """.formatted(profile.getIdentity(), profile.getTargetAudience(),
                profile.getContentStyle(), String.join("、", profile.getCoreDirections()));
    }

    public String topicPrompt(int count, LocalDate date) {
        return """
                为%s生成今日选题。选题数量：%d。
                每个选题必须包含：topicTitle、coreAngle、targetAudience、contentType、keywords字符串数组、
                targetMatchScore、viralPotentialScore、overallScore（0-100整数）、potentialAnalysis、publishPriority（1-%d且不重复）。
                输出对象结构：{"topics":[...]}。选题要具体、可执行、各有差异，避免暴富和制造焦虑。
                """.formatted(date, count, count);
    }

    public String contentPrompt(Map<String, Object> topic) {
        return """
                根据以下选题生成完整内容包：%s。
                输出：titles恰好5项（text、attractionScore），body小红书正文，coverTitle，coverSubtitle，
                cards恰好6项（cardNo 1-6、title、body），tags为5-8个不带#的标签，interactionGuide。
                正文使用自然的第一人称成长记录，提供可执行价值；若选题没有提供真实经历，只能表述为方法、计划或建议，不能虚构实测结果。
                """.formatted(topic);
    }

    public String reviewPrompt(Map<String, Object> content) {
        return """
                审核以下小红书内容：%s。
                检查AI味、标题吸引力、真实性、夸大收益、敏感表达、明显营销和平台适配度。
                输出字段：aiToneScore（越自然越高）、titleAttractionScore、authenticityScore、platformFitScore（均0-100），
                exaggeratedIncomeRisk、sensitiveExpressionRisk、marketingRisk布尔值，riskLevel（LOW/MEDIUM/HIGH），summary，suggestions字符串数组。
                """.formatted(content);
    }

    public String analysisPrompt(Map<String, Object> metrics) {
        return """
                复盘以下已发布内容及数据：%s。
                输出overallGrade、performanceSummary、topicAnalysis、titleAnalysis、favoriteRateAnalysis、
                interactionRateAnalysis、nextTopicSuggestions（3-5个具体选题）。结论要基于数据，小样本时明确不确定性。
                """.formatted(metrics);
    }
}
