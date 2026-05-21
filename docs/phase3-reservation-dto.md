# Phase 3 요구사항: ReservationDto 신규 추가 및 list_reservations 개선

## 배경

현재 `list_reservations` 도구(`selectResveList.do`)의 응답을 `OfficeDto`로 받고 있어
실제 응답 필드가 전혀 매핑되지 않는다.

**실제 응답 예시**
```json
{
  "total": "1",
  "rows": [
    {
      "resveId": "d320112688bc4cab97f8eb84a057fab3",
      "mtgSj": "그룹웨어 미팅",
      "dt": "2026-05-21",
      "bgnTime": "14:00",
      "endTime": "15:00",
      "secretAt": "N",
      "rsvctmId": "BP231205",
      "atdrnQty": 1,
      "mtgCtt": null,
      "mtgrmId": "62e5cec08d3d11f0a07e74d02b7ae0ec",
      "confmAt": "Y",
      "resveSttusCd": "STT001",
      "psnetEventId": null,
      "rsvctmNm": "송재완",
      "userInfo": "송재완(BP231205) / 현장대리인(그룹웨어)",
      "videoMtgrmAt": "Y",
      "mtgrmNm": "2회의실(화상)",
      "offmId": "67237a228d254b0bbe31b1adf17961e0",
      "offmNm": "마포티타운 3F",
      "colorCd": "MTR003"
    }
  ]
}
```

**핵심 문제**
1. `OfficeDto`에는 `resveId`, `mtgSj`, `dt`, `bgnTime`, `endTime` 등 예약 필드가 없어 모두 null 반환
2. 예약 취소 시 `resveId`와 `psnetEventId`가 필수인데, list_reservations 결과에서 이를 얻을 수 없음
   → 사용자가 "내 예약 취소해줘"라고 할 때 Claude가 `resveId`를 전달할 수 없음

---

## 1. ReservationDto 신규 생성

**파일 위치**: `src/main/java/com/psnm/mcp/meetingroom/client/dto/ReservationDto.java`

```java
public class ReservationDto {

    // ── 예약 식별 (취소 시 필수) ──────────────────────────
    private String resveId;         // 예약 ID (취소 API의 resveId)
    private String psnetEventId;    // PSNET 이벤트 ID (취소 API의 psnetEventId, null 가능)

    // ── 회의 기본 정보 ─────────────────────────────────────
    private String mtgSj;           // 회의 제목 (예: "그룹웨어 미팅")
    private String dt;              // 예약 날짜 (yyyy-MM-dd)
    private String bgnTime;         // 시작 시간 (HH:mm)
    private String endTime;         // 종료 시간 (HH:mm)
    private String mtgCtt;          // 회의 내용 (null 가능)
    private Integer atdrnQty;       // 참석 인원 수
    private String secretAt;        // 비밀 여부 ("Y"/"N")

    // ── 예약 상태 ──────────────────────────────────────────
    private String resveSttusCd;    // 예약 상태 코드 (STT001: 예약완료, STT002: 취소 등)
    private String confmAt;         // 확인 여부 ("Y"/"N")

    // ── 회의실 정보 ────────────────────────────────────────
    private String mtgrmId;         // 회의실 ID
    private String mtgrmNm;         // 회의실 이름 (예: "2회의실(화상)")
    private String videoMtgrmAt;    // 화상회의 가능 여부 ("Y"/"N")
    private String mtgrmSeCd;       // 회의실 구분코드 (MTG001: 일반, MTG002: 대형)

    // ── 사무실(층/건물) 정보 ───────────────────────────────
    private String offmId;          // 사무실 ID
    private String offmNm;          // 사무실 이름 (예: "마포티타운 3F")

    // ── 예약자 정보 ────────────────────────────────────────
    private String rsvctmId;        // 예약자 사번
    private String rsvctmNm;        // 예약자 이름 (예: "송재완")
    private String userInfo;        // 예약자 요약 (예: "송재완(BP231205) / 현장대리인(그룹웨어)")

    // ── 부가 정보 ──────────────────────────────────────────
    private String colorCd;         // 캘린더 색상 코드 (예: "MTR003")

    // getter/setter 또는 @Data (Lombok)
}
```

> **설계 기준**
> - `resveId`, `psnetEventId`: 취소 API 호출에 직접 사용되므로 반드시 포함
> - `resveSttusCd`: 이미 취소된 예약을 사용자에게 구분해 보여주기 위해 포함
> - `frstRegistPnttm`, `lastUpdtPnttm` 등 감사(audit) 전용 long 타입 타임스탬프 필드는 제외
>   (사용자 노출 불필요, 직렬화 비용 절감)
> - `reptitCycle`, `reptitUnitCd`, `day`, `endSeCd`, `endDt`, `endCnt` 등 반복예약 제어 필드는
>   현재 서비스 범위(단건 예약)에서 불필요하므로 제외

---

## 2. BackendApiClient 변경

### 변경 전
```java
public ListVO<OfficeDto> getMyReservations() { ... }
```

