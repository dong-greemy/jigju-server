package com.jigju.server.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jigju.server.common.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.Map;
@Service
public class SearchExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResponseEntity<ApiErrorResponse> handleApiException(Exception e) {
        if (e instanceof HttpClientErrorException || e instanceof HttpServerErrorException) {
            var httpException = (HttpClientErrorException) e;
            String responseBody = httpException.getResponseBodyAsString();

            try {
                Map<String, String> errorMap = objectMapper.readValue(responseBody, Map.class);
                String errorCode = errorMap.getOrDefault("code", "Unknown");
                String errorMessage = errorMap.getOrDefault("message", "No message");

                return buildErrorResponse(httpException.getStatusCode().value(), errorCode, errorMessage);

            } catch (Exception parseException) {
                return buildErrorResponse(httpException.getStatusCode().value(), "PARSE_ERROR", responseBody);
            }

        } else if (e instanceof RestClientException) {
            return buildErrorResponse(500, "REST_CLIENT_ERROR", e.getMessage());
        } else {
            return buildErrorResponse(500, "UNKNOWN_ERROR", e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(int status, String code, String message) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .errorCode(code)
                .message(message)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}
