package com.jigju.server.search.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_keyword")
@Getter
public class SearchKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String keyword;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String mapx;

    @Column(nullable = false)
    private String mapy;

    @Column(nullable = false)
    private int count;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected SearchKeyword() {};

    public SearchKeyword(String keyword, String address, String category, String mapx, String mapy) {
        this.keyword = keyword;
        this.address = address;
        this.category = category;
        this.mapx = mapx;
        this.mapy = mapy;
        this.count = 1;
    }

    public void incrementCount() {
        this.count++;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        if (this.count == 0) this.count = 1;
        this.updatedAt = LocalDateTime.now();
    }
}
