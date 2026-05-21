# Phase 2 요구사항: check_availability 개선 및 MeetingRoomDto 신규 추가

## 배경

Phase 1 실제 테스트 결과 아래 두 가지 문제가 확인되었다.

1. `check_availability` 응답에 회의실 이름이 전부 `null`로 반환됨
   - 원인: 백엔드 응답 필드(`mtgrmId`, `mtgrmNm`, ...)와 현재 `OfficeDto` 필드 불일치
2. "3층 회의실 조회" 같은 자연어 요청에 층/건물 필터가 없어 전체 회의실이 반환됨
   - 원인: `check_availability` 툴에 `offmId` 파라미터 없음

---

## 1. MeetingRoomDto 신규 생성

`selectMtgRmList.do` 실제 응답 필드에 맞는 전용 DTO를 생성한다.

**파일 위치**: `src/main/java/com/psnm/mcp/meetingroom/client/dto/MeetingRoomDto.java`

```java
public class MeetingRoomDto {
    private String mtgrmId;      // 회의실 ID
    private String mtgrmNm;      // 회의실 이름 (예: "1회의실", "프로젝트룸(화상)")
    private Integer seatQty;     // 좌석 수
    private String offmId;       // 소속 사무실(건물/층) ID
    private String videoMtgrmAt; // 화상회의 가능 여부 ("Y"/"N")
    private String mtgrmSeCd;    // 회의실 구분코드 (MTG001: 일반, MTG002: 대형/특수)
    private Integer alignNo;     // 정렬 순서
    private String useAt;        // 사용 여부 ("Y"/"N")
    private String fxtrsInfo;    // 비품 정보
    private String atchFileId;   // 첨부파일 ID (이미지)
    // getter/setter 또는 @Data (Lombok)
}
```

---

## 2. check_availability 툴 파라미터 변경

### 변경 전
| 파라미터 | 설명 |
|---------|------|
| startDate | 조회 날짜 (yyyy-MM-dd) |
| startTime | 시작 시간 (HH:mm) |
| endTime   | 종료 시간 (HH:mm) |

### 변경 후
| 파라미터 | 필수 | 설명 |
|---------|------|------|
| offmId    | 필수 | 사무실(건물/층) ID. list_offices로 먼저 조회 후 전달 |
| startTime | 필수 | 시작 시간 (HH:mm) |
| endTime   | 필수 | 종료 시간 (HH:mm) |

**startDate 제거 이유**: 백엔드 `selectMtgRmList.do`는 `dt` 파라미터가 없으면 당일 날짜를 자동 적용한다. 현재 서비스 범위(당일 예약)에서는 날짜 파라미터가 불필요하다.

**offmId 추가 이유**: 사용자는 보통 "3층", "본사 회의실" 처럼 특정 공간을 지정해 조회한다. offmId 없이 조회하면 전체 45건이 반환되어 사용자가 원하는 정보를 찾기 어렵다.

### MeetingRoomTools.java 수정

```java
@McpTool(
    name = "check_availability",
    description = "특정 사무실(건물/층)에서 예약 가능한 회의실 목록을 조회합니다. " +
                  "'3층 회의실 비어있어?', '본사 회의실 예약 가능한 곳 알려줘' 같은 요청에 사용합니다. " +
                  "offmId는 list_offices 도구로 먼저 조회해서 전달해야 합니다."
)
public List<MeetingRoomDto> findAvailableRooms(
    @McpToolParam(description = "사무실 ID (list_offices 결과의 offmId)")
    @NotBlank String offmId,

    @McpToolParam(description = "시작 시간 (HH:mm 형식, 예: 13:00)")
    @NotBlank String startTime,

    @McpToolParam(description = "종료 시간 (HH:mm 형식, 예: 14:00)")
    @NotBlank String endTime
)
```

---

## 3. BackendApiClient.findAvailableRooms 변경

