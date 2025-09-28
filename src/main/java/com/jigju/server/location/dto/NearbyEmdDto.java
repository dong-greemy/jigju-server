package com.jigju.server.location.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class NearbyEmdDto {
    private Target target;
    private ArrayList<EmdTransferPathDto> nearbyEmds;

    @Getter
    @Setter
    public static class Target {
        private String address;
        private double x;
        private double y;
        private int limitTime;
    }
}
