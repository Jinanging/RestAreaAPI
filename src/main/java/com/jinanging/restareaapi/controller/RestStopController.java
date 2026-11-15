package com.jinanging.restareaapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jinanging.restareaapi.domain.RestStop;
import com.jinanging.restareaapi.dto.FeaturedMenuResponse;
import com.jinanging.restareaapi.dto.MenuResponse;
import com.jinanging.restareaapi.dto.RestStopDto;
import com.jinanging.restareaapi.service.RestStopService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/rest-stops")
public class RestStopController {

    private final RestStopService service;

    public RestStopController(RestStopService service) {
        this.service = service;
    }

    @Operation(
            summary = "전체 휴게소 조회",
            description = "등록된 경부고속도로 부산 방향 휴게소를 통과 순서대로 반환합니다. 검색 화면에서 사용할 수 있습니다.")
    @GetMapping
    public List<RestStopDto> list() {
        return service.list().stream().map(RestStopDto::from).toList();
    }

    @Operation(
            summary = "차량 앞에 있는 휴게소 조회",
            description = """
                    차량의 현재 위치를 기준으로 이미 지나간 휴게소를 제외하고,
                    앞에 있는 휴게소를 경부고속도로 하행 본선 기준 가까운 순서대로 모두 반환합니다.
                    각 휴게소의 featuredMenu에 대표 메뉴와 사진 주소가 포함되며 브랜드와 편의시설도 반환합니다.
                    지원 구간에서 벗어났거나 앞에 남은 휴게소가 없으면 빈 배열을 반환합니다.
                    잘못된 좌표 또는 지원하지 않는 방향은 400을 반환합니다.
                    """)
    @GetMapping("/next")
    public List<RestStopDto> next(
            @Parameter(description = "Pleos에서 받은 차량 위도", example = "37.0181")
            @RequestParam("lat") double latitude,
            @Parameter(description = "Pleos에서 받은 차량 경도", example = "127.143547")
            @RequestParam("lng") double longitude,
            @Parameter(description = "고속도로 통행 방향. 생략하면 부산 방향입니다.", example = "SOUTHBOUND")
            @RequestParam(value = "direction", defaultValue = "SOUTHBOUND") String direction) {
        if (!Double.isFinite(latitude) || Math.abs(latitude) > 90
                || !Double.isFinite(longitude) || Math.abs(longitude) > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바른 위도와 경도가 필요합니다.");
        }
        if (!"SOUTHBOUND".equals(direction)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "경부고속도로 하행만 지원합니다.");
        }

        return service.next(latitude, longitude).stream().map(RestStopDto::from).toList();
    }

    @Operation(
            summary = "휴게소 대표 메뉴 조회",
            description = "목록 응답의 id로 대표 메뉴와 사진 주소를 featuredMenu 객체로 조회합니다. 등록되지 않은 ID는 404를 반환합니다.")
    @GetMapping("/{restStopId}/featured-menus")
    public FeaturedMenuResponse featuredMenus(
            @Parameter(description = "휴게소 조회 응답의 id", example = "anseong-busan")
            @PathVariable("restStopId") String restStopId) {
        RestStop stop = requireRestStop(restStopId);
        return new FeaturedMenuResponse(stop.id(), RestStopDto.Menu.from(stop.featuredMenu()));
    }

    @Operation(
            summary = "휴게소 전체 메뉴 조회",
            description = "목록 또는 차량 앞 휴게소 조회 응답의 id로 해당 휴게소의 공개 메뉴, 가격, 사진 주소를 조회합니다. imageUrl은 API 서버 기준 /images/로 시작하는 상대 경로입니다. 등록되지 않은 ID는 404를 반환합니다.")
    @GetMapping("/{restStopId}/menus")
    public MenuResponse menus(
            @Parameter(description = "휴게소 조회 응답의 id", example = "anseong-busan")
            @PathVariable("restStopId") String restStopId) {
        RestStop stop = requireRestStop(restStopId);
        List<MenuResponse.Item> menus = service.menus(stop.id()).stream()
                .map(menu -> new MenuResponse.Item(menu.name(), null, menu.priceWon(), menu.imageUrl()))
                .toList();
        return new MenuResponse(stop.id(), menus);
    }

    private RestStop requireRestStop(String restStopId) {
        return service.findById(restStopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "휴게소를 찾을 수 없습니다."));
    }
}
