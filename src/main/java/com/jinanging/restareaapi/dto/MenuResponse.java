package com.jinanging.restareaapi.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record MenuResponse(String restStopId, List<Item> menus) {
    public record Item(
            String name,
            String description,
            int priceWon,
            @Schema(description = "API 서버 기준 이미지 상대 경로입니다.", example = "/images/menu/examples/yukgaejang.jpg")
            String imageUrl
    ) {}
}
