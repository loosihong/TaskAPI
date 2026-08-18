package com.example.TaskAPI.hackerrank.client;

import com.example.TaskAPI.hackerrank.client.dto.HackerRankArticle;
import com.example.TaskAPI.hackerrank.client.dto.HackerRankPage;
import com.example.TaskAPI.hackerrank.config.HackerRankClientConfig;
import com.example.TaskAPI.hackerrank.exception.HackerRankApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class HackerRankArticleClientTest {
    private final static String BASE_URL = "http://hackerrank.test";

    private MockRestServiceServer server;
    private HackerRankArticleClient client;

    @BeforeEach
    public void setup() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);

        HackerRankClientConfig.customize(builder);
        server = MockRestServiceServer.bindTo(builder).build();
        client = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(builder.build()))
                .build()
                .createClient(HackerRankArticleClient.class);
    }

    @Test
    @DisplayName("binds the snake_case envelope and generis data list")
    void fetchPage_mapsEnvelope() {
        String json = """
                {"page":1,"per_page":10,"total":256,"total_pages":26,
                 "data":[{"title":null,"story_title":"Show HN: something",
                          "url":null,"story_url":"https://example.com",
                          "author":"alice","num_comments":3,
                          "story_id":42,"parent_id":null,"created_at":1512265991}]}
                """;

        server.expect(requestTo(BASE_URL + "/api/articles?page=1"))
                .andExpect(header("User-Agent", "TaskAPI/1.0"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        HackerRankPage<HackerRankArticle> result = client.fetchPage(1);

        assertThat(result.perPage()).isEqualTo(10);
        assertThat(result.totalPages()).isEqualTo(26);
        assertThat(result.hasNextPage()).isTrue();
        assertThat(result.data()).singleElement()
                .extracting(HackerRankArticle::displayTitle)
                .isEqualTo("Show HN: something");
        server.verify();
    }

    @Test
    @DisplayName("upstream error status is translated to HackerRankApiException")
    void fetchPage_upstreamReturns503_throwsHackerRankApiException() {
        server.expect(requestTo(BASE_URL + "/api/articles?page=1"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.fetchPage(1))
                .isInstanceOf(HackerRankApiException.class)
                .hasMessageContaining("503");
    }
}
