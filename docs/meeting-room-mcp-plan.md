# 회의실 예약 MCP 서버 구축 계획서

## 문서 정보

- **작성일**: 2026-05-19
- **프로젝트명**: `meeting-room-mcp` (가칭)
- **목적**: 기존 사내 회의실 예약시스템(`espora-min-ps-ofc`)을 wrapping하는 MCP 서버 구축. Claude Desktop 사용자가 자연어로 회의실을 조회/예약/취소할 수 있도록 한다.
- **대상 독자**: 구현 담당 coder AI agent
- **참조 프로젝트**: `E:\_JavaDev\workspace\espora-min-ps-ofc` (기존 예약시스템 소스)

---

## 1. 프로젝트 개요

### 1.1 배경

작년(2025년)에 사내 회의실/좌석 예약시스템 `espora-min-ps-ofc`를 구축 완료한 상태다. 회의실 앞에 설치된 태블릿을 통해 예약 조회 및 등록이 가능하다. 올해는 이 시스템에 자연어 인터페이스를 추가하여, Claude Desktop을 사용하는 사내 구성원 누구나 자연어로 회의실 예약을 처리할 수 있도록 한다.

### 1.2 접근 방식

기존 예약시스템에는 태블릿용으로 만들어진 referer 기반 API endpoint 군이 존재한다 (`MtgrmTabletController`). 이 endpoint들은 별도 인증 없이 사번을 파라미터로 받아 예약 처리한다. 본 MCP 서버는 이 태블릿 API를 그대로 호출하는 wrapper로 동작한다.

### 1.3 범위

**포함**:
- 사무실(건물/층) 목록 조회
- 회의실 목록 조회 (특정 사무실)
- 회의실 가용성 확인 (특정 날짜/시간대 빈 회의실)
- 내 예약 목록 조회
- 회의실 예약 등록
- 회의실 예약 취소
- 참석자 검색 (직원 검색)

**제외**:
- 좌석 예약 기능 (기존 시스템에 있지만 본 프로젝트 범위 외)
- 입실/퇴실 처리 (물리적 위치 기반 동작이라 원격 호출 부적합)
- 예약 수정 (Phase 2 검토. 취소 후 재등록으로 대체 가능)
- 반복 예약 (Phase 2 검토. MVP는 단발성 예약만)
- 첨부파일 등록 (Phase 2 검토)
- 사용자 인증 (사내망 신뢰 환경 가정)

---

## 2. 의사결정 사항

| 항목 | 결정 사항 | 근거 |
|---|---|---|
| 클라이언트 | Claude Desktop | 사내 구성원 모두가 사용. GUI 친화적 |
| Transport | Streamable HTTP | SSE는 deprecated, Streamable HTTP가 현재 표준 |
| 프레임워크 | Spring Boot 3.x + Spring AI 1.1.6 | 기존 Spring 5.3.6 시스템과 별도 신규 프로젝트 |
| Java 버전 | 17 이상 | Spring Boot 3.x 요구사항 |
| MCP 도구 정의 방식 | 어노테이션 기반 (`@McpTool`) | Spring AI 1.1+ 권장 방식 |
| 사번 전달 방식 | HTTP 헤더 `x-emp-no` | Claude Desktop connector 설정에 1회 입력 |
| 사용자 인증 | 없음 | 사내망 한정 + 사용자 편의성 우선. 보안 조치는 별도 트랙 |
| 자연어 언어 | 한국어 | 도구 description은 한국어 위주, 식별자는 영어 |
| 개발 환경 배포 | 개발자 PC 가상화 환경 + 포트포워딩 | 1차 테스트용 |

---

## 3. 기술 스택

### 3.1 의존성 (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version>
        <relativePath/>
    </parent>

    <groupId>com.psnm.mcp</groupId>
    <artifactId>meeting-room-mcp</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.1.6</spring-ai.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Spring Web (Servlet 기반) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring AI MCP Server - Streamable HTTP -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
        </dependency>

        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Lombok (선택) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- 테스트 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3.2 application.yml

```yaml
spring:
  application:
    name: meeting-room-mcp
  ai:
    mcp:
      server:
        name: meeting-room
        version: 1.0.0
        type: SYNC
        protocol: STREAMABLE
        instructions: |
          이 도구들은 사내 회의실 예약 시스템에 접근하기 위한 도구입니다.
          회의실 조회, 가용성 확인, 예약 등록, 예약 취소 등의 기능을 제공합니다.
          회의실 이름은 보통 "18층 3회의실" 같은 형식으로 표현됩니다.
          예약 등록과 취소는 destructive operation이므로 실행 전 사용자에게 확인을 받으세요.
        streamable-http:
          mcp-endpoint: /mcp
          keep-alive-interval: 30s

# 기존 espora-min-ps-ofc 백엔드 연동 설정
backend:
  api:
    base-url: ${BACKEND_BASE_URL:http://localhost:8081}
    # 기존 시스템의 referer 검증을 통과시키기 위한 헤더 값
    # MtgrmTabletController.isValidReferer()는 ".*/com/smartofc/mtgTablet.*\\.do" 패턴을 검사
    referer: ${BACKEND_REFERER:http://localhost:8081/com/smartofc/mtgTablet_list.do}
    connect-timeout: 5s
    read-timeout: 15s

server:
  port: 8080

logging:
  level:
    root: INFO
    com.psnm.mcp.meetingroom: DEBUG
    org.springframework.ai.mcp: DEBUG
```

---

## 4. 아키텍처

### 4.1 데이터 흐름

```
[Claude Desktop]
    │ HTTP POST /mcp (Streamable HTTP)
    │ Headers:
    │   x-emp-no: 12345  ← 사번
    ▼
[meeting-room-mcp (이 프로젝트)]
    │ 1. HandlerInterceptor가 x-emp-no 추출 → ThreadLocal 저장
    │ 2. Spring AI MCP starter가 도구 디스패치
    │ 3. @McpTool 메서드 실행
    │ 4. BackendApiClient가 기존 시스템 호출
    │    (Referer 헤더 자동 첨부, 사번을 파라미터로 주입)
    ▼
[espora-min-ps-ofc (기존 시스템)]
    │ MtgrmTabletController (referer 검증)
    │   → MtgrmResveController (실제 비즈니스 로직)
    ▼
[MariaDB]
```

### 4.2 핵심 컴포넌트

