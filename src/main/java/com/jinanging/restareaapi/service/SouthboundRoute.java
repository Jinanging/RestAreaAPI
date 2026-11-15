package com.jinanging.restareaapi.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

import com.jinanging.restareaapi.domain.RestStop;

final class SouthboundRoute {
    private static final double METERS_PER_LATITUDE_DEGREE = 111_195;
    private static final double MAX_ROUTE_DISTANCE_METERS = 150;
    private final List<Point> points = new ArrayList<>();
    private final Map<String, Integer> entrances = new HashMap<>();

    SouthboundRoute() {
        try (var reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("gyeongbu-southbound.csv").getInputStream(), StandardCharsets.UTF_8))) {
            for (String row; (row = reader.readLine()) != null; ) {
                String[] fields = row.split(",", -1);
                if (!fields[2].isEmpty()) {
                    entrances.put(fields[2], points.size());
                }
                points.add(new Point(Double.parseDouble(fields[0]), Double.parseDouble(fields[1])));
            }
        } catch (IOException e) {
            throw new IllegalStateException("경부고속도로 하행 경로를 읽을 수 없습니다.", e);
        }
    }

    List<RestStop> next(double latitude, double longitude, List<RestStop> stops) {
        double longitudeScale = METERS_PER_LATITUDE_DEGREE * Math.cos(Math.toRadians(latitude));
        double nearestDistance = Double.POSITIVE_INFINITY;
        double progress = 0;

        for (int i = 0; i < points.size() - 1; i++) {
            Point start = points.get(i);
            Point end = points.get(i + 1);
            double x = (start.longitude() - longitude) * longitudeScale;
            double y = (start.latitude() - latitude) * METERS_PER_LATITUDE_DEGREE;
            double dx = (end.longitude() - start.longitude()) * longitudeScale;
            double dy = (end.latitude() - start.latitude()) * METERS_PER_LATITUDE_DEGREE;
            double lengthSquared = dx * dx + dy * dy;
            if (lengthSquared == 0) {
                continue;
            }

            double fraction = Math.max(0, Math.min(1, -(x * dx + y * dy) / lengthSquared));
            double distance = Math.hypot(x + fraction * dx, y + fraction * dy);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                progress = i + fraction;
            }
        }

        if (nearestDistance > MAX_ROUTE_DISTANCE_METERS) {
            return List.of();
        }

        double vehicleProgress = progress;
        return stops.stream()
                .filter(stop -> entrances.get(stop.id()) >= vehicleProgress)
                // Entrance indexes increase with distance along this one-way route.
                .sorted(Comparator.comparingInt(stop -> entrances.get(stop.id())))
                .toList();
    }

    private record Point(double latitude, double longitude) {}
}
