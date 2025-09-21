package com.jigju.server.location.controller;

import com.jigju.server.common.dto.ApiResponse;
import com.jigju.server.location.dto.EmdDestinationResponse;
import com.jigju.server.location.dto.GeocoderAddressResponse;
import com.jigju.server.location.dto.LocationResponse;
import com.jigju.server.location.service.NearbyLocationService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
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
    public ArrayList<LocationResponse.Feature> getNearbyPolygon(
            @RequestParam double x,
            @RequestParam double y,
            @RequestParam int time) throws Exception {
        return nearbyLocationService.getNeighboringDistricts(x, y, time);
    }

    @GetMapping("/geocoder/coords")
    public ResponseEntity<ApiResponse<Object>> getCoords(@RequestParam String address) throws Exception {
        return nearbyLocationService.convertAddressToCoords(address);
    }

    @GetMapping("/geocoder/address")
    public ResponseEntity<ApiResponse<GeocoderAddressResponse>> getAddress(@RequestParam double x, double y) throws Exception {
        Coordinate coords = new Coordinate(x, y);
        return nearbyLocationService.convertCoordsToAddress(coords);
    }

    @GetMapping("/nearbyEmd")
    public ArrayList<EmdDestinationResponse> getNearbyEmds(
            @RequestParam double x,
            @RequestParam double y,
            @RequestParam int time) throws Exception {
        return nearbyLocationService.getNearbyEmds(x, y, time);
    }
}