| 컴포넌트 | 역할 |
|---|---|
| `EmpNoHeaderInterceptor` | HTTP 요청에서 `x-emp-no` 헤더 추출 → `UserContext`에 저장 |
| `UserContext` | ThreadLocal 기반 사번 보관소 |
| `MeetingRoomTools` | `@McpTool` 메서드 모음 (자연어로 노출되는 도구들) |
| `BackendApiClient` | 기존 시스템 호출용 `RestClient` 래퍼. 모든 요청에 referer 헤더 첨부 |
| `BackendApiConfig` | `RestClient` Bean 설정 |
| DTO 클래스 | 백엔드 request/response 매핑 |

### 4.3 사번 전달 패턴 (옵션 B 구현)

Claude Desktop의 connector 설정 (사용자가 1회 설정):

```json
{
  "mcpServers": {
    "meeting-room": {
      "type": "http",
      "url": "http://[서버 주소]:8080/mcp",
      "headers": {
        "x-emp-no": "12345"
      }
    }
  }
}
```

이렇게 설정해두면 Claude Desktop이 매 호출마다 자동으로 헤더를 첨부한다. MCP 서버는 매 요청마다 헤더에서 사번을 꺼내어 백엔드 호출 시 사용한다.

**구현 패턴**:

```java
// 1. Interceptor에서 헤더 추출
@Component
public class EmpNoHeaderInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        String empNo = req.getHeader("x-emp-no");
        if (empNo != null) {
            UserContext.setEmpNo(empNo);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        UserContext.clear();
    }
}

// 2. ThreadLocal 보관소
public class UserContext {
    private static final ThreadLocal<String> EMP_NO = new ThreadLocal<>();
    public static void setEmpNo(String empNo) { EMP_NO.set(empNo); }
    public static String getEmpNo() { return EMP_NO.get(); }
    public static void clear() { EMP_NO.remove(); }
}

// 3. Tool 메서드에서 사용
@McpTool(description = "...")
public List<Reservation> listMyReservations(...) {
    String empNo = UserContext.getEmpNo();
    if (empNo == null) {
        throw new IllegalStateException("사번이 설정되지 않았습니다. Claude Desktop의 connector 설정을 확인하세요.");
    }
    return backendClient.findReservationsByEmpNo(empNo);
}
```

---

## 5. 도구(Tool) 명세

각 도구는 `@McpTool` 어노테이션으로 정의한다. `description`은 Claude가 어떤 도구를 호출할지 판단하는 핵심 정보이므로 자연어 요청을 다양한 표현으로 받아도 매칭되도록 충분히 상세히 기술해야 한다.

### 5.1 도구 일람

| 도구 이름 | 역할 | 매핑 백엔드 endpoint | 종류 |
|---|---|---|---|
| `list_offices` | 사무실 목록 조회 | `/com/smartofc/mtgTablet/selectOffmList.do` | read |
| `list_meeting_rooms` | 회의실 목록 조회 | `/com/smartofc/mtgTablet/selectMtgRmList.do` | read |
| `check_availability` | 빈 회의실 조회 | `/com/smartofc/mtgTablet/selectMtgRmList.do` (whereType=available) | read |
| `list_reservations` | 예약 목록 조회 | `/com/smartofc/mtgTabletResve/selectResveList.do` | read |
| `search_employees` | 직원 검색 | `/com/smartofc/mtgTabletResve/selectEmplyrList.do` | read |
| `create_reservation` | 예약 등록 | `/com/smartofc/mtgTabletResve/insert.do` | **write (확인 필수)** |
| `cancel_reservation` | 예약 취소 | `/com/smartofc/mtgTabletResve/updateSttus.do` (sttDiv=resveCancel) | **write (확인 필수)** |

### 5.2 도구별 어노테이션 예시

#### `list_offices`

```java
@McpTool(
    name = "list_offices",
    description = """
        사내 사무실(건물/층) 목록을 조회합니다.
        회의실은 특정 사무실에 속하므로, 회의실을 조회하기 전에 사무실 목록을 먼저 확인할 수 있습니다.
        사용자가 "어디에 사무실이 있나" "지점이 어디 있나" 같은 질문을 할 때 사용하세요.
        """
)
public List<OfficeDto> listOffices();
```

#### `list_meeting_rooms`

```java
@McpTool(
    name = "list_meeting_rooms",
    description = """
        회의실 목록을 조회합니다.
        특정 사무실(offmId)에 속한 회의실만 필터링할 수도 있고, 전체 회의실을 조회할 수도 있습니다.
        회의실 이름은 "18층 3회의실" 같은 형식이 일반적입니다.
        각 회의실의 수용 인원(seatQty), 위치, 보유 장비(fxtrsInfo) 등의 메타데이터를 포함합니다.
        사용자가 "어떤 회의실이 있어" "큰 회의실 알려줘" 같은 질문을 할 때 사용하세요.
        """
)
public List<MeetingRoomDto> listMeetingRooms(
    @McpToolParam(description = "사무실 ID. 생략 시 전체 회의실 조회", required = false) String offmId
);
```

#### `check_availability`

```java
@McpTool(
    name = "check_availability",
    description = """
        지정한 날짜와 시간대에 예약 가능한 회의실 목록을 반환합니다.
        선택적으로 최소 수용 인원, 사무실로 필터링할 수 있습니다.
        사용자가 "내일 오후 2시에 5명 들어갈 회의실 있어?" "다음 주 월요일 10시부터 11시까지 빈 회의실" 같은 자연어 질문을 할 때 사용하세요.
        시간 형식은 24시간제 HH:mm, 날짜 형식은 yyyy-MM-dd 입니다.
        """
)
public List<MeetingRoomDto> checkAvailability(
    @McpToolParam(description = "조회 날짜 (yyyy-MM-dd)", required = true) String dt,
    @McpToolParam(description = "시작 시간 (HH:mm, 24시간제)", required = true) String bgnTime,
    @McpToolParam(description = "종료 시간 (HH:mm, 24시간제)", required = true) String endTime,
    @McpToolParam(description = "최소 수용 인원", required = false) Integer minCapacity,
    @McpToolParam(description = "사무실 ID (특정 사무실 회의실만 조회)", required = false) String offmId
);
```

#### `list_reservations`