### 변경 후
```java
public ListVO<ReservationDto> getMyReservations() {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("rsvctmId", UserContext.getEmpNo());

    return restClient.post()
            .uri("/com/smartofc/mtgTabletResve/selectResveList.do")
            .header("Referer", referer)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(new ParameterizedTypeReference<ListVO<ReservationDto>>() {});
}
```

---

## 3. MeetingRoomTools 변경

### 반환 타입 변경
```java
// 변경 전
public List<OfficeDto> getMyReservations()

// 변경 후
public List<ReservationDto> getMyReservations()
```

### description 보강
```java
@McpTool(
    name = "list_reservations",
    description = "내가 예약한 회의실 목록을 조회합니다. " +
                  "'내 예약 보여줘', '오늘 예약 있어?' 같은 요청에 사용합니다. " +
                  "각 항목에는 resveId(예약 ID), mtgSj(회의 제목), dt(날짜), " +
                  "bgnTime~endTime(시간), mtgrmNm(회의실 이름), offmNm(층/건물), " +
                  "psnetEventId(PSNET 이벤트 ID)가 포함됩니다. " +
                  "예약 취소 요청이 들어오면 이 도구를 먼저 호출하여 " +
                  "resveId와 psnetEventId를 확인한 후 cancel_reservation에 전달해야 합니다."
)
public List<ReservationDto> getMyReservations()
```

---

## 4. 취소 흐름 개선 (다단계 조회)

`cancel_reservation` 호출 전 `list_reservations`로 `resveId`를 확인하는 흐름을 Claude가 자연스럽게 실행할 수 있어야 한다.

```
Step 1: list_reservations 호출
  → 예약 목록 조회
  → 사용자 요청("오늘 3층 미팅 취소해줘")에 맞는 항목 식별
  → resveId = "d320112688bc4cab97f8eb84a057fab3"
  → psnetEventId = null

Step 2: cancel_reservation 호출
  → resveId = "d320112688bc4cab97f8eb84a057fab3"
  → psnetEventId = null (null이면 빈 문자열 또는 생략)

Step 3: 결과 응답
  → "그룹웨어 미팅 (14:00~15:00, 2회의실(화상)) 예약이 취소되었습니다."
```

`cancel_reservation` 툴 description에도 안내 추가:
```java
@McpTool(
    name = "cancel_reservation",
    description = "회의실 예약을 취소합니다. " +
                  "resveId는 list_reservations 결과의 resveId 필드를 사용합니다. " +
                  "psnetEventId가 null인 경우 빈 문자열로 전달합니다."
)
```

---

## 5. Claude 노출 정보 기준

| 필드 | 사용자 노출 | 비고 |
|------|-----------|------|
| mtgSj | ✅ 필수 | 회의 제목 |
| dt / bgnTime / endTime | ✅ 필수 | 날짜·시간 |
| mtgrmNm | ✅ 필수 | 회의실 이름 |
| offmNm | ✅ 권장 | 층/건물명 |
| atdrnQty | ✅ 권장 | 참석 인원 |
| videoMtgrmAt | ✅ 권장 | 화상회의 가능 여부 |
| resveSttusCd | ✅ 권장 | 예약 상태 (취소된 건 구분) |
| resveId | ⚠️ 내부용 | 취소 시 필요, 사용자에게 직접 노출 불필요 |
| psnetEventId | ⚠️ 내부용 | 취소 시 필요, null 가능 |
| userInfo / rsvctmNm | ⚠️ 상황별 | 대리 예약 확인 등에 참고 |
| colorCd / mtgrmAtchFileId 등 | ❌ 미노출 | UI 전용 데이터 |

---

## 6. 테스트 코드 업데이트

`MeetingRoomToolsTest.java`에서 `getMyReservations` 관련 테스트를 `ReservationDto` 기반으로 교체:

```java
@Test
void getMyReservations_Successful() {
    // Given
    ReservationDto reservation = new ReservationDto();
    reservation.setResveId("d320112688bc4cab97f8eb84a057fab3");
    reservation.setMtgSj("그룹웨어 미팅");
    reservation.setDt("2026-05-21");
    reservation.setBgnTime("14:00");
    reservation.setEndTime("15:00");
    reservation.setMtgrmNm("2회의실(화상)");

    ListVO<ReservationDto> mockResponse = new ListVO<>();
    mockResponse.setRows(List.of(reservation));
    when(backendApiClient.getMyReservations()).thenReturn(mockResponse);

    // When
    List<ReservationDto> result = meetingRoomTools.getMyReservations();

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("d320112688bc4cab97f8eb84a057fab3", result.get(0).getResveId());
    assertEquals("그룹웨어 미팅", result.get(0).getMtgSj());
}
```

---

## 7. 검증 시나리오

| 입력 | 예상 흐름 |
|------|---------|
| "내 예약 목록 보여줘" | list_reservations → 날짜·시간·회의실명 목록 출력 |
| "오늘 예약 있어?" | list_reservations → dt = 오늘 날짜인 항목 필터 후 응답 |
| "그룹웨어 미팅 취소해줘" | list_reservations → mtgSj 매칭 → resveId 확인 → cancel_reservation |
| "내 예약 전부 취소해줘" | list_reservations → 전체 resveId 순회 → cancel_reservation 반복 호출 |
