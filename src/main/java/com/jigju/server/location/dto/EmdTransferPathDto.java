package com.jigju.server.location.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EmdTransferPathDto {

    private EmdDestinationResponse destination;
    private int time;
    private int distance;
    private ArrayList<String> tags;
    private List<Path> pathList;

    @Getter
    @Setter
    public static class Path {
        private String routeNm;
        private String transit;
        private Integer routeId;
        private String fname;
        private String tname;

    }
}