```java
@McpTool(
    name = "list_reservations",
    description = """
        예약 목록을 조회합니다.
        파라미터로 조회 범위를 좁힐 수 있습니다 (날짜, 회의실, 본인 예약만 등).
        scope를 "mine"으로 지정하면 본인 사번으로 등록된 예약만 조회합니다.
        scope를 "all"로 지정하면 해당 날짜의 모든 예약을 조회합니다.
        사용자가 "내 예약 보여줘" "오늘 회의실 예약 현황" 같은 질문을 할 때 사용하세요.
        """
)
public List<ReservationDto> listReservations(
    @McpToolParam(description = "조회 날짜 (yyyy-MM-dd). 생략 시 오늘", required = false) String dt,
    @McpToolParam(description = "조회 범위: 'mine' (본인 예약만) 또는 'all' (전체)", required = false) String scope,
    @McpToolParam(description = "회의실 ID로 필터링", required = false) String mtgrmId
);
```

#### `search_employees`

```java
@McpTool(
    name = "search_employees",
    description = """
        참석자로 추가할 직원을 검색합니다.
        이름, 부서명, 사번 등 검색어로 직원을 찾을 수 있습니다.
        예약 등록 시 참석자 목록에 추가하기 위해 사용하세요.
        반환된 emplNo 값을 create_reservation의 attendees 파라미터에 사용합니다.
        """
)
public List<EmployeeDto> searchEmployees(
    @McpToolParam(description = "검색어 (이름, 부서명, 사번)", required = true) String keyword
);
```

#### `create_reservation`

```java
@McpTool(
    name = "create_reservation",
    description = """
        회의실을 예약 등록합니다.

        ⚠️ 이 도구는 destructive operation 입니다. 호출 전 반드시 사용자에게 다음 사항을 확인하세요:
        - 예약 일시, 회의실, 참석자가 맞는지 확인
        - 이 예약이 등록되면 참석자 전원에게 메신저 알림이 발송되며, 사내 그룹웨어 일정에 자동 등록됩니다
        - 사용자의 명시적 동의 ("등록해줘", "예약해줘" 등) 후에만 호출하세요
        """
)
public CreateReservationResult createReservation(
    @McpToolParam(description = "회의실 ID", required = true) String mtgrmId,
    @McpToolParam(description = "예약 날짜 (yyyy-MM-dd)", required = true) String dt,
    @McpToolParam(description = "시작 시간 (HH:mm)", required = true) String bgnTime,
    @McpToolParam(description = "종료 시간 (HH:mm)", required = true) String endTime,
    @McpToolParam(description = "회의 제목", required = true) String mtgSj,
    @McpToolParam(description = "회의 내용 (선택)", required = false) String mtgCtt,
    @McpToolParam(description = "참석자 사번 목록", required = false) List<String> attendees
);
```

#### `cancel_reservation`

```java
@McpTool(
    name = "cancel_reservation",
    description = """
        기존 회의실 예약을 취소합니다.

        ⚠️ 이 도구는 destructive operation 입니다. 호출 전 반드시 사용자에게 확인하세요:
        - 어떤 예약을 취소하는지 확인 (날짜, 시간, 회의실, 제목)
        - 취소 시 참석자에게 메신저 알림이 발송되며, 사내 그룹웨어 일정도 함께 취소됩니다
        - 본인이 예약한 건만 취소 가능합니다
        """
)
public CancelReservationResult cancelReservation(
    @McpToolParam(description = "예약 ID (list_reservations에서 획득)", required = true) String resveId
);
```

---

## 6. 백엔드 API 연동 상세 명세

이 섹션은 코드 분석 결과 (컨트롤러, mapper XML, 태블릿 JS) 기반의 정확한 API 명세이다.

### 6.1 공통 사항

- **base URL**: 기존 시스템 호스트 (application.yml의 `backend.api.base-url`)
- **referer 헤더**: 모든 요청에 `backend.api.referer` 값 첨부 (`MtgrmTabletController.isValidReferer()` 통과용. 정규식 `.*/com/smartofc/mtgTablet.*\.do(?:\?.*)?$`)
- **메서드**: 백엔드는 GET/POST 모두 허용 (`method = {RequestMethod.GET, RequestMethod.POST}`). POST + form-urlencoded 권장
- **인코딩**: UTF-8. 한국어 회의 제목 등 한글 파라미터 처리 주의
- **응답 포맷**: JSON
  - 목록 조회: `ListVO` 구조 — `{ "rows": [...], "total": N, "status": "...", "message": "..." }`
  - 단건 작업: `ResultVO` 구조 — `{ "data": ..., "total": N, "status": "...", "message": "..." }`
- **상태 코드**: `status` 값이 `"000"` 또는 미설정이면 성공. 그 외 (`"920"`, `"921"`, `"923-1"` 등)는 에러

### 6.2 공통 코드 정의

MCP 서버에서 자주 참조하므로 enum 또는 상수로 관리할 것을 권장한다.

**예약 상태 코드 (`resveSttusCd`, 코드그룹 CST156)**:

| 코드 | 의미 | 비고 |
|---|---|---|
| `STT001` | 예약완료 | 정상 등록된 예약. 입실 가능 상태 |
| `STT002` | 예약취소 | 취소된 예약 |
| `STT003` | 사용중 (입실완료) | 입실 처리됨. 회의 진행 중 |
| `STT004` | 회의종료 (퇴실완료) | 퇴실 처리됨 |

**예약 상태 전이 규칙** (백엔드에서 검증):

- 등록 시 → `STT001`로 생성
- 취소(`sttDiv=resveCancel`): `STT001` → `STT002` (그 외 상태에서는 거부)
- 입실(`sttDiv=checkin`): `STT001` + 시간 범위 내 → `STT003`
- 퇴실(`sttDiv=checkout`): `STT003` → `STT004`

**MCP 도구가 다루는 상태**: 조회 시 `STT001`(예약완료), `STT003`(사용중)만 보통 의미 있음. `STT002`(취소)는 사용자가 별도 요청 시에만 노출.

### 6.3 endpoint별 상세 명세

#### 6.3.1 사무실 조회 — `list_offices`

- **URL**: `POST /com/smartofc/mtgTablet/selectOffmList.do`
- **Forward**: `/com/smartofc/offmMngr/selectList.do?mode=combo`
- **요청 헤더**:
  - `Referer: {backend.api.referer}` ← 필수
  - `Content-Type: application/x-www-form-urlencoded`
- **요청 파라미터** (모두 선택):

| 파라미터 | 타입 | 설명 |
|---|---|---|
| `lcCd` | string | 위치 코드 (공통코드 CST157). 특정 지역만 필터링 시 |
| `offmId` | string | 특정 사무실 ID로 단건 조회 |

- **응답 (`rows` 항목, mode=combo)**:

