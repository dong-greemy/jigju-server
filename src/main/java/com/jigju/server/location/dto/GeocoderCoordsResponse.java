package com.jigju.server.location.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeocoderCoordsResponse {
    private Response response;

    @Getter
    @Setter
    public static class Response {
        private Result result;
    }

    @Getter
    @Setter
    public static class Result {
        private String crs;
        private Point point;
    }

    @Getter
    @Setter
    public static class Point {
        private double x;
        private double y;
    }
}
