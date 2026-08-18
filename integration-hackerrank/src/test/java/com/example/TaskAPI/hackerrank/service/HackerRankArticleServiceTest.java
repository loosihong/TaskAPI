package com.example.TaskAPI.hackerrank.service;

import com.example.TaskAPI.hackerrank.client.HackerRankArticleClient;
import com.example.TaskAPI.hackerrank.client.dto.HackerRankArticle;
import com.example.TaskAPI.hackerrank.client.dto.HackerRankPage;
import com.example.TaskAPI.hackerrank.exception.HackerRankApiException;
import com.example.TaskAPI.hackerrank.exception.HackerRankUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HackerRankArticleServiceTest {
    @Mock
    private HackerRankArticleClient articleClient;
    private HackerRankArticleService articleService;

    @BeforeEach
    public void setUp() {
        articleService = new HackerRankArticleService(articleClient);
    }

    private HackerRankPage<HackerRankArticle> pageOf(int page, int totalPages, HackerRankArticle... articles) {
        return new HackerRankPage<>(page, 10, totalPages * 10, totalPages, List.of(articles));
    }

    private HackerRankArticle titled(String title) {
        return new HackerRankArticle(
                title,
                "https://example.com/a",
                "alice",
                3,
                42L,
                null,
                null,
                null,
                1512265991L);
    }

    private HackerRankArticle storyTitled(String storyTitle) {
        return new HackerRankArticle(
                null,
                null,
                "carol",
                1,
                44L,
                storyTitle,
                "https://example.com/story",
                null,
                1512265993L);
    }

    private HackerRankArticle untitled() {
        return new HackerRankArticle(
                null,
                null,
                "bob",
                0,
                43L,
                null,
                null,
                null,
                1512265992L);
    }

    @Nested
    @DisplayName("Single Page Fetch")
    class SinglePageFetch {
        @Test
        void fetchPage_delegatesToClient() {
            HackerRankPage<HackerRankArticle> expected = pageOf(1, 3, titled("Show HN"));

            when(articleClient.fetchPage(1))
                    .thenReturn(expected);

            assertThat(articleService.fetchPage(1)).isEqualTo(expected);
            verify(articleClient).fetchPage(1);
        }

        @Test
        void fetchPage_transientFailureThenSuccess_returnsPage() {
            when(articleClient.fetchPage(1))
                    .thenThrow(new ResourceAccessException("connect timed out"))
                    .thenReturn(pageOf(1, 1, titled("One")));

            assertThat(articleService.fetchPage(1).data()).hasSize(1);
            verify(articleClient, times(2)).fetchPage(1);
        }

        @Test
        void fetchPage_clientError_isNotRetried() {
            when(articleClient.fetchPage(1))
                    .thenThrow(new HackerRankApiException("HackerRank 400: ", null));

            assertThatThrownBy(() -> articleService.fetchPage(1))
                    .isInstanceOf(HackerRankApiException.class);
            verify(articleClient, times(1)).fetchPage(1);
        }
    }

    @Nested
    @DisplayName("Page walking")
    class PageWalking {
        @Test
        void fetchAllTitledArticles_singlePage_callsClientOnce() {
            when(articleClient.fetchPage(1))
                    .thenReturn(pageOf(1, 1, titled("One")));

            assertThat(articleService.fetchAllTitledArticles())
                    .hasSize(1)
                    .isUnmodifiable();
            verify(articleClient, times(1)).fetchPage(anyInt());
        }

        @Test
        void fetchAllTitledArticles_multiplePages_walksUntilTotalPages() {
            when(articleClient.fetchPage(1))
                    .thenReturn(pageOf(1, 3, titled("One")));
            when(articleClient.fetchPage(2))
                    .thenReturn(pageOf(2, 3, titled("Two")));
            when(articleClient.fetchPage(3))
                    .thenReturn(pageOf(3, 3, titled("Three")));

            assertThat(articleService.fetchAllTitledArticles())
                    .extracting(HackerRankArticle::displayTitle)
                    .containsExactly("One", "Two", "Three");
            verify(articleClient, times(3)).fetchPage(anyInt());
        }

        @Test
        void fetchAllTitledArticles_dropsArticlesWithoutAnyTitle() {
            when(articleClient.fetchPage(1))
                    .thenReturn(pageOf(1, 1, titled("Keep"), untitled(), storyTitled("Fallback")));

            assertThat(articleService.fetchAllTitledArticles())
                    .extracting(HackerRankArticle::displayTitle)
                    .containsExactly("Keep", "Fallback");
        }

        @Test
        void fetchAllTitledArticles_absurdTotalPages_stopsAtmaxPages() {
            when(articleClient.fetchPage(anyInt()))
                    .thenReturn(pageOf(1, 999));

            articleService.fetchAllTitledArticles();

            verify(articleClient, times(20)).fetchPage(anyInt());
        }

        @Test
        void fetchAllTitledArticles_failureMidWalk_propagates() {
            when(articleClient.fetchPage(1))
                    .thenReturn(pageOf(1, 3, titled("One")));
            when(articleClient.fetchPage(2))
                    .thenThrow(new HackerRankUnavailableException("HackerRank 503: ", null));

            assertThatThrownBy(() -> articleService.fetchAllTitledArticles())
                    .isInstanceOf(HackerRankUnavailableException.class);
            verify(articleClient, times(4)).fetchPage(anyInt());
        }
    }
}