| 필드 | 타입 | 설명 |
|---|---|---|
| `offmId` | string | 사무실 ID (PK, UUID 형태) |
| `offmNm` | string | `"{위치코드명} {사무실명}"` 형태 (예: "서울 본사"). DB에서 CONCAT으로 합쳐서 내려옴 |
| `atchFileId` | string | 첨부파일 ID (보통 무시) |

- **참고**: 위 응답은 `mode=combo`일 때의 축약 컬럼. 본 사무실의 분리된 위치/이름이 필요하면 mode 제거하고 full column 호출 가능 (`lcCd`, `alignNo` 등 포함).

---

#### 6.3.2 회의실 조회 — `list_meeting_rooms`

- **URL**: `POST /com/smartofc/mtgTablet/selectMtgRmList.do`
- **Forward**: `/com/smartofc/mtgrmMngr/selectList.do?dt={오늘날짜}`

⚠️ **중요 함정**: forward URL에 `dt={오늘}`이 하드코딩되어 있어, 클라이언트가 보낸 `dt`와 충돌 가능. 미래 날짜의 회의실 가용성을 조회하려 할 때 영향을 받을 수 있음.

**회피 방법**: 본 endpoint(`/com/smartofc/mtgrmMngr/selectList.do`)를 직접 호출하면 referer 검증이 없어서 그대로 통과한다. `MtgrmMngrController`에 referer 검증 없고, 인증 코드는 주석 처리되어 있음. coder agent는 실제 동작 확인 후 결정:

1. **1차 시도**: 태블릿 endpoint를 호출, `dt`를 명시적으로 전달 → 동작하면 사용
2. **fallback**: 동작 이상하면 본 endpoint 직접 호출

- **요청 파라미터**:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| `offmId` | string | 사무실 필터 |
| `mtgrmId` | string | 특정 회의실 ID |
| `mtgrmNm` | string | 회의실명 (LIKE prefix 검색) |
| `mtgrmSeCd` | string | 회의실 구분 코드 (CST158) |
| `dt` | string | 조회 기준일 (`yyyy-MM-dd`). 예약 불허기간 제외 판정용 |

- **응답 (`rows` 항목, full column)**:

| 필드 | 타입 | 설명 |
|---|---|---|
| `mtgrmId` | string | 회의실 ID (PK) |
| `mtgrmNm` | string | 회의실명 (예: "18층 3회의실") |
| `seatQty` | int | 좌석 수 (수용 인원) |
| `fxtrsInfo` | string | 비품 정보 (자유 텍스트) |
| `rm` | string | 비고 |
| `offmId` | string | 소속 사무실 ID |
| `offmEtc` | string | 사무실 기타 정보 |
| `videoMtgrmAt` | string | 화상 회의실 여부 (`Y`/`N`) |
| `mtgrmSeCd` | string | 회의실 구분 코드 (CST158) |
| `useAt` | string | 사용 여부 (`Y`/`N`) |
| `atchFileId` | string | 첨부파일 ID |

---

#### 6.3.3 가용 회의실 조회 — `check_availability`

- **URL**: `POST /com/smartofc/mtgTablet/selectMtgRmList.do` (위와 동일 endpoint)
- **차이점**: `whereType=available` 파라미터 추가

- **요청 파라미터**:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| `whereType` | string | **`available`** 고정 |
| `dt` | string | 조회 날짜 (`yyyy-MM-dd`), 필수 |
| `bgnTime` | string | 시작 시간 (`HH:mm`), 필수 |
| `endTime` | string | 종료 시간 (`HH:mm`), 필수 |
| `offmId` | string | 사무실 필터 (선택) |

⭐ **`whereType=available` 백엔드 직접 지원**: `MtgrmMngr_maria.xml`의 `where` 절에 다음과 같은 `NOT EXISTS` 서브쿼리로 시간 겹침 회의실을 자동 제외하는 분기가 이미 구현되어 있음. **MCP 서버에서 별도 조합 로직 불필요**.

```sql
AND NOT EXISTS (
    SELECT MTGRM_ID
    FROM T_MTGRM_RESVE
    WHERE DT = #{dt}
    AND   BGN_TIME < #{endTime}
    AND   END_TIME > #{bgnTime}
    AND   RESVE_STTUS_CD != 'STT002'
    AND   USE_AT = 'Y'
    AND   MTGRM_ID = A.MTGRM_ID
)
```

- **응답**: `list_meeting_rooms`와 동일 컬럼

- **MCP 서버 후처리**: 요청 파라미터 `minCapacity`(백엔드 미지원)는 응답을 받아서 `seatQty >= minCapacity` 조건으로 필터링한 후 반환한다.

---

#### 6.3.4 예약 목록 조회 — `list_reservations`

- **URL**: `POST /com/smartofc/mtgTabletResve/selectResveList.do`
- **Forward**: `/com/smartofc/mtgrmResve/selectList.do?withResveCancel=N&dt={오늘날짜}`

⚠️ 위와 동일한 forward dt 충돌 가능성. 미래/과거 날짜 조회 시 본 endpoint(`/com/smartofc/mtgrmResve/selectList.do`) 직접 호출 fallback 필요할 수 있음.

- **요청 파라미터**:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| `dt` | string | 단일 날짜 (`yyyy-MM-dd`) |
| `bgnDt` + `endDt` | string | 날짜 범위 |
| `mtgrmId` | string | 회의실 필터 |
| `offmId` | string | 사무실 필터 |
| `rsvctmId` | string | 예약자 사번 (scope=mine일 때 UserContext.empNo 주입) |
| `resveSttusCd` | string | 예약 상태 필터 (`STT001` 등) |
| `mtgSj` | string | 회의 제목 (LIKE `%mtgSj%`) |
| `userNm` | string | 예약자명 (LIKE `userNm%`) |
| `mtgrmNm` | string | 회의실명 (LIKE `mtgrmNm%`) |
| `withResveCancel` | string | `N`이면 취소건 제외 (기본 forward URL에 주입됨) |

- **응답 (`rows` 항목)**:

