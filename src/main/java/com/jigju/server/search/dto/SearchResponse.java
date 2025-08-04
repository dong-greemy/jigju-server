package com.jigju.server.search.dto;


import com.jigju.server.search.entity.SearchKeyword;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchResponse {
    private String lastBuildDate;
    private int total;
    private int start;
    private int display;
    private List<LocationItem> items;

    @Getter
    @Builder
    public static class LocationItem {
        private String title;
        private String address;
        private String category;
        private String mapx;
        private String mapy;

        public SearchKeyword toEntity() {
            return new SearchKeyword(
                    this.title,
                    this.address,
                    this.category,
                    this.mapx,
                    this.mapy
            );
        }
    }


}