### 변경 전 파라미터
```
dt, bgnTime, endTime, whereType=available
```

### 변경 후 파라미터
```
offmId, bgnTime, endTime, whereType=available
(dt 제거 → 백엔드 자동 today 적용)
```

### 변경 후 반환 타입
```java
// 변경 전
public ListVO<OfficeDto> findAvailableRooms(String startDate, String startTime, String endTime)

// 변경 후
public ListVO<MeetingRoomDto> findAvailableRooms(String offmId, String startTime, String endTime)
```

### 구현 예시
```java
public ListVO<MeetingRoomDto> findAvailableRooms(String offmId, String startTime, String endTime) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("offmId", offmId);
    form.add("bgnTime", startTime);
    form.add("endTime", endTime);
    form.add("whereType", "available");

    return restClient.post()
            .uri("/com/smartofc/mtgTablet/selectMtgRmList.do")
            .header("Referer", referer)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(new ParameterizedTypeReference<ListVO<MeetingRoomDto>>() {});
}
```

---

## 4. OfficeDto 개선 (list_offices offmNm null 수정)

### 문제 상황

현재 `list_offices` 호출 시 `offmNm`이 null로 반환된다. Claude가 "3층", "본사" 같은 자연어를 `offmId`로 매핑하려면 사무실 이름/층 정보가 반드시 필요하다.

- **원인 추정**: `selectOffmList.do` 실제 응답 필드명이 `offmNm`이 아닌 다른 이름일 가능성
  - 예: `officeNm`, `officeName`, `nm` 등
- **확인 방법**: 백엔드에 직접 POST 요청 후 응답 JSON의 실제 키 이름 확인
  ```
  POST /com/smartofc/mtgTablet/selectOffmList.do
  Content-Type: application/x-www-form-urlencoded
  Referer: http://<backend>/com/smartofc/mtgTablet_list.do
  ```

---

### 4-1. OfficeDto 필드 추가

`selectOffmList.do` 응답에 포함될 것으로 예상되는 필드를 추가한다. 실제 응답 확인 후 불필요한 필드는 제거하고 `@JsonProperty`로 이름 불일치를 해소한다.

**파일 위치**: `src/main/java/com/psnm/mcp/meetingroom/client/dto/OfficeDto.java`

```java
public class OfficeDto {
    private String offmId;          // 사무실 ID (PK)
    private String offmNm;          // 사무실 이름 ← 실제 응답 필드명 확인 후 @JsonProperty 추가

    // 층/건물 정보 (Claude 자연어 매핑에 필수)
    private String flor;            // 층 번호 (예: "3", "B1")
    private String florNm;          // 층 이름 (예: "3층", "지하1층")
    private String buldNm;          // 건물명 (예: "본사", "A동")
    private String lcCd;            // 위치 코드
    private String lcNm;            // 위치 이름

    // 부가 정보
    private String useAt;           // 사용 여부 ("Y"/"N")
    private String atchFileId;      // 첨부파일 ID (이미지)

    // getter/setter 또는 @Data (Lombok)
}
```

> **주의**: 실제 응답 JSON을 확인하기 전까지는 위 필드명이 가정값이다.
> 응답 키가 다를 경우 반드시 `@JsonProperty("실제키명")`을 추가해야 한다.
>
> 예시:
> ```java
> @JsonProperty("officeName")   // 실제 응답 키가 officeName인 경우
> private String offmNm;
>
> @JsonProperty("floor")        // 실제 응답 키가 floor인 경우
> private String flor;
> ```

---

### 4-2. `@McpTool` description 보강

Claude가 자연어 요청("3층 회의실", "본사 A동")을 `offmId`로 자동 매핑하려면 `list_offices` 툴 설명에 반환 필드 안내가 있어야 한다.

**MeetingRoomTools.java 수정**

