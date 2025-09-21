package com.jigju.server.location.util;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTWriter;

public class GeoUtils {
//    private static final double BUS_AVERAGE_SPEED_KM_PER_MIN = 0.23;
//    private static final double SUBWAY_AVERAGE_SPEED_KM_PER_MIN = 0.55;
    private static final double NARROW_CIRCLE = 0.1;
    private static final int POLYGON_SIDES = 64;

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public static double calculateRadiusKm(int min) {
        return NARROW_CIRCLE * min * 1000;
    }

    public static String generateCircularPolygonWKT(double lon, double lat, int time) throws Exception {
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

    public static Coordinate findCentroid (Coordinate[] points) {
        double area = 0, x = 0, y = 0;
        int len = points.length;
        Coordinate p1, p2;
        double f;

        for (int i = 0, j = len - 1; i < len; j = i++) {
            p1 = points[i];
            p2 = points[j];

            f = p1.x * p2.y - p2.x * p1.y;
            x += (p1.x + p2.x) * f;
            y += (p1.y + p2.y) * f;
            area += f;
        }

        area /= 2.0; // 다각형 면적
        x /= (6.0 * area);
        y /= (6.0 * area);

        return  new Coordinate(x, y);
    }
}
