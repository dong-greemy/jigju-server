package com.jigju.server.location.util;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTWriter;

public class GeoUtils {
    //    private static final double BUS_AVERAGE_SPEED = 0.23;

    private static final double SUBWAY_AVERAGE_SPEED_KM_PER_MIN = 0.55;
    private static final int POLYGON_SIDES = 64;

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public static double calculateRadiusKm(int min) {
        return SUBWAY_AVERAGE_SPEED_KM_PER_MIN * min * 1000;
    }

    public static String generateCircularPolygonWKT(double lon, double lat, int time) throws Exception {
//        double[] coordinates = generateEPSG4326Coordinate(mapx, mapy);
//        double lon = coordinates[0];
//        double lat = coordinates[1];

        // GeoTools의 GeodeticCalculator (기본 WGS84 사용)
        double radiusKm = calculateRadiusKm(time);

        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
        GeodeticCalculator calc = new GeodeticCalculator(crs);

        Coordinate[] coords = new Coordinate[POLYGON_SIDES + 1]; // 닫힌 다각형이므로 마지막=첫번째

        for (int i = 0; i < POLYGON_SIDES; i++) {
            double azimuth = i * (360.0 / POLYGON_SIDES); // 방위각 (0°=북쪽 기준, 시계방향)

            calc.setStartingGeographicPoint(lon, lat); // (경도, 위도)
            calc.setDirection(azimuth, radiusKm); // 거리(km)

            java.awt.geom.Point2D dest = calc.getDestinationGeographicPoint();
            coords[i] = new Coordinate(dest.getX(), dest.getY()); // (lon, lat)
        }

        coords[POLYGON_SIDES] = coords[0]; // 다각형 닫기

        Polygon polygon = geometryFactory.createPolygon(coords);
        return new WKTWriter().write(polygon).replace(" ((", "((");
    }

    public static double[] generateEPSG4326Coordinate(String rawX, String rawY) {
        double lon = Double.parseDouble(rawX.substring(0, 3) + "." + rawX.substring(3));
        double lat = Double.parseDouble(rawY.substring(0, 2) + "." + rawY.substring(2));
        return new double[]{lon, lat};
    }

}
