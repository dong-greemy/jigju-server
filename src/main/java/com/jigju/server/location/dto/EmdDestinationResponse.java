package com.jigju.server.location.dto;

import lombok.*;
import org.locationtech.jts.geom.Coordinate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmdDestinationResponse {
    private String province;       // 시/도
    private String district;       // 시/군/구
    private String emdName;        // 동 이름 (예: 연희동)
    private String officeName;     // 주민센터 이름 (없으면 null)
    private Coordinate location;   // 주민센터 좌표 or centroid 좌표
    private Integer postalCode;
    private String address;        // 도로명 주소
}
