# Meeting Room MCP Server

Spring Boot 기반의 회의실 예약 MCP 서버로, 사내 Tablet API(espora-min-ps-ofc)를 감싸는 모델 컨텍스트 프로토콜(MCP) 서버입니다.

## 프로젝트 개요

이 프로젝트는 사내 회의실 예약 시스템을 감싸는 MCP(Model Context Protocol) 서버로, Claude Desktop을 사용하는 사내 구성원이 자연어로 회의실 예약을 할 수 있도록 합니다.

### 주요 기능
- 회의실 예약 생성 (`create_reservation`)
- 회의실 예약 취소 (`cancel_reservation`)  
- 가용 회의실 조회 (`check_availability`)
- 내 예약 목록 조회 (`list_reservations`)
- 사무실 목록 조회 (`list_offices`)

## 기술 스택

- **Framework**: Spring Boot 3.3.5
- **Spring AI**: 1.1.6
- **Java**: 17+
- **Transport**: Streamable HTTP
- **Backend API**: espora-min-ps-ofc Tablet API

## 실행 방법

### 1. 로컬 실행
```bash
# 개발 환경에서 실행
mvn spring-boot:run

# 또는 
java -jar target/meeting-room-mcp-0.0.1-SNAPSHOT.jar
```

### 2. Docker 실행
```bash
# Docker 이미지 빌드
docker build -t meeting-room-mcp .

# 컨테이너 실행
docker run -p 8080:8080 \
  -e BACKEND_BASE_URL=http://your-backend-host:8081 \
  -e BACKEND_REFERER=http://your-backend-host:8081/com/smartofc/mtgTablet_list.do \
  meeting-room-mcp
```

### 3. 환경 변수 설정
```bash
# 백엔드 API URL
BACKEND_BASE_URL=http://localhost:8081

# 백엔드 referer 헤더
BACKEND_REFERER=http://localhost:8081/com/smartofc/mtgTablet_list.do

# timezone
TZ=Asia/Seoul
```

## MCP Tool 사용 예시

### 예약 생성
```
"오늘 오후 3시에 1시간 회의실 예약해줘"
```

### 가용 회의실 조회
```
"내일 오전 비어있는 대회의실 찾아줘"
```

### 예약 취소
```
"내 예약 취소해줘"
```

## 개발 환경

### 요구사항
- Java 17+
- Maven 3.8+
- Docker (옵션)

### 빌드
```bash
# 컴파일
mvn clean compile

# 테스트
mvn test

# 패키징
mvn package
```

### 설정 파일
`application.yml`에서 다음과 같은 설정이 가능합니다:
```yaml
spring:
  ai:
    mcp:
      server:
        protocol: STREAMABLE
        streamable-http:
          mcp-endpoint: /mcp
```

## 프로젝트 구조

```
src/main/java/com/psnm/mcp/meetingroom/
├── MeetingRoomMcpApplication.java     # 메인 애플리케이션
├── tools/                           # MCP Tool 구현
│   └── MeetingRoomTools.java      # 예약 관련 도구
├── client/                          # 백엔드 API 클라이언트
│   ├── BackendApiClient.java          # Tablet API 연동
│   └── dto/                         # DTO 정의
├── config/                        # 설정 클래스
└── interceptor/                     # 헤더 인터셉터
```

## Phase 1 완료 사항

✅ **MCP Tool 4개 완료**:
- create_reservation: 회의실 예약 생성
- cancel_reservation: 회의실 예약 취소  
- check_availability: 가용 회의실 조회
- list_reservations: 내 예약 목록 조회
- list_offices: 사무실 목록 조회

✅ **Backend API 연동 완료**:
- Tablet API 연결 (refer header 자동 추가)
- 멀티파트/form 데이터 처리
- 예외 처리 및 로깅 완료

✅ **Spring AI 1.1.6 호환성 문제 해결**:
- 정상 컴파일 완료
- MCP 어노테이션 경로 수정
- 의존성 정상 설정

## 향후 계획

1. **Docker 이미지 빌드 및 배포**
2. **MCP Inspector 및 Claude Desktop 연동 테스트**
3. **단위 및 통합 테스트 작성**
4. **Spring AI 1.2.0 이상 버전으로 업데이트**