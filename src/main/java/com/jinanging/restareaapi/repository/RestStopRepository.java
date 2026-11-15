package com.jinanging.restareaapi.repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import com.jinanging.restareaapi.domain.MenuItem;
import com.jinanging.restareaapi.domain.RestStop;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class RestStopRepository {
    // Coordinates and facilities: Korea Expressway Corporation, 2026-09-17.
    private final List<RestStop> stops = List.of(
            stop("anseong-busan", "안성휴게소", 1, 37.013407, 127.1448001, "LOW",
                    List.of("주유소", "LPG 충전소", "전기차 충전소"),
                    List.of("할리스커피", "로띠번", "파스쿠찌", "JDX", "드롭탑"), "안성국밥"),
            stop("manghyang-busan", "망향휴게소", 2, 36.8556501, 127.180929, "MEDIUM",
                    List.of("주유소", "LPG 충전소", "전기차 충전소"),
                    List.of("던킨도너츠", "BBQ", "파스쿠찌", "LYNX", "드롭탑"), "망향등심돈가스"),
            stop("cheonan-hodu-busan", "천안호두휴게소", 3, 36.730226, 127.263879, "HIGH",
                    List.of("주유소", "LPG 충전소", "전기차 충전소"),
                    List.of("던킨도너츠", "엔제리너스", "BBQ올리브돈까스", "탐앤탐스"), "김치철판볶음밥"),
            stop("oksan-busan", "옥산휴게소", 4, 36.657743, 127.369817, "LOW",
                    List.of("주유소", "LPG 충전소", "전기차 충전소"),
                    List.of("던킨도너츠", "할리스커피"), "(실속)소고기국밥"),
            stop("jugam-busan", "죽암휴게소", 5, 36.4868101, 127.429261, "HIGH",
                    List.of("주유소", "LPG 충전소", "전기차 충전소"),
                    List.of("던킨도너츠", "롯데리아", "탐앤탐스"), "죽암수제묵밥"),
            stop("okcheon-busan", "옥천휴게소", 6, 36.296863, 127.595223, "MEDIUM",
                    List.of("주유소", "LPG 충전소", "전기차 충전소"),
                    List.of("던킨도너츠", "엔제리너스", "클라이머홀릭", "이마트24"), "콩나물밥"),
            stop("geumgang-busan", "금강휴게소", 7, 36.279148, 127.672231, "HIGH",
                    List.of("주유소", "전기차 충전소"),
                    List.of("엔제리너스", "드롭탑"), "금강도리뱅뱅정식"),
            stop("hwanggan-busan", "황간휴게소", 8, 36.248899, 127.8526701, "LOW",
                    List.of("주유소", "LPG 충전소", "전기차 충전소"),
                    List.of("할리스커피", "용우동", "CU"), "영표국밥"),
            stop("chupungnyeong-busan", "추풍령휴게소", 9, 36.200356, 128.000331, "MEDIUM",
                    List.of("주유소", "LPG 충전소", "전기차 충전소"),
                    List.of("할리스커피"), "돌솥흑돼지불고기정식")
    );

    private final Map<String, List<MenuItem>> menusByRestStop;

    public RestStopRepository(JsonMapper jsonMapper) {
        try (var input = new ClassPathResource("rest-stop-menus.json").getInputStream()) {
            menusByRestStop = jsonMapper.readValue(input, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("휴게소 메뉴 자료를 읽을 수 없습니다.", e);
        }
    }

    public List<MenuItem> findMenusByRestStopId(String id) {
        return menusByRestStop.getOrDefault(id, List.of());
    }

    public List<RestStop> findAll() {
        return stops;
    }

    public Optional<RestStop> findById(String id) {
        return stops.stream().filter(stop -> stop.id().equals(id)).findFirst();
    }

    private static RestStop stop(String id, String name, int order, double latitude, double longitude,
                                    String congestion, List<String> tags, List<String> brands, String menu) {
        return new RestStop(id, name, "경부고속도로", "SOUTHBOUND",
                latitude, longitude, order, congestion, tags, brands,
                new RestStop.Menu(menu, null, "/images/" + id + ".jpg"), false);
    }

}
