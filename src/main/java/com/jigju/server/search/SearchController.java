package com.jigju.server.search;

import com.jigju.server.config.KakaoConfig;
import com.jigju.server.config.NaverConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class SearchController {

//    private final KakaoConfig kakaoConfig;
    private final NaverConfig naverConfig;
    private final RestTemplate restTemplate;

    @GetMapping("/search/location")
    public ResponseEntity<?> searchLocation(
        @RequestParam String from
    ) {
        URI apiUrl = UriComponentsBuilder
                .fromUriString(naverConfig.getApiUrl())
                .path("/v1/search/local.json")
                .queryParam("query", from)
                .queryParam("display", 5)
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverConfig.getClientId());
        headers.set("X-Naver-Client-Secret", naverConfig.getClientSecret());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<SearchResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    SearchResponse.class
            );

            SearchResponse body = response.getBody();
            if (body == null || body.getItems() == null) {
                return ResponseEntity.ok(List.of());
            }

            return ResponseEntity.ok(body.getItems());
        } catch (Exception e) {
            SearchExceptionHandler exceptionHandler = new SearchExceptionHandler();
            return exceptionHandler.handleApiException(e);
        }
    }

//
//    @GetMapping("/search/location/kakao")
//    public ResponseEntity<String> searchLocationKakao(
//        @RequestParam String from
//    ) {
//        URI apiUrl = UriComponentsBuilder
//                .fromUriString(kakaoConfig.getApiUrl())
//                .path("/v2/local/search/keyword.json")
//                .queryParam("query", from)
//                .encode()
//                .build()
//                .toUri();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", kakaoConfig.getAuthorization());
//        HttpEntity<Void> entity = new HttpEntity<>(headers);
//
//        try {
//            ResponseEntity<String> response = restTemplate.exchange(
//                    apiUrl,
//                    HttpMethod.GET,
//                    entity,
//                    String.class
//            );
//            return ResponseEntity.ok(response.getBody());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("API 호출 실패: " + e.getMessage());
//        }
//    }
}
