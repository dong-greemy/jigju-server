package com.jigju.server.location.service;

import com.jigju.server.common.dto.ApiResponse;
import com.jigju.server.common.dto.ErrorResponse;
import com.jigju.server.external.config.KrDataConfig;
import com.jigju.server.external.config.VWorldConfig;
import com.jigju.server.location.dto.*;
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
    private final KrDataConfig krDataConfig;
    private final RestTemplate restTemplate;

    public ResponseEntity<ApiResponse<LocationResponse>> findEmdsWithinPolygon(String polygon, int page, HttpEntity<Void> entity) throws Exception {

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

    public ArrayList<LocationResponse.Feature> findAllPagedEmdsWithinPolygon(double startX, double startY, int time) throws Exception {
        String polygon = GeoUtils.generateCircularPolygonWKT(startX, startY, time);
        ArrayList<LocationResponse.Feature> result = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        int curPage = 1;
        int maxPage = Integer.MAX_VALUE;

        while (curPage <= maxPage) {
            ResponseEntity<ApiResponse<LocationResponse>> responseEntity = findEmdsWithinPolygon(polygon, curPage, entity);
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

    public ResponseEntity<ApiResponse<GeocoderCoordsResponse>> convertAddressToCoords(String address) throws Exception {
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
                GeocoderCoordsResponse.Point point = convertAddressToCoords(emdOfficeLoc.getAddress()).getBody()
                                                                                                      .data()
                                                                                                      .getResponse()
                                                                                                      .getResult()
                                                                                                      .getPoint();
                EmdDestinationResponse destination = mapToDestinationResponse(point, emdOfficeLoc, props);
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
                        null,
                        null
                );
                emdDestinations.add(destination);
            }

        }

        return emdDestinations;
    }

    private static EmdDestinationResponse mapToDestinationResponse(GeocoderCoordsResponse.Point point, EmdOfficeLocation emdOfficeLoc, LocationResponse.Properties props) {
        Coordinate coords = new Coordinate(point.getX(), point.getY());

        return new EmdDestinationResponse(
                emdOfficeLoc.getProvince(),
                emdOfficeLoc.getDistrict(),
                props.getEmd_kor_nm(),
                emdOfficeLoc.getEmd_office(),
                coords,
                emdOfficeLoc.getPostal_code(),
                emdOfficeLoc.getAddress()
        );
    }

    public ArrayList<EmdDestinationResponse> getNearbyEmds(double startX, double startY, int time) throws Exception {
        try {
            ArrayList<LocationResponse.Feature> properties = findAllPagedEmdsWithinPolygon(startX, startY, time);
            return findEmdDestinations(properties);

        } catch (Exception e) {
            System.out.println("getNearbyEmdOffices 처리 중 오류 발생" + e.getMessage());
            throw e;
        }
    }

    public String getTransferRouteTest(double startX, double startY, double endX, double endY) throws Exception {
        URI url = UriComponentsBuilder.fromUriString(krDataConfig.getApiUrl())
                                      .path("/getPathInfoByBusNSub")
                                      .queryParam("startX", startX)
                                      .queryParam("startY", startY)
                                      .queryParam("endX", endX)
                                      .queryParam("endY", endY)
                                      .queryParam("serviceKey", krDataConfig.getServiceKey())
                                      .queryParam("resultType", "json")
                                      .build(true)
                                      .toUri();


        return restTemplate.getForObject(url, String.class);

    }

    public ApiResponse<PathInfoResponse.Item> getTransferRoute(double startX, double startY, double endX, double endY) throws Exception {
        URI url = UriComponentsBuilder.fromUriString(krDataConfig.getApiUrl())
                                      .path("/getPathInfoByBusNSub")
                                      .queryParam("startX", startX)
                                      .queryParam("startY", startY)
                                      .queryParam("endX", endX)
                                      .queryParam("endY", endY)
                                      .queryParam("serviceKey", krDataConfig.getServiceKey())
                                      .queryParam("resultType", "json")
                                      .build(true)
                                      .toUri();

        HttpHeaders headers = new HttpHeaders();
//        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
//        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<PathInfoResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, PathInfoResponse.class);
            if (response.getBody() == null || response.getBody()
                                                      .getMsgBody() == null || response.getBody()
                                                                                       .getMsgBody()
                                                                                       .getItemList() == null) {
                return ApiResponse.success(null);
            } else {
                return ApiResponse.success(response.getBody()
                                                   .getMsgBody()
                                                   .getItemList()
                                                   .getFirst());
            }
        } catch (Exception e) {
            System.out.println("[PATH][EX] " + e.getClass()
                                                .getName() + ": " + e.getMessage());

            throw e;
        }
    }

    public NearbyEmdDto getTop3NearbyEmd(double startX, double startY, int time) throws Exception {
        NearbyEmdDto result = new NearbyEmdDto();
        NearbyEmdDto.Target target = new NearbyEmdDto.Target();
        target.setX(startX);
        target.setY(startY);
        target.setLimitTime(time);
        target.setAddress(convertCoordsToAddress(new Coordinate(startX, startY)).getBody()
                                                                                .data()
                                                                                .getResponse()
                                                                                .getResult()
                                                                                .getFirst()
                                                                                .getText());

        result.setTarget(target);

        EmdPathResult pathResults = getAllEmdPathList(startX, startY, time);

        ArrayList<EmdTransferPathDto> emdsTransferRoutes = pathResults.getEmdsTransferRoutes();
        int minTime = pathResults.getMinTime();
        int minPath = pathResults.getMinPath();
        ArrayList<EmdTransferPathDto> top3Emds = new ArrayList<>();

        for(EmdTransferPathDto route : emdsTransferRoutes){
            ArrayList<String> tags = new ArrayList<>();
            if (route.getTime() == minTime) {
                tags.add("minTime");
                // minTime = Integer.MAX_VALUE; 3개 반환 위한 것
            }
            if (route.getPathList().size() == minPath) {
                tags.add("minPath");
                // minPath = Integer.MAX_VALUE;
            }

            if (!tags.isEmpty()) {
                route.setTags(tags);
                top3Emds.add(route);
            }
        }

        result.setNearbyEmds(top3Emds);

        return result;
    }

    public EmdPathResult getAllEmdPathList(double startX, double startY, int time) throws Exception {
        ArrayList<EmdTransferPathDto> emdsTransferRoutes = new ArrayList<>();

        ArrayList<EmdDestinationResponse> emdsDestinations = getNearbyEmds(startX, startY, time);

        int minTime = Integer.MAX_VALUE;
        int minPath = Integer.MAX_VALUE;

        for (EmdDestinationResponse dest : emdsDestinations) {
            PathInfoResponse.Item pathInfo =
                    getTransferRoute(startX, startY, dest.getLocation().x, dest.getLocation().y).data();
            EmdTransferPathDto dto = new EmdTransferPathDto();

            if (pathInfo != null && pathInfo.getTime() <= time) {
                dto.setDestination(dest);
                dto.setTime(pathInfo.getTime());
                dto.setDistance(pathInfo.getDistance());
                List<EmdTransferPathDto.Path> paths = getPathDtos(pathInfo);
                dto.setPathList(paths);
                minTime =  Math.min(minTime, dto.getTime());
                minPath = Math.min(minPath, paths.size());

                emdsTransferRoutes.add(dto);
            }
        }
        return new EmdPathResult(emdsTransferRoutes, minTime, minPath);
    }
    private static List<EmdTransferPathDto.Path> getPathDtos(PathInfoResponse.Item pathInfo) {
        List<EmdTransferPathDto.Path> pathDtos = new ArrayList<>();
        for (PathInfoResponse.Path p : pathInfo.getPathList()) {
            EmdTransferPathDto.Path path = new EmdTransferPathDto.Path();
            path.setRouteNm(p.getRouteNm());
            path.setTransit(p.getRouteId() == null ? "subway" : "bus");
            path.setRouteId(p.getRouteId());
            path.setFname(p.getFname());
            path.setTname(p.getTname());
            pathDtos.add(path);
        }
        return pathDtos;
    }


}