| 필드 | 타입 | 설명 |
|---|---|---|
| `resveId` | string | 예약 ID (PK, 취소 도구 호출 시 필요) |
| `mtgSj` | string | 회의 제목 |
| `dt` | string | 일자 (`yyyy-MM-dd`) |
| `bgnTime` | string | 시작 시각 (`HH:mm`) |
| `endTime` | string | 종료 시각 (`HH:mm`) |
| `rsvctmId` | string | 예약자 ID (esntlId, 사번과 동일) |
| `rsvctmNm` | string | 예약자명 |
| `emplNo` | string | 예약자 사번 |
| `ofcpsNm` | string | 예약자 직위 |
| `orgnztNm` | string | 예약자 부서명 |
| `userInfo` | string | `"{name}({사번}) / {부서}"` 표시용 |
| `atdrnQty` | int | 참석자 수 |
| `mtgrmId` | string | 회의실 ID |
| `mtgrmNm` | string | 회의실명 |
| `offmId` | string | 사무실 ID |
| `offmNm` | string | 사무실명 (위치 prefix 포함) |
| `videoMtgrmAt` | string | 화상 회의실 여부 |
| `resveSttusCd` | string | 예약 상태 (STT001~STT004) |
| `confmAt` | string | 승인 여부 |
| `secretAt` | string | 비밀 여부 |
| `psnetEventId` | string | 그룹웨어 연동 ID (취소 시 그룹웨어 일정 취소용으로 필요) |
| `parntsResveId` | string | 부모 예약 ID (반복 예약) |
| `reptitCycle`, `reptitUnitCd`, `day`, `endSeCd`, `endDt`, `endCnt` | - | 반복 예약 관련 (Phase 1에서는 무시) |

- **scope 처리 로직** (MCP 서버 내부):
  - `scope="mine"`: 요청 파라미터에 `rsvctmId={UserContext.getEmpNo()}` 추가
  - `scope="all"` 또는 미지정: 사번 파라미터 미주입 (모든 예약 조회)

---

#### 6.3.5 직원 검색 — `search_employees`

- **URL**: `POST /com/smartofc/mtgTabletResve/selectEmplyrList.do`
- **Forward**: `/com/hr/comtnemplyrinfo/selectList.do?emplyrSttusCode=P`
- **백엔드 구현**: 표준 전자정부 프레임워크 사용자 정보 서비스에 위임 (`COMTNEMPLYRINFO` 테이블)

- **요청 파라미터** (검색 기준 중 택일 권장):

| 파라미터 | 타입 | 설명 |
|---|---|---|
| `userNm` | string | 이름 (LIKE) |
| `emplyrId` | string | 사용자 아이디 |
| `orgnztNm` | string | 부서명 (LIKE) |
| `emplNo` | string | 사번 (정확 일치) |
| `emplyrSttusCode` | string | 가입상태. 기본 `P`(정상) 주입됨 |

- **MCP 도구 구현 가이드**: `keyword` 단일 파라미터를 받아서, 숫자 형태이면 `emplNo`, 그 외는 `userNm`으로 라우팅. 또는 두 번 호출 후 결과 합치기 (이름 + 부서명 동시 매칭).

- **응답 (`rows` 항목)**:

| 필드 | 타입 | 설명 |
|---|---|---|
| `emplyrId` | string | 사용자 아이디 |
| `emplNo` | string | 사번 (MCP에서 attendee로 사용할 키 값) |
| `userNm` | string | 사용자명 |
| `ofcpsNm` | string | 직위 (CST126 코드 매핑된 값) |
| `orgnztNm` | string | 부서명 |
| `mbtlnum` | string | 이동전화번호 |
| `emailAdres` | string | 이메일 주소 |
| `emplyrSttusCode` | string | 가입 상태 (P=정상) |

---

#### 6.3.6 예약 등록 — `create_reservation`

- **URL**: `POST /com/smartofc/mtgTabletResve/insert.do`
- **Forward**: `/com/smartofc/mtgrmResve/insert.do`
- **Content-Type**: `multipart/form-data`
  - `UploadHelper`가 multipart로 받음. 파일 없어도 multipart로 보내야 함
  - 만약 multipart가 어려우면 `application/x-www-form-urlencoded`로 시도해보고 동작 확인 필요

- **요청 파라미터**:

**필수**:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| `mtgrmId` | string | 회의실 ID |
| `dt` | string | 예약 날짜 (`yyyy-MM-dd`) |
| `bgnTime` | string | 시작 시간 (`HH:mm`) |
| `endTime` | string | 종료 시간 (`HH:mm`) |
| `mtgSj` | string | 회의 제목 |
| `rsvctmId` | string | 예약자 사번 (`UserContext.getEmpNo()` 주입) |
| `lastUpdusrId` | string | 최종 수정자 사번 (예약자와 동일) |

**고정값 권장** (생략 시 NULL이 들어가서 INSERT 실패 가능):

| 파라미터 | 권장값 | 설명 |
|---|---|---|
| `confmAt` | `Y` | 승인 여부 |
| `secretAt` | `N` | 비밀 여부 |
| `resveSttusCd` | `STT001` | 예약 상태 (예약완료) |
| `day` | `[]` | 반복 요일 JSON. 일반 예약은 빈 배열 문자열 |
| `reptitUnitCd` | (빈 문자열) | 반복 단위. 일반 예약은 공백 |
| `atdrnQty` | `1` 또는 참석자 수 | 참석자 수 (참석자 목록 길이로 계산 권장) |

**선택**:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| `atdrnUserList` | string (JSON) | 참석자 목록 JSON 문자열. 예: `'[{"emplNo":"12345","userNm":"홍길동"}]'`. 비어 있으면 `'[]'` |
| `mtgCtt` | string | 회의 내용 (description) |
| `rsvctmNm` | string | 예약자명 (display용. 백엔드에서 다시 조회하므로 필수는 아님) |
| `atchFileId` | string | 첨부파일 ID. Phase 1에서는 미지정 |
| `parntsResveId` | string | 부모 예약 ID (반복 예약용. Phase 1 미사용) |
| `psnetEventId` | string | 그룹웨어 이벤트 ID. 등록 후 받는 값이라 보통 비움 |

- **응답 (`ResultVO`)**:

| 필드 | 의미 |
|---|---|
| `status` | `"000"`이면 "바로 입실 가능 시점"이라는 의미, 그 외 빈 값이면 일반 성공. 에러 시 `"921"`, `"999"` 등 |
| `message` | 에러 메시지 또는 "바로 입실처리 하시겠습니까?" |
| `data` | 성공 시 `{resveId, rsvctmId}` 포함 가능 (즉시 입실 가능한 경우만) |
| `total` | INSERT 영향 건수 |

⚠️ MCP 응답 매핑 시 주의: "즉시 입실" 응답은 MCP 도구로는 의미 없음 (원격 호출이라 입실 처리할 수 없음). 응답 처리 시 단순히 `total > 0`이면 성공으로 판정하면 됨.

