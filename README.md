# Meeting Room MCP Server

Spring Boot 기반의 회의실 예약 MCP 서버입니다. 사내 Tablet API(espora-min-ps-ofc)를 감싸 Claude Desktop에서 자연어로 회의실을 조회하고 예약할 수 있도록 합니다.

---

## 목차

1. [주요 기능](#주요-기능)
2. [기술 스택](#기술-스택)
3. [프로젝트 구조](#프로젝트-구조)
4. [서버 실행](#서버-실행)
5. [Claude Desktop 연결](#claude-desktop-연결)
6. [사용 예시](#사용-예시)
7. [개발 환경 설정](#개발-환경-설정)

---

## 주요 기능

| 도구 | 설명 | 주요 파라미터 |
|------|------|-------------|
| `list_offices` | 사무실(건물/층) 목록 조회 | `lcCd`, `offmId` |
| `check_availability` | 특정 사무실의 예약 가능 회의실 조회 | `offmId`, `startTime`, `endTime` |
| `list_reservations` | 내 예약 목록 조회 | 없음 (사번 자동 적용) |
| `create_reservation` | 회의실 예약 생성 | `meetingRoomId`, `startDate`, `startTime`, `endTime`, `purpose` |
| `cancel_reservation` | 회의실 예약 취소 | `reservationId`, `psnetEventId` |

---

## 기술 스택

- **Framework**: Spring Boot 3.3.5
- **Spring AI**: 1.1.6 (MCP Streamable HTTP)
- **Java**: 17+
- **Transport**: Streamable HTTP (`/mcp` 엔드포인트)
- **Backend API**: espora-min-ps-ofc Tablet API

---

## 프로젝트 구조

```
src/main/java/com/psnm/mcp/meetingroom/
├── MeetingRoomMcpApplication.java
├── tools/
│   └── MeetingRoomTools.java          # MCP 도구 5개 정의
├── client/
│   ├── BackendApiClient.java           # Tablet API 연동
│   └── dto/
│       ├── OfficeDto.java              # 사무실 정보 (list_offices)
│       ├── MeetingRoomDto.java         # 회의실 정보 (check_availability)
│       ├── ReservationDto.java         # 예약 정보 (list_reservations)
│       ├── ResultVO.java               # API 단건 응답 래퍼
│       └── ListVO.java                 # API 목록 응답 래퍼
├── config/
│   ├── BackendApiConfig.java
│   └── BackendApiProperties.java
├── context/
│   └── UserContext.java                # ThreadLocal 사번 관리
└── interceptor/
    └── EmpNoHeaderInterceptor.java     # x-emp-no 헤더 → UserContext
```

---

## 서버 실행

### 환경 변수

| 변수 | 필수 | 설명 | 기본값 |
|------|------|------|--------|
| `BACKEND_BASE_URL` | ✅ | Tablet API 서버 주소 | `http://localhost:8081` |
| `BACKEND_REFERER` | 선택 | Referer 헤더값 (백엔드 검증용) | `http://localhost:8081/com/smartofc/mtgTablet_list.do` |
| `TZ` | 선택 | 타임존 | `Asia/Seoul` |

### 방법 1 — 로컬 직접 실행 (Java 필요)

```bash
# 빌드
mvn clean package -DskipTests

# 실행 (환경변수 인라인 전달)
java -DBACKEND_BASE_URL=http://실제백엔드주소:8081 \
     -jar target/meeting-room-mcp-0.0.1-SNAPSHOT.jar
```

또는 `.env` 파일 대신 Windows 환경 변수로 설정 후:

```bash
mvn spring-boot:run
```

### 방법 2 — Docker 실행 (권장)

```bash
# 이미지 빌드
mvn clean package -DskipTests
docker build -t meeting-room-mcp .

# 컨테이너 실행
docker run -d \
  --name meeting-room-mcp \
  -p 8080:8080 \
  -e BACKEND_BASE_URL=http://실제백엔드주소:8081 \
  -e BACKEND_REFERER=http://실제백엔드주소:8081/com/smartofc/mtgTablet_list.do \
  -e TZ=Asia/Seoul \
  --restart unless-stopped \
  meeting-room-mcp
```

docker-compose 사용 시:

```bash
# .env 파일 생성
cat > .env << EOF
BACKEND_BASE_URL=http://실제백엔드주소:8081
BACKEND_REFERER=http://실제백엔드주소:8081/com/smartofc/mtgTablet_list.do
TZ=Asia/Seoul
EOF

docker-compose up -d
```

### 서버 동작 확인

```bash
# 헬스체크 (Spring Actuator 미사용 시 MCP 엔드포인트로 확인)
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
```

정상 응답 시 `{"jsonrpc":"2.0","id":1,"result":{...}}` 형태의 JSON이 반환됩니다.

---

## Claude Desktop 연결

Claude Desktop은 `claude_desktop_config.json`의 MCP 서버 항목에 HTTP URL을 직접 지정하는 기능을 지원하지 않습니다. **stdio 브리지 프로그램**을 통해 연결해야 합니다.

> **사번(직원 번호) 전달 필수**  
> 모든 요청에 `x-emp-no` 헤더로 사번을 전달해야 예약자 정보가 올바르게 처리됩니다.

---

### 방법 A — mcp-remote (Node.js 설치된 경우)

**Node.js 설치 확인**:
```bash
node -v   # v18 이상 권장
```

`claude_desktop_config.json` 설정:

```json
{
  "mcpServers": {
    "meeting-room": {
      "command": "npx",
      "args": [
        "mcp-remote",
        "http://서버주소:8080/mcp",
        "--header",
        "x-emp-no:본인사번"
      ]
    }
  }
}
```

**`claude_desktop_config.json` 위치**:
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`

---

### 방법 B — mcp-proxy + uv (Node.js 미설치 시)

`uv`는 Python 없이도 단독 설치 가능한 Rust 기반 패키지 관리자입니다.

**Step 1 — uv 설치** (이미 설치된 경우 생략):

```powershell
# Windows PowerShell
powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"
```

```bash
# macOS / Linux
curl -LsSf https://astral.sh/uv/install.sh | sh
```

**Step 2 — mcp-proxy 설치**:

```bash
uv tool install mcp-proxy
```

**Step 3 — `claude_desktop_config.json` 설정**:

```json
{
  "mcpServers": {
    "meeting-room": {
      "command": "mcp-proxy",
      "args": [
        "http://서버주소:8080/mcp",
        "--headers",
        "{\"x-emp-no\": \"본인사번\"}"
      ]
    }
  }
}
```

> **uv가 설치되어 있다면 `uvx`로 설치 없이 바로 실행도 가능합니다:**
> ```json
> {
>   "mcpServers": {
>     "meeting-room": {
>       "command": "uvx",
>       "args": [
>         "mcp-proxy",
>         "http://서버주소:8080/mcp",
>         "--headers",
>         "{\"x-emp-no\": \"본인사번\"}"
>       ]
>     }
>   }
> }
> ```

---

### 연결 확인

Claude Desktop을 재시작한 후 채팅창에서 아래와 같이 입력합니다:

```
사무실 목록 조회해줘
```

응답에 사무실 목록이 나타나면 연결 성공입니다.

---

## 사용 예시

### 회의실 가용성 조회 (다단계 흐름)

Claude는 자연어 요청에서 아래 순서로 도구를 자동 호출합니다.

```
사용자: "3층에서 오후 1시~2시 예약 가능한 회의실 알려줘"

1. list_offices 호출
   → "3층"에 해당하는 offmId 탐색

2. check_availability 호출
   → offmId, startTime=13:00, endTime=14:00

3. 응답 예시:
   "3층(마포티타운 3F)에서 13:00~14:00 예약 가능한 회의실입니다.
    - 1회의실 (8석)
    - 4회의실 (6석)
    - 프로젝트룸 화상 (12석, 화상회의 가능)"
```

### 예약 생성

```
사용자: "2회의실 오늘 오후 3시~4시 팀 미팅으로 예약해줘"

→ create_reservation 호출
  (meetingRoomId는 check_availability 결과의 mtgrmId 사용)
```

### 예약 취소 (다단계 흐름)

```
사용자: "오늘 그룹웨어 미팅 취소해줘"

1. list_reservations 호출
   → resveId, psnetEventId 확인

2. cancel_reservation 호출
   → resveId, psnetEventId 전달

3. 응답:
   "그룹웨어 미팅 (14:00~15:00, 2회의실(화상)) 예약이 취소되었습니다."
```

---

## 개발 환경 설정

### 요구사항

- Java 17+
- Maven 3.8+
- Docker (운영 배포 시)

### 로컬 빌드 및 테스트

```bash
# 전체 빌드 + 테스트
mvn clean verify

# 테스트만
mvn test

# 빌드 (테스트 스킵)
mvn clean package -DskipTests
```

### 주요 설정 파일

**`src/main/resources/application.yml`**

```yaml
backend:
  api:
    base-url: ${BACKEND_BASE_URL:http://localhost:8081}
    referer: ${BACKEND_REFERER:http://localhost:8081/com/smartofc/mtgTablet_list.do}
    connect-timeout: 5s
    read-timeout: 15s

server:
  port: 8080
```

### 주의사항

- Spring Boot는 `.env` 파일을 자동으로 읽지 않습니다. 환경변수는 OS 수준에서 설정하거나 `-D` JVM 인수로 전달하세요.
- `x-emp-no` 헤더는 Claude Desktop 설정(브리지 args)에서 관리합니다. MCP 서버 코드에서는 이 헤더를 백엔드로 전달하지 않고 `UserContext`(ThreadLocal)에 사번을 저장하는 용도로만 사용합니다.
- 백엔드 `Referer` 헤더는 `MtgrmTabletController.isValidReferer()` 검증을 통과하기 위한 값입니다. 실제 백엔드 URL에 맞게 설정이 필요합니다.
