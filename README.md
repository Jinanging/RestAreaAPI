# RestAreaAPI

Pleos 공모전 화면 연동용 휴게소 조회 API. IntelliJ에서 이 폴더를 열고 `RestAreaApiApplication`을 실행하거나 `./gradlew bootRun`으로 시작한다. 기본 주소는 `http://localhost:8080`이다. Swagger UI는 `/swagger-ui.html`, OpenAPI JSON은 `/v3/api-docs`에서 열 수 있다.

| 요청 | 응답 |
| --- | --- |
| `GET /api/rest-stops` | 경부고속도로 부산 방향의 선정 휴게소 9곳을 통과 순서대로 반환 |
| `GET /api/rest-stops/next?lat=37.0181&lng=127.143547&direction=SOUTHBOUND` | 현재 위치 앞의 휴게소를 본선 기준 가까운 순서로 모두 반환 |
| `GET /api/rest-stops/anseong-busan/featured-menus` | 안성휴게소 부산 방향의 대표 메뉴와 사진 주소. 모르는 ID는 404 |
| `GET /api/rest-stops/anseong-busan/menus` | 안성휴게소 부산 방향의 공개 메뉴 목록, 가격, 사진 주소. 모르는 ID는 404 |

좌표와 시설은 [한국도로공사 휴게소정보](https://www.data.go.kr/data/15025446/fileData.do)의 2026-09-17 기준 데이터다. 대표 메뉴는 [경부선 휴게소 메뉴 목록](https://life.ttpk.co.kr/highway_rest/route/1)과 [추풍령 부산 방향 메뉴 정보](https://carcalc.manuz.com/rest/경부선/추풍령(부산))에서 각 한 개씩 선정했다. `routeOrder`는 이 목록 안에서의 통과 순서다.

`congestion`은 시연용 고정값이다. `LOW`는 여유, `MEDIUM`은 보통, `HIGH`는 혼잡이다. 앱에서는 시연용 혼잡도로 표시한다. 메뉴의 `description`은 `null`이다. 대표 메뉴의 `imageUrl`은 서버 기준 상대 경로다. 앱에서 API 기본 주소에 붙여 이미지를 불러온다. 사진은 음식 예시이며, 카드의 243 × 182dp 비율에 맞춘 729 × 546px JPEG다. 사진 전체를 유지하고 남는 부분은 연한 배경으로 채웠다. [이미지 출처와 이용 조건](src/main/resources/static/images/credits.txt)은 서버의 `/images/credits.txt`에서도 열 수 있다. `direction`의 `SOUTHBOUND`는 이 노선의 부산 방향을 뜻한다. 다음 휴게소 조회의 `direction` 기본값은 `SOUTHBOUND`이며 다른 방향은 400이다. 위도·경도는 필수다.

다음 휴게소는 차량 좌표를 안성 진입 전부터 추풍령 진입 후까지의 경부고속도로 부산 방향 본선에 투영하고, 아직 지나지 않은 진입 지점을 기준으로 정한다. 단일 하행 경로에서는 차량 앞 진입 지점의 순서가 남은 주행거리 순서와 같다. 화면의 경유지 남은 거리는 [Pleos NaviHelper](https://document.pleos.ai/en/docs/pleos-only/release-note/sdk-release-note)의 `WaypointInfo.remainDistance`를 사용할 수 있다. 경로와 150m 넘게 떨어졌거나 이후 휴게소가 없으면 빈 배열을 반환한다. 부산 방향 주행을 전제로 하며, 인접 도로나 반대편 차로의 주행 여부는 판별하지 않는다.

- 안성 진입 전 `37.0181,127.143547` → 안성부터 추풍령까지 9곳
- 안성 진입 후 `37.013227,127.146014` → 망향부터 추풍령까지 8곳

본선 경로 및 진입 지점은 [OpenStreetMap](https://www.openstreetmap.org/copyright) 기여자 데이터와 [FOSSGIS OSRM](https://routing.openstreetmap.de/) 경로를 바탕으로 추출했다. 조회 시 경로 파일은 서버 내부에서 읽는다.

`brands`는 카드에 표시할 입점 브랜드 이름 배열이다. 현재 값은 [휴게소별 공개 매장 목록](https://rest.mustarddata.com/rest-area/anseong-busan/)을 기준으로 저장한 자료다. [도로공사 0501 브랜드 매장 API](https://data.ex.co.kr/openapi/basicinfo/openApiInfoM?apiId=0501&pn=-1)를 연동할 때는 휴게소 코드 `stdRestCd`로 매칭하고 `brdName`을 이 배열에 넣는다.

이 서버는 공개 자료에서 선정한 데이터를 내장한다. [한국도로공사 Open API](https://data.ex.co.kr/openapi/intro/introduce02)로 자동 갱신하려면 발급받은 인증키가 필요하다.

`./gradlew test`로 조회 응답을 검증한다.


휴게소 목록과 대표 메뉴 조회는 카드용 대표 메뉴를 `featuredMenu` 객체로 반환한다. 상세 조회의 `menus`에는 공개 메뉴 목록과 원 단위 가격(`priceWon`)이 들어간다. 상세 메뉴는 2026-11-11에 조회한 [휴게소별 공개 메뉴표](https://rest.mustarddata.com/rest-area/)의 경부고속도로 부산 방향 9곳 자료를 서버에 저장한 값이다. 현재 메뉴와 가격은 현장에서 달라질 수 있다.

전체 메뉴 252개의 `imageUrl`은 모두 `/images/`로 시작하는 서버 기준 상대 경로다. 앱에서 API 기본 주소에 붙여 이미지를 불러온다. 이용 조건을 확인한 사진과 음식 종류별 데모 이미지를 서버에서 제공한다. 사진 출처와 이용 조건은 `/images/credits.txt`에서 확인할 수 있다. 이미지 표시 영역이 243 × 182dp인 경우 Android에서 비율을 유지해 맞추면 사진 전체를 표시할 수 있다.
