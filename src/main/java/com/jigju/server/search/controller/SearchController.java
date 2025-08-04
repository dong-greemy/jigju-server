package com.jigju.server.search.controller;

import com.jigju.server.common.dto.ApiResponse;
import com.jigju.server.common.dto.ErrorResponse;
import com.jigju.server.external.config.NaverConfig;
import com.jigju.server.search.dto.SearchResponse;
import com.jigju.server.search.entity.SearchKeyword;
import com.jigju.server.search.service.SearchKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchKeywordService searchKeywordService;
    private final NaverConfig naverConfig;
    private final RestTemplate restTemplate;

    private static final int DEFAULT_DISPLAY_COUNT = 5;

    @GetMapping("/location")

    public ResponseEntity<ApiResponse<Object>> searchLocation(
            @RequestParam String query
    ) {
        URI url = UriComponentsBuilder
                .fromUriString(naverConfig.getApiUrl())
                .path("/v1/search/local.json")
                .queryParam("query", query)
                .queryParam("display", DEFAULT_DISPLAY_COUNT)
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverConfig.getClientId());
        headers.set("X-Naver-Client-Secret", naverConfig.getClientSecret());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<SearchResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    SearchResponse.class
            );

            SearchResponse body = response.getBody();
            if (body == null || body.getItems() == null) {
                return ResponseEntity.ok(ApiResponse.success(List.of()));
            }

            searchKeywordService.recordKeywordAsync(query);

            return ResponseEntity.ok(ApiResponse.success(body.getItems()));
        } catch (Exception e) {
            ErrorResponse errorResponse = restTemplate.getForObject(url, ErrorResponse.class);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(errorResponse));
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<Object>> popularKeywords() {
        List<SearchKeyword> topKeywords = searchKeywordService.getTopSearchKeywords();
        List<String> result = topKeywords.stream()
                                         .map(SearchKeyword::getKeyword)
                                         .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

}
