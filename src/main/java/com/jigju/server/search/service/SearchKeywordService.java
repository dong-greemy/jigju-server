package com.jigju.server.search.service;

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
    public void recordKeywordAsync(String keyword) {
        CompletableFuture.runAsync(() -> recordKeyword(keyword));
    }

    private void recordKeyword(String keyword) {
        repository.findByKeyword(keyword)
                  .ifPresentOrElse(entity -> {
                      entity.incrementCount();
                      repository.save(entity);
                  }, () -> {
                      SearchKeyword newKeyword = new SearchKeyword(keyword);
                      repository.save(newKeyword);
                  });
    }

    public List<SearchKeyword> getTopSearchKeywords() {
        return repository.findTop10ByOrderByCountDesc();
    }
}