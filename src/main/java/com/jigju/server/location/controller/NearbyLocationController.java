package com.jigju.server.location.controller;

import com.jigju.server.common.dto.ApiResponse;
import com.jigju.server.location.dto.*;
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
    public ArrayList<LocationResponse.Feature> getNearbyEmdsWithinPolygon(
            @RequestParam double x,
            @RequestParam double y,
            @RequestParam int time) throws Exception {
        return nearbyLocationService.findAllPagedEmdsWithinPolygon(x, y, time);
    }

    @GetMapping("/geocoder/coords")
    public ResponseEntity<ApiResponse<GeocoderCoordsResponse>> getCoords(@RequestParam String address) throws Exception {
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

    @GetMapping("/routes")
    public NearbyEmdDto getEmdRoutes(
            @RequestParam double x,
            @RequestParam double y,
            @RequestParam int time) throws Exception {
        return nearbyLocationService.getTop3NearbyEmd(x, y, time);

    }

    @GetMapping("/krroutes")
    public String getKrRoutes(
            @RequestParam double startX,
            @RequestParam double startY,
            @RequestParam double endX,
            @RequestParam double endY
    ) throws  Exception {
         return nearbyLocationService.getTransferRouteTest(startX,startY,endX, endY);
    }
}