- **부수 효과** (사용자에게 명확히 알릴 것):
  - 참석자 + 예약자 전원에게 메신저 알림 자동 발송 (`NotiConnector.sendMessenger`)
  - 사내 그룹웨어 일정 자동 등록 (`ApprvConnector.send`)
  - 시간 충돌 시 에러 응답 (`whereType=duplResve` 검증)

---

#### 6.3.7 예약 취소 — `cancel_reservation`

- **URL**: `POST /com/smartofc/mtgTabletResve/updateSttus.do`
- **Forward**: `/com/smartofc/mtgrmResve/updateSttus.do?mode=updateSttusTablet`

- **요청 파라미터**:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| `resveId` | string | **필수**. 취소할 예약 ID |
| `sttDiv` | string | **필수**. 고정값 `resveCancel` |
| `lastUpdusrId` | string | 요청자 사번 (`UserContext.getEmpNo()`) |
| `psnetEventId` | string | 그룹웨어 이벤트 ID. 취소 시 그룹웨어 일정도 같이 취소되므로 권장. `list_reservations` 결과에서 획득해서 전달 |
| `parntsResveId` | string | 반복 예약 부모 ID (반복 예약 전체 취소 시. Phase 1 미사용) |

- **응답 (`ResultVO`)**:
  - 정상: `status` 비어있거나 "000", `data`에 영향 건수
  - 거부: `status="923-2"`, message="예약을 취소할 수 없는 건입니다..." (`STT001` 상태가 아닐 때)
  - `total > 0`이면 성공

- **백엔드 동작**:
  - `resveSttusCd`가 `STT001`(예약완료)인 경우만 취소 가능 → `STT002`로 변경
  - 그룹웨어 일정 취소: `ApprvConnector.send("SCDLDEL", ...)`
  - 참석자에게 취소 알림: `mtgrmResveNoti(resveId, "CANCEL")`

- **MCP 도구 구현 노트**:
  - 본인이 예약한 건만 취소 가능하도록 백엔드가 자동으로 검증하지는 않음. UserContext의 사번과 예약의 `rsvctmId`가 일치하는지 **MCP 서버에서 한 번 더 검증** 권장 (먼저 `list_reservations`로 조회해서 `rsvctmId == UserContext.getEmpNo()` 확인)

---

### 6.4 BackendApiClient 구현 예시

```java
@Component
public class BackendApiClient {

    private final RestClient restClient;

    public BackendApiClient(RestClient.Builder builder, BackendApiProperties props) {
        this.restClient = builder
            .baseUrl(props.getBaseUrl())
            .defaultHeader("Referer", props.getReferer())
            .defaultHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
            .build();
    }

    public List<OfficeDto> findOffices(String lcCd, String offmId) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        if (lcCd != null) form.add("lcCd", lcCd);
        if (offmId != null) form.add("offmId", offmId);

        ListVO<OfficeDto> response = restClient.post()
            .uri("/com/smartofc/mtgTablet/selectOffmList.do")
            .body(form)
            .retrieve()
            .body(new ParameterizedTypeReference<ListVO<OfficeDto>>() {});

        return response.getRows();
    }

    public List<MeetingRoomDto> findAvailableRooms(String dt, String bgnTime, String endTime, String offmId) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("whereType", "available");
        form.add("dt", dt);
        form.add("bgnTime", bgnTime);
        form.add("endTime", endTime);
        if (offmId != null) form.add("offmId", offmId);

        ListVO<MeetingRoomDto> response = restClient.post()
            .uri("/com/smartofc/mtgTablet/selectMtgRmList.do")
            .body(form)
            .retrieve()
            .body(new ParameterizedTypeReference<ListVO<MeetingRoomDto>>() {});

        return response.getRows();
    }

    public ResultVO insertReservation(InsertReservationRequest req) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("mtgrmId", req.getMtgrmId());
        form.add("dt", req.getDt());
        form.add("bgnTime", req.getBgnTime());
        form.add("endTime", req.getEndTime());
        form.add("mtgSj", req.getMtgSj());
        form.add("rsvctmId", req.getEmpNo());
        form.add("lastUpdusrId", req.getEmpNo());
        // 고정값
        form.add("confmAt", "Y");
        form.add("secretAt", "N");
        form.add("resveSttusCd", "STT001");
        form.add("day", "[]");
        form.add("reptitUnitCd", "");
        form.add("atdrnQty", String.valueOf(
            req.getAttendees() != null ? req.getAttendees().size() : 1));
        // 참석자 목록 JSON
        form.add("atdrnUserList", toJson(req.getAttendees()));
        if (req.getMtgCtt() != null) form.add("mtgCtt", req.getMtgCtt());

        return restClient.post()
            .uri("/com/smartofc/mtgTabletResve/insert.do")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(form)
            .retrieve()
            .body(ResultVO.class);
    }

    public ResultVO cancelReservation(String resveId, String empNo, String psnetEventId) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("resveId", resveId);
        form.add("sttDiv", "resveCancel");
        form.add("lastUpdusrId", empNo);
        if (psnetEventId != null) form.add("psnetEventId", psnetEventId);

        return restClient.post()
            .uri("/com/smartofc/mtgTabletResve/updateSttus.do")
            .body(form)
            .retrieve()
            .body(ResultVO.class);
    }

    // 나머지 메서드 동일 패턴
}
```

### 6.5 응답 wrapper 클래스 (ListVO, ResultVO)

```java
@Data
public class ListVO<T> {
    private List<T> rows;
    private Integer total;
    private String status;
    private String message;
}

@Data
public class ResultVO {
    private Object data;
    private Integer total;
    private String status;
    private String message;
}
```

---

## 7. 프로젝트 구조