```java
@McpTool(
    name = "list_offices",
    description = "전체 사무실(건물/층) 목록을 조회합니다. " +
                  "각 항목에는 offmId(사무실 ID), offmNm(사무실 이름), florNm(층 이름), buldNm(건물명)이 포함됩니다. " +
                  "'3층', '본사', 'B1층' 같은 자연어 요청이 들어오면 이 도구를 먼저 호출해서 " +
                  "해당하는 offmId를 찾은 후 check_availability에 전달해야 합니다."
)
public List<OfficeDto> listOffices()
```

---

### 4-3. 검증 방법

`list_offices` 호출 결과에서 아래 항목을 확인한다.

| 확인 항목 | 기대값 | 실패 시 조치 |
|---------|--------|-----------|
| `offmNm` | "3층 사무실", "본관 2층" 등 비어있지 않은 문자열 | 실제 응답 필드명 확인 후 `@JsonProperty` 추가 |
| `florNm` 또는 `flor` | "3층", "B1" 등 층 정보 | 응답에 없으면 `offmNm`에서 층 정보 포함 여부 확인 |
| `buldNm` | "본사", "A동" 등 건물명 | 응답에 없으면 제거 |

Claude가 "3층 회의실 조회해줘" 요청에 대해 `list_offices` 결과에서 `offmNm` 또는 `florNm`이 "3층"을 포함하는 항목을 찾아 `offmId`를 추출할 수 있어야 한다.

---

## 5. Claude 응답 흐름 (다단계 조회)

"3층에서 1시~2시 예약 가능한 회의실 알려줘" 같은 요청이 들어오면 Claude는 아래 순서로 도구를 호출해야 한다.

```
Step 1: list_offices 호출
  → "3층"에 해당하는 offmId를 찾는다
  → 예: offmId = "67237a228d254b0bbe31b1adf17961e0" (offmNm이 3층인 항목)

Step 2: check_availability 호출
  → offmId = "67237a228d254b0bbe31b1adf17961e0"
  → startTime = "13:00"
  → endTime = "14:00"

Step 3: 결과를 사용자에게 응답
  → 예: "3층에서 13:00~14:00 사이 예약 가능한 회의실은 다음과 같습니다.
         - 1회의실 (8석)
         - 4회의실 (6석)
         - 프로젝트룸 화상 (12석, 화상회의 가능)"
```

이 흐름이 자연스럽게 동작하려면 `list_offices`의 `@McpTool` description에 층/건물 정보를 담은 사무실 목록을 반환한다는 설명이 명확해야 하며, `check_availability`의 description에 "offmId는 list_offices로 먼저 조회해서 전달"이라는 안내가 포함되어야 한다.

---

## 6. 응답 포맷 개선 권고

현재 Claude가 사용자에게 줄 수 있는 회의실 정보:

| 필드 | 사용자 노출 여부 | 비고 |
|------|---------------|------|
| mtgrmNm | ✅ 필수 | 회의실 이름 |
| seatQty | ✅ 권장 | "6석", "12석" |
| videoMtgrmAt | ✅ 권장 | "화상회의 가능" 여부 |
| mtgrmId | ⚠️ 내부용 | 예약 시 필요, 사용자에게 직접 노출 불필요 |
| rm | ❌ 미노출 | Base64 인코딩된 HTML, 파싱 불필요 |
| alignNo / frstRegistPnttm 등 | ❌ 미노출 | 내부 정렬/감사용 |

---

## 7. 검증 시나리오

| 입력 | 예상 흐름 |
|------|---------|
| "3층에서 오후 1시~2시 예약 가능한 회의실" | list_offices → 3층 offmId 획득 → check_availability → 결과 목록 |
| "지금 당장 쓸 수 있는 회의실" | list_offices(선택) → check_availability(현재 시간 기준) |
| "화상 회의 가능한 방 있어?" | check_availability → videoMtgrmAt=Y인 항목만 필터 |
| "10명 이상 들어가는 회의실" | check_availability → seatQty >= 10 필터 |
