package com.jigju.server.location.controller;

import com.jigju.server.common.dto.ApiResponse;
import com.jigju.server.location.dto.LocationResponse;
import com.jigju.server.location.service.NearbyLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/location")
public class NearbyLocationController {
    private final NearbyLocationService nearbyLocationService;

    @GetMapping("/polygon")
//    public ResponseEntity<ApiResponse<Object>> getNearbyPolygon(
    public ArrayList<LocationResponse.Properties> getNearbyPolygon(
            @RequestParam double x,
            @RequestParam double y,
            @RequestParam int time) throws Exception {
        return nearbyLocationService.getNearbyDistricts(x, y, time);
    }

    @GetMapping("/geocode")
    public ResponseEntity<ApiResponse<Object>> getGeocode(@RequestParam String address) throws Exception {
        return nearbyLocationService.getGeocoder(address);
    }
}
