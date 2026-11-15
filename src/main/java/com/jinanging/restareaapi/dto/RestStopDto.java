package com.jinanging.restareaapi.dto;

import java.util.List;

import com.jinanging.restareaapi.domain.RestStop;

import io.swagger.v3.oas.annotations.media.Schema;

public record RestStopDto(
        String id,
        String name,
        String highway,
        String direction,
        double latitude,
        double longitude,
        int routeOrder,
        @Schema(description = "시연용 혼잡도. LOW는 여유, MEDIUM은 보통, HIGH는 혼잡입니다.",
                allowableValues = {"LOW", "MEDIUM", "HIGH"}, example = "LOW")
        String congestion,
        List<String> tags,
        List<String> brands,
        Menu featuredMenu,
        boolean demo
) {
    public static RestStopDto from(RestStop stop) {
        return new RestStopDto(stop.id(), stop.name(), stop.highway(), stop.direction(),
                stop.latitude(), stop.longitude(), stop.routeOrder(), stop.congestion(),
                stop.tags(), stop.brands(), Menu.from(stop.featuredMenu()), stop.demo());
    }

    public record Menu(String name, String description, String imageUrl) {
        public static Menu from(RestStop.Menu menu) {
            return new Menu(menu.name(), menu.description(), menu.imageUrl());
        }
    }
}
