package com.jinanging.restareaapi.domain;

import java.util.List;

public record RestStop(
        String id,
        String name,
        String highway,
        String direction,
        double latitude,
        double longitude,
        int routeOrder,
        String congestion,
        List<String> tags,
        List<String> brands,
        Menu featuredMenu,
        boolean demo
) {
    public record Menu(String name, String description, String imageUrl) {}
}