```
meeting-room-mcp/
├── pom.xml
├── README.md
├── Dockerfile
├── docker-compose.yml
├── src/
│   ├── main/
│   │   ├── java/com/psnm/mcp/meetingroom/
│   │   │   ├── MeetingRoomMcpApplication.java     # @SpringBootApplication
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── BackendApiProperties.java      # @ConfigurationProperties("backend.api")
│   │   │   │   ├── BackendApiConfig.java          # RestClient Bean
│   │   │   │   └── WebMvcConfig.java              # Interceptor 등록
│   │   │   │
│   │   │   ├── interceptor/
│   │   │   │   └── EmpNoHeaderInterceptor.java
│   │   │   │
│   │   │   ├── context/
│   │   │   │   └── UserContext.java               # ThreadLocal 사번 보관
│   │   │   │
│   │   │   ├── tools/
│   │   │   │   └── MeetingRoomTools.java          # @McpTool 메서드 모음
│   │   │   │
│   │   │   ├── service/
│   │   │   │   └── ReservationService.java        # 도구별 비즈니스 로직 위임
│   │   │   │
│   │   │   ├── client/
│   │   │   │   ├── BackendApiClient.java
│   │   │   │   └── dto/
│   │   │   │       ├── OfficeDto.java
│   │   │   │       ├── MeetingRoomDto.java
│   │   │   │       ├── ReservationDto.java
│   │   │   │       ├── EmployeeDto.java
│   │   │   │       ├── CreateReservationResult.java
│   │   │   │       ├── CancelReservationResult.java
│   │   │   │       ├── InsertReservationRequest.java
│   │   │   │       ├── ListVO.java               # 백엔드 응답 wrapper
│   │   │   │       └── ResultVO.java
│   │   │   │
│   │   │   ├── constants/
│   │   │   │   └── ReservationStatus.java        # STT001~STT004 enum
│   │   │   │
│   │   │   └── exception/
│   │   │       ├── BackendApiException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── logback-spring.xml
│   │
│   └── test/
│       └── java/com/psnm/mcp/meetingroom/
│           ├── tools/MeetingRoomToolsTest.java
│           └── client/BackendApiClientTest.java
```

---

## 8. 개발 환경 셋업 가이드

### 8.1 가상화 환경 준비 (Docker 권장)

```dockerfile
# Dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/meeting-room-mcp-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  meeting-room-mcp:
    build: .
    ports:
      - "8080:8080"
    environment:
      BACKEND_BASE_URL: http://[기존시스템호스트]
      BACKEND_REFERER: http://[기존시스템호스트]/com/smartofc/mtgTablet_list.do
      TZ: Asia/Seoul
    restart: unless-stopped
```

### 8.2 포트포워딩

VM/컨테이너의 8080 포트를 호스트 PC의 8080(또는 다른 포트)으로 매핑. 같은 PC 안에서 Claude Desktop 테스트 시 `localhost:8080`으로 접근 가능.

### 8.3 빌드 및 실행

```bash
# 로컬 빌드
mvn clean package

# Docker 빌드 & 실행
docker compose up --build -d

# 로그 확인
docker compose logs -f meeting-room-mcp
```

### 8.4 MCP Inspector로 사전 검증

