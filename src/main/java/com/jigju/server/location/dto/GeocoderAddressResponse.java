package com.jigju.server.location.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeocoderAddressResponse {
    private Response response;

    @Getter
    @Setter
    public static class Response {
        private List<Result> result;
    }

    @Getter
    @Setter
    public static class Result {
        private int zipcode;
        private String text;
    }
}
