package com.jigju.server.location.service;

import com.jigju.server.common.dto.ApiResponse;
import com.jigju.server.common.dto.ErrorResponse;
import com.jigju.server.external.config.VWorldConfig;
import com.jigju.server.location.dto.EmdDestinationResponse;
import com.jigju.server.location.dto.GeocoderAddressResponse;
import com.jigju.server.location.dto.GeocoderCoordsResponse;
import com.jigju.server.location.dto.LocationResponse;
import com.jigju.server.location.entity.EmdOfficeLocation;
import com.jigju.server.location.repository.EmdOfficeLocationRepository;
import com.jigju.server.location.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
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

    public ResponseEntity<ApiResponse<LocationResponse>> getOfficesWithinPolygon(String polygon, int page, HttpEntity<Void> entity) throws Exception {

        URI url = findLocationUriBuilder(polygon, page);

        try {
            ResponseEntity<LocationResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, LocationResponse.class);

            return ResponseEntity.ok(ApiResponse.success(response.getBody()));
        } catch (Exception e) {
            System.out.println("[CENTERS][EX] " + e.getClass()
                                                   .getName() + ": " + e.getMessage());

            ErrorResponse errorResponse = restTemplate.getForObject(url, ErrorResponse.class);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(ApiResponse.error(errorResponse));
        }
    }

    public ArrayList<LocationResponse.Feature> getNeighboringDistricts(double startX, double startY, int time) throws Exception {
        String polygon = GeoUtils.generateCircularPolygonWKT(startX, startY, time);
        ArrayList<LocationResponse.Feature> result = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        int curPage = 1;
        int maxPage = Integer.MAX_VALUE;

        while (curPage <= maxPage) {
            ResponseEntity<ApiResponse<LocationResponse>> responseEntity = getOfficesWithinPolygon(polygon, curPage, entity);
            if (!responseEntity.getStatusCode()
                               .is2xxSuccessful()) break;

            assert responseEntity.getBody() != null;
            LocationResponse locationResponse = responseEntity.getBody()
                                                              .data();

            if (curPage == 1) {
                maxPage = locationResponse.getResponse()
                                          .getPage()
                                          .getTotal();
            }

            result.addAll(locationResponse.getResponse()
                                          .getResult()
                                          .getFeatureCollection()
                                          .getFeatures());

            curPage++;
        }

        return result;
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

    public ResponseEntity<ApiResponse<Object>> convertAddressToCoords(String address) throws Exception {
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
            ResponseEntity<GeocoderCoordsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    GeocoderCoordsResponse.class
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

    public ResponseEntity<ApiResponse<GeocoderAddressResponse>> convertCoordsToAddress(Coordinate coords) throws Exception {

        String point = coords.x + "," + coords.y;
        URI url = UriComponentsBuilder.fromUriString(vworldConfig.getApiUrl())
                                      .path("/address")
                                      .queryParam("key", vworldConfig.getKey())
                                      .queryParam("request", "getAddress")
                                      .queryParam("service", "address")
                                      .queryParam("point", point)
                                      .queryParam("type", "ROAD")
                                      .queryParam("simple", true)
                                      .queryParam("format", "json")
                                      .encode()
                                      .build()
                                      .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
        try {
            ResponseEntity<GeocoderAddressResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    GeocoderAddressResponse.class
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

    private final EmdOfficeLocationRepository administrativeEmdRepository;

    public ArrayList<EmdDestinationResponse> findEmdDestinations(ArrayList<LocationResponse.Feature> features) throws Exception {
        ArrayList<EmdDestinationResponse> emdDestinations = new ArrayList<>();

        for (LocationResponse.Feature feature : features) {
            LocationResponse.Properties props = feature.getProperties();

            String[] addressParts = props.getFull_nm()
                                         .split(" ");
            EmdOfficeLocation emdOfficeLoc = administrativeEmdRepository.findMatchedEmdOffice(addressParts[0], addressParts[1], props.getEmd_kor_nm());

            if (emdOfficeLoc != null) { // 행정동일 경우 행정복지센터 반환
                EmdDestinationResponse destination = new EmdDestinationResponse(
                        emdOfficeLoc.getProvince(),
                        emdOfficeLoc.getDistrict(),
                        props.getEmd_kor_nm(),
                        emdOfficeLoc.getEmd_office(),
                        null,
                        emdOfficeLoc.getPostal_code(),
                        emdOfficeLoc.getAddress()
                );
                emdDestinations.add(destination);
            } else {  // 법정동일 경우 폴리곤 정중앙 반환
                List<List<Double>> linearRing = feature.getGeometry()
                                                       .getCoordinates()
                                                       .getFirst()
                                                       .getFirst();

                Coordinate[] points = new Coordinate[linearRing.size()];

                for (int i = 0; i < linearRing.size(); i++) {
                    List<Double> coord = linearRing.get(i);
                    points[i] = new Coordinate(coord.get(0), coord.get(1));
                }

                Coordinate centroid = GeoUtils.findCentroid(points);

                EmdDestinationResponse destination = new EmdDestinationResponse(
                        addressParts[0],
                        addressParts[1],
                        props.getEmd_kor_nm(),
                        null,
                        centroid,
                        -1,
                        null
                );
                emdDestinations.add(destination);
            }

        }

        return emdDestinations;
    }

    public ArrayList<EmdDestinationResponse> getNearbyEmds(double startX, double startY, int time) throws Exception {
        try {
            ArrayList<LocationResponse.Feature> properties = getNeighboringDistricts(startX, startY, time);
            return findEmdDestinations(properties);

        } catch (Exception e) {
            System.out.println("getNearbyEmdOffices 처리 중 오류 발생" + e.getMessage());
            throw e;
        }
    }
}
