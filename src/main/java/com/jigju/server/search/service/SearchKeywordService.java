package com.jigju.server.search.service;

import com.jigju.server.search.dto.SearchResponse;
import com.jigju.server.search.entity.SearchKeyword;
import com.jigju.server.search.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class SearchKeywordService {

    private final SearchKeywordRepository repository;

    @Async
    public void recordKeywordAsync(SearchResponse.LocationItem item) {
        CompletableFuture.runAsync(() -> recordKeyword(item));
    }

    private void recordKeyword(SearchResponse.LocationItem item) {
        repository.findByKeyword(item.getTitle())
                  .ifPresentOrElse(entity -> {
                      entity.incrementCount();
                      repository.save(entity);
                  }, () -> repository.save(item.toEntity()));

    }

    public List<SearchKeyword> getTopSearchKeywords() {
        return repository.findTop10ByOrderByCountDesc();
    }
}