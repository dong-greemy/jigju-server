package com.jigju.server.location.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LocationResponse {
    private Response response;

    @Getter
    @Setter
    public static class Response {
        private ServiceInfo service;
        private Record record;
        private Page page;
        private Result result;
    }

    @Getter
    @Setter
    public static class ServiceInfo {
        private String name;
        private String version;
        private String operation;
        private String time;
    }

    @Getter
    @Setter
    public static class Record {
        private String total;
        private String current;
    }

    @Getter
    @Setter
    public static class Page {
        private String total;
        private String current;
        private String size;
    }

    @Getter
    @Setter
    public static class Result {
        private FeatureCollection featureCollection;
    }

    @Getter
    @Setter
    public static class FeatureCollection {
        private String type;
        private List<Double> bbox;
        private List<Feature> features;
    }

    @Getter
    @Setter
    public static class Feature {
        private String type;
//        private Geometry geometry;
        private Properties properties;
        private String id;
    }

    @Getter
    @Setter
    public static class Geometry {
        private String type;
        private List<List<List<List<Double>>>> coordinates;
    }

    @Getter
    @Setter
    public static class Properties {
        private String emd_eng_nm;
        private String emd_kor_nm;
        private String full_nm;
        private String emd_cd;
    }
}
