package com.jinanging.restareaapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestAreaApiApplicationTests {
    @Autowired
    private MockMvc mvc;

    @Test
    void listsNineRealStopsInDrivingOrder() throws Exception {
        mvc.perform(get("/api/rest-stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9))
                .andExpect(jsonPath("$[0].routeOrder").value(1))
                .andExpect(jsonPath("$[0].featuredMenu.name").value("안성국밥"))
                .andExpect(jsonPath("$[0].menus").doesNotHaveJsonPath())
                .andExpect(jsonPath("$[8].routeOrder").value(9))
                .andExpect(jsonPath("$[0].demo").value(false))
                .andExpect(jsonPath("$[0].congestion").value("LOW"))
                .andExpect(jsonPath("$[0].tags[0]").value("주유소"))
                .andExpect(jsonPath("$[0].brands[0]").value("할리스커피"))
                .andExpect(jsonPath("$[0].latitude").value(37.013407));
    }

    @Test
    void returnsAllUpcomingStopsAndDropsAnseongAfterPassingIt() throws Exception {
        mvc.perform(get("/api/rest-stops/next")
                        .param("lat", "37.0181").param("lng", "127.143547"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9))
                .andExpect(jsonPath("$[0].id").value("anseong-busan"))
                .andExpect(jsonPath("$[8].id").value("chupungnyeong-busan"))
                .andExpect(jsonPath("$[1].id").value("manghyang-busan"));
        mvc.perform(get("/api/rest-stops/next")
                        .param("lat", "37.013227").param("lng", "127.146014")
                        .param("direction", "SOUTHBOUND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].id").value("manghyang-busan"))
                .andExpect(jsonPath("$[0].congestion").value("MEDIUM"))
                .andExpect(jsonPath("$[0].brands[0]").value("던킨도너츠"))
                .andExpect(jsonPath("$[7].id").value("chupungnyeong-busan"))
                .andExpect(jsonPath("$[1].id").value("cheonan-hodu-busan"));
        mvc.perform(get("/api/rest-stops/next")
                        .param("lat", "36.198277").param("lng", "128.001919"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rejectsInvalidCoordinatesAndUnsupportedDirection() throws Exception {
        mvc.perform(get("/api/rest-stops/next")
                        .param("lat", "NaN").param("lng", "127.14"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/rest-stops/next")
                        .param("lat", "37.0181").param("lng", "127.143547")
                        .param("direction", "NORTHBOUND"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/rest-stops/next")
                        .param("lat", "37.5665").param("lng", "126.978"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnsOnlyFeaturedMenuAndNotFoundForUnknownStop() throws Exception {
        mvc.perform(get("/api/rest-stops/anseong-busan/featured-menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restStopId").value("anseong-busan"))
                .andExpect(jsonPath("$.menus").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.featuredMenu.name").value("안성국밥"))
                .andExpect(jsonPath("$.featuredMenu.imageUrl").value("/images/anseong-busan.jpg"));
        mvc.perform(get("/api/rest-stops/missing/featured-menus"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsFullMenuForKnownStopAndNotFoundForUnknownStop() throws Exception {
        mvc.perform(get("/api/rest-stops/anseong-busan/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restStopId").value("anseong-busan"))
                .andExpect(jsonPath("$.menus.length()").value(28))
                .andExpect(jsonPath("$.menus[*].imageUrl", everyItem(startsWith("/images/"))))
                .andExpect(jsonPath("$.menus[0].priceWon").value(10000))
                .andExpect(jsonPath("$.menus[0].name").value("안성국밥"))
                .andExpect(jsonPath("$.menus[1].name").value("한우소머리국밥"))
                .andExpect(jsonPath("$.menus[1].imageUrl").isString())
                .andExpect(jsonPath("$.menus[2].name").value("한우육개장"))
                .andExpect(jsonPath("$.menus[2].imageUrl").value("/images/menu/examples/yukgaejang.jpg"))
                .andExpect(jsonPath("$.menus[4].name").value("꼬시래기등심돈가스"))
                .andExpect(jsonPath("$.menus[4].imageUrl").value(
                        "/images/menu/examples/seaweed-pork-cutlet.jpg"));
        mvc.perform(get("/api/rest-stops/jugam-busan/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menus[1].name").value("보은대추왕갈비탕"))
                .andExpect(jsonPath("$.menus[1].imageUrl").value(
                        "/images/menu/jugam-busan-boeun-jumbo-rib-soup.jpg"));
        mvc.perform(get("/images/menu/jugam-busan-boeun-jumbo-rib-soup.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
        mvc.perform(get("/images/menu/examples/yukgaejang.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
        mvc.perform(get("/images/anseong-busan.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
        mvc.perform(get("/api/rest-stops/missing/menus"))
                .andExpect(status().isNotFound());
    }
}
