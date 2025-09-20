package com.jigju.server.location.service;

import com.jigju.server.common.dto.ApiResponse;
import com.jigju.server.common.dto.ErrorResponse;
import com.jigju.server.external.config.VWorldConfig;
import com.jigju.server.location.dto.GeocodeResponse;
import com.jigju.server.location.dto.LocationResponse;
import com.jigju.server.location.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NearbyLocationService {

    private final VWorldConfig vworldConfig;
    private final RestTemplate restTemplate;

    public ResponseEntity<ApiResponse<LocationResponse>> getNearbyCenters(String polygon, int page, HttpEntity<Void> entity) throws Exception {

        URI url =  findLocationUriBuilder(polygon, page);

        try {
            ResponseEntity<LocationResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, LocationResponse.class);

            return ResponseEntity.ok(ApiResponse.success(response.getBody()));
        } catch (Exception e) {
            System.out.println("[CENTERS][EX] " + e.getClass().getName() + ": " + e.getMessage());

            ErrorResponse errorResponse = restTemplate.getForObject(url, ErrorResponse.class);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(ApiResponse.error(errorResponse));
        }
    }

    public  ArrayList<LocationResponse.Properties> getNearbyDistricts(double startX, double startY, int time) throws Exception {
        String polygon = GeoUtils.generateCircularPolygonWKT(startX, startY, time);
        System.out.println("Polygon: " + polygon);
        ArrayList<LocationResponse.Properties> properties = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        int curPage = 1;
        int maxPage = Integer.MAX_VALUE;

        while(curPage <= maxPage) {
            ResponseEntity<ApiResponse<LocationResponse>> responseEntity = getNearbyCenters(polygon, curPage, entity);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) break;

            LocationResponse locationResponse = responseEntity.getBody().data();

            if (curPage == 1) {
                maxPage = locationResponse.getResponse().getPage().getTotal();
                System.out.println("Total:"+locationResponse.getResponse().getRecord().getTotal());
            }

            System.out.println("cur page:"+curPage);
            locationResponse.getResponse().getResult().getFeatureCollection().getFeatures().stream().map(LocationResponse.Feature::getProperties).forEach(properties::add);
            curPage++;
        }

        return properties;
    }

    public URI findLocationUriBuilder(String polygon, int page) {
        return UriComponentsBuilder.fromUriString(vworldConfig.getApiUrl())
                                   .path("/data")
                                   .queryParam("key", vworldConfig.getKey())
                                   .queryParam("request", "GetFeature")
                                   .queryParam("data", "LT_C_ADEMD_INFO")
                                   .queryParam("geomFilter", polygon)
                                   .queryParam("domain", "https://dg-client-six.vercel.app/")
                                   .queryParam("size", "20")
                                   .queryParam("format", "json")
                                   .queryParam("page", page)
                                   .encode()
                                   .build()
                                   .toUri();

    }

    public ResponseEntity<ApiResponse<Object>> getGeocoder(String address) throws Exception {
        URI url = UriComponentsBuilder.fromUriString(vworldConfig.getApiUrl())
                                      .path("/address")
                                      .queryParam("key", vworldConfig.getKey())
                                      .queryParam("request", "GetCoord")
                                      .queryParam("service", "address")
                                      .queryParam("type", "ROAD")
                                      .queryParam("address", address)
                                      .queryParam("simple", true)
                                      .queryParam("format", "json")
                                      .encode()
                                      .build()
                                      .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
        try {
            ResponseEntity<GeocodeResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    GeocodeResponse.class
            );

            return ResponseEntity.ok(ApiResponse.success(response.getBody()));
        } catch (Exception e) {
            System.out.println("[GEOCODE][EX] " + e.getClass()
                                                   .getName() + ": " + e.getMessage());

            ErrorResponse errorResponse = restTemplate.getForObject(url, ErrorResponse.class);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(ApiResponse.error(errorResponse));
        }
    }
}
