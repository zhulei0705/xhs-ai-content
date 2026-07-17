package com.xhs.ai.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class WorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCompleteSafeContentWorkflow() throws Exception {
        JsonNode topics = postJson("/api/v1/topics/generate", Map.of("count", 3));
        assertThat(topics.path("data")).hasSize(3);
        String topicId = topics.at("/data/0/id").asText();

        JsonNode content = postJson("/api/v1/contents/generate", Map.of("topicId", topicId));
        assertThat(content.at("/data/titles")).hasSize(5);
        assertThat(content.at("/data/cards")).hasSize(6);
        String contentId = content.at("/data/id").asText();

        postJson("/api/v1/reviews/execute", Map.of("contentId", contentId));
        postJson("/api/v1/reviews/confirm", Map.of("contentId", contentId, "approved", true));

        JsonNode ready = postJson("/api/v1/contents/detail", Map.of("id", contentId));
        assertThat(ready.at("/data/status").asText()).isEqualTo("READY_TO_PUBLISH");

        JsonNode published = postJson("/api/v1/publish-records/save", Map.of(
                "contentId", contentId,
                "publishedAt", LocalDateTime.of(2026, 7, 17, 12, 0).toString(),
                "noteUrl", "https://www.xiaohongshu.com/explore/test-note",
                "exposureCount", 1000,
                "likeCount", 80,
                "favoriteCount", 120,
                "commentCount", 30,
                "followerGrowth", 12));
        String publishRecordId = published.at("/data/id").asText();
        assertThat(published.at("/data/favoriteRate").decimalValue()).isEqualByComparingTo("0.1200");

        JsonNode analysis = postJson("/api/v1/analyses/execute", Map.of("publishRecordId", publishRecordId));
        assertThat(analysis.at("/data/nextTopicSuggestions")).hasSizeGreaterThanOrEqualTo(3);

        mockMvc.perform(post("/api/v1/analyses/dashboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.publishedCount").value(1))
                .andExpect(jsonPath("$.data.totalExposure").value(1000));
    }

    private JsonNode postJson(String url, Object body) throws Exception {
        String response = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }
}