Claude Desktop 연결 전에 [MCP Inspector](https://modelcontextprotocol.io/docs/tools/inspector)로 도구 동작을 검증한다:

```bash
npx @modelcontextprotocol/inspector
```

- Transport Type: `Streamable HTTP`
- URL: `http://localhost:8080/mcp`
- 사번 테스트: 헤더에 `x-emp-no: [테스트사번]` 추가
- 각 도구 호출 → 정상 응답 확인

---

## 9. Claude Desktop 설정 가이드

### 9.1 설정 파일 위치

- Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`

### 9.2 설정 예시

```json
{
  "mcpServers": {
    "meeting-room": {
      "type": "http",
      "url": "http://[MCP서버주소]:8080/mcp",
      "headers": {
        "x-emp-no": "[본인 사번]"
      }
    }
  }
}
```

운영 단계에서는 nginx reverse proxy를 통해 HTTPS로 노출하고 URL을 `https://obliviate.o-r.kr/mcp/meeting-room/mcp` 같은 형태로 변경.

---

## 10. 알려진 제약사항 (보안 미조치 사항)

본 프로젝트는 사내망 한정 + 사용자 편의성 우선 정책에 따라 다음 제약을 인지한 채 진행한다. 향후 선임자 검토 후 별도 트랙으로 보안 강화 예정:

1. **인증 없음**: MCP 서버는 사용자 인증을 수행하지 않음. `x-emp-no` 헤더에 임의 사번을 넣으면 그 사람으로 위장 가능
2. **사번 위장**: Claude Desktop 설정만 바꾸면 누구든 다른 사람 사번으로 예약 가능
3. **기존 시스템 보안 취약점**: 작년 12월 보안 감사 보고서(`E:\_JavaDev\workspace\espora-min-ps-ofc\security-audit-report.md`)의 Critical 4건 미조치 상태
   - SQL Injection (`${reptitUnitCd}`)
   - Spring Security 미사용
   - CSRF 토큰 전체 미사용
   - 비밀번호 해시 취약 (사번을 Salt로)
4. **referer 검증의 한계**: 기존 태블릿 API의 referer 검증은 HTTP 헤더 위조로 우회 가능 (본 MCP 서버가 그 패턴을 그대로 사용)

**완화 조치 (개발 단계에서 가능한 범위)**:
- MCP 서버 호출 로그를 별도 파일로 기록 (감사 추적용). `[timestamp] [empNo] [tool] [params]` 형식
- 운영 배포 시 nginx 단에서 사내 IP 화이트리스트 적용 (`allow 사내대역; deny all;`)
- README에 "본 시스템은 사내망 전용이며, 외부 노출 금지" 명시

---

## 11. 구현 단계 (Phase 계획)

### Phase 1: 스켈레톤 + 기본 동작 검증

목표: 가장 단순한 도구 하나가 백엔드와 연동하여 동작하는 것까지 확인

작업 항목:
1. Maven 프로젝트 생성, `pom.xml` 작성
2. `application.yml` 작성
3. `MeetingRoomMcpApplication`, `BackendApiConfig`, `BackendApiClient` 스켈레톤
4. `EmpNoHeaderInterceptor` + `UserContext` 구현
5. `WebMvcConfig`로 Interceptor 등록
6. `list_offices` 도구 1개 구현 + DTO 정의
7. MCP Inspector로 동작 확인
8. Claude Desktop 연결 테스트

완료 기준: Claude Desktop에서 "사무실 목록 알려줘"라고 했을 때 정상 응답

### Phase 2: Read-only 도구 완성

작업 항목:
- `list_meeting_rooms`
- `check_availability` (whereType=available 활용)
- `list_reservations`
- `search_employees`

forward URL의 `dt` 충돌 문제 확인 및 필요 시 본 endpoint fallback 구현.

완료 기준: 모든 조회/검색 시나리오가 자연어로 동작

### Phase 3: Write 도구 구현

작업 항목:
- `create_reservation`
- `cancel_reservation`
- destructive operation 가이드(description) 검증

특히 신경 쓸 점:
- 백엔드 `insert.do`의 multipart 처리 (UploadHelper가 multipart 요구)
- 그룹웨어 일정 자동 등록(`ApprvConnector.send`) 의도된 동작 확인
- 메신저 알림 발송 확인
- 예약 등록 실패 케이스 (시간 충돌 등) 사용자에게 명확히 전달
- cancel 도구에서 본인 예약 검증 로직 추가

완료 기준: 자연어로 "내일 3시에 18층 3회의실 예약해줘, 김아무개 과장님이랑"이 되고 취소도 가능

### Phase 4: 운영 배포

작업 항목:
- `obliviate.o-r.kr`에 Docker 컨테이너 배포
- nginx reverse proxy 설정 (`/mcp/meeting-room/` 경로 노출)
- 사내 IP 화이트리스트 적용
- HTTPS 적용 (certbot 또는 기존 인증서 재활용)
- 사내 사용자 대상 설정 가이드 배포

### Phase 5 (향후): 확장 기능

- 예약 수정 도구 (`update_reservation`)
- 반복 예약 지원
- 첨부파일 등록
- 보안 조치 (선임자 결재 후)

---

## 12. 테스트 계획

### 12.1 단위 테스트

- `BackendApiClient`: WireMock 또는 MockRestServiceServer로 백엔드 응답 mocking
- `UserContext`: ThreadLocal 격리 검증
- DTO 직렬화/역직렬화 테스트

### 12.2 통합 테스트

- 개발 환경 기존 시스템과 실제 연동
- MCP Inspector로 각 도구 정상 응답 확인

### 12.3 E2E 시나리오

Claude Desktop에서 다음 시나리오 모두 통과:

1. "오늘 예약된 회의실 알려줘" → `list_reservations`
2. "내 예약 보여줘" → `list_reservations(scope=mine)`
3. "5층에 어떤 회의실 있어?" → `list_meeting_rooms` (5층 사무실 ID 매칭 필요)
4. "내일 오후 2시에 5명짜리 회의실 비어있어?" → `check_availability` (minCapacity=5)
5. "내일 오후 2시부터 3시까지 18층 3회의실 예약해줘, 회의 제목은 'AI 개발 회의'" → `create_reservation` (확인 후)
6. "방금 그 예약 취소해줘" → `cancel_reservation` (확인 후)
7. "김철수 과장님 참석자로 추가해서 예약" → `search_employees` → `create_reservation`

---

## 13. 참고 자료

### 13.1 공식 문서

- [Spring AI MCP Reference](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html)
- [Spring AI MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- [Streamable HTTP MCP Servers (Spring AI)](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-streamable-http-server-boot-starter-docs.html)
- [MCP Specification](https://modelcontextprotocol.io/specification/)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)

### 13.2 본 프로젝트 관련 파일

기존 예약시스템 (`E:\_JavaDev\workspace\espora-min-ps-ofc\`):

- **태블릿 컨트롤러**: `src\main\java\biz\com\product\smartofc\web\MtgrmTabletController.java` (referer 검증 + forward)
- **본 예약 컨트롤러**: `src\main\java\biz\com\product\smartofc\web\MtgrmResveController.java` (실제 비즈니스 로직)
- **본 회의실 컨트롤러**: `src\main\java\biz\com\product\smartofc\web\MtgrmMngrController.java` (회의실 조회)
- **사무실 컨트롤러**: `src\main\java\biz\com\product\smartofc\web\OffmMngrController.java`
- **예약 Mapper**: `src\main\resources\egovframework\mapper\biz\com\product\smartofc\MtgrmResve_maria.xml`
- **회의실 Mapper**: `src\main\resources\egovframework\mapper\biz\com\product\smartofc\MtgrmMngr_maria.xml` (whereType=available 분기 포함)
- **사무실 Mapper**: `src\main\resources\egovframework\mapper\biz\com\product\smartofc\OffmMngr_maria.xml`
- **태블릿 JS**: `src\main\webapp\js\biz\com\product\smartofc\mtgTablet_inqire.js`, `mtgUsrTablet_list.js` (실제 호출 파라미터 참고)
- **직원 검색 JSP**: `src\main\webapp\WEB-INF\jsp\biz\com\customize\hr\user\comtnemplyrinfo_list.jsp` (검색 파라미터 키 참고)
- **DB 스키마**: `database\espora_DB_maria.sql`
- **보안 감사 보고서**: `security-audit-report.md`

---

## 14. coder AI agent를 위한 시작 체크리스트

이 문서를 받은 coder AI agent는 다음 순서로 진행한다:

1. ☐ `E:\_JavaDev\workspace\espora-min-ps-ofc\` 디렉토리 구조 파악
2. ☐ 본 문서 섹션 6의 API 명세를 기준으로 DTO 클래스 작성. 모호한 부분은 mapper XML / JS 파일 추가 분석으로 확정
3. ☐ DB 스키마(`database\espora_DB_maria.sql`)에서 회의실/예약/직원 테이블 구조 확인 (T_MTGRM_RESVE, T_MTGRM_MNGR, T_OFFM_MNGR, COMTNEMPLYRINFO)
4. ☐ 새 프로젝트 디렉토리 생성 (예: `E:\_JavaDev\workspace\meeting-room-mcp\`)
5. ☐ Phase 1 진행: 스켈레톤 + `list_offices` 단일 도구
6. ☐ MCP Inspector로 1차 검증 + 백엔드 응답이 본 문서 명세와 일치하는지 확인
7. ☐ 차이가 있다면 Wan에게 알리고 본 문서를 업데이트할지 결정
8. ☐ Phase 2~3 순차 진행
9. ☐ Phase 4 배포는 Wan과 협의 후 진행

### 진행 중 Wan에게 확인할 것

- 기존 시스템의 실제 호스트 URL (`backend.api.base-url`)
- 새 프로젝트 디렉토리 위치
- patch 형태로 단계별 결과를 review 받을지, 일괄 구현 후 review할지
- forward URL `dt` 충돌 이슈가 실제로 발생하는지 (Phase 2 시점에 테스트 필요)

---

## 변경 이력

| 일자 | 변경 내용 | 작성자 |
|---|---|---|
| 2026-05-19 | 초안 작성 | Wan & Claude |
| 2026-05-19 | 섹션 6 백엔드 API 명세 상세화 (코드 분석 결과 반영) | Claude |
