package com.example.TaskAPI.hackerrank.api;

import com.example.TaskAPI.hackerrank.client.dto.HackerRankArticle;
import com.example.TaskAPI.hackerrank.client.dto.HackerRankPage;
import com.example.TaskAPI.hackerrank.exception.HackerRankApiException;
import com.example.TaskAPI.hackerrank.service.HackerRankArticleService;
import com.example.TaskAPI.web.BaseControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HackerRankArticleController.class)
@Import(HackerRankExceptionHandler.class)
public class HackerRankArticleControllerTest extends BaseControllerTest {
    @MockitoBean
    private HackerRankArticleService articleService;

    @Test
    void getArticles_returnsMappedArticles() throws Exception {
        when(articleService.fetchPage(1))
                .thenReturn(
                        new HackerRankPage<>(
                                1, 10, 20, 2, List.of(
                                new HackerRankArticle(
                                        "Show HN",
                                        "https://example/com/a",
                                        "alice",
                                        3,
                                        42L,
                                        null,
                                        null,
                                        null,
                                        15126665991L))));

        mockMvc.perform(get("/integrations/hackerrank/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Show HN"))
                .andExpect(jsonPath("$[0].commentCount").value(3));
    }

    @Test
    void getArticles_upstreamsFails_returns502() throws Exception {
        when(articleService.fetchPage(1))
                .thenThrow(new HackerRankApiException("upstream down", new RuntimeException()));

        mockMvc.perform(get("/integrations/hackerrank/articles"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Upstream integration unavailable"));
    }

    @Test
    void getArticles_noPageParam_defaultsToPageOne() throws Exception {
        when(articleService.fetchPage(1))
                .thenReturn(new HackerRankPage<>(1, 10, 20, 2, List.of()));

        mockMvc.perform(get("/integrations/hackerrank/articles"))
                .andExpect(status().isOk());

        verify(articleService).fetchPage(1);
    }

    @Test
    void getArticles_page0_returns500() throws Exception {
        mockMvc.perform(get("/integrations/hackerrank/articles").param("pageNum", "0"))
                .andExpect(status().is5xxServerError());
    }
}
