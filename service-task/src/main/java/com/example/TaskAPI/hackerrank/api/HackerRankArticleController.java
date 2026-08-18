package com.example.TaskAPI.hackerrank.api;

import com.example.TaskAPI.hackerrank.api.dto.HackerRankArticleResponse;
import com.example.TaskAPI.hackerrank.service.HackerRankArticleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "HackerRank Integration")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/integrations/hackerrank")
public class HackerRankArticleController {
    private final HackerRankArticleService articleService;

    @GetMapping("articles")
    public ResponseEntity<List<HackerRankArticleResponse>> getArticles(
            @RequestParam(defaultValue = "1") @Min(1) @Max(HackerRankArticleService.MAX_PAGES) int pageNum) {
        return ResponseEntity.ok(
                articleService.fetchPage(pageNum).data().stream()
                        .map(HackerRankArticleResponse::from)
                        .toList());
    }
}
