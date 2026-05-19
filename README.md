# Meeting Room MCP Server

Spring Boot 3.x + Spring AI 1.1.6 기반의 회의실 예약 시스템 MCP 서버입니다.

## 기능

- 회의실 예약 생성
- 회의실 예약 취소  
- 가용 회의실 조회
- 사용자 예약 목록 조회

## 구현 특징

### 1. MCP Tool 구현
- `@McpTool` 어노테이션 사용
- 한국어 도구 설명
- 파라미터 유효성 검증

### 2. 에러 처리
- Backend API 예외 처리
- 네트워크 오류 처리
- 예약 충돌 처리

### 3. 로깅
- 요청/응답 로깅
- 성능 모니터링
- 감사 로그

### 4. 검증
- DTO 유효성 검증
- 입력 파라미터 검증

## API 구성

### 예약 생성
`create_reservation`
- 매개변수: 회의실 ID, 날짜, 시작 시간, 종료 시간, 예약 목적
- 설명: 회의실 예약을 생성합니다.

### 예약 취소  
`cancel_reservation`
- 매개변수: 예약 ID
- 설명: 회의실 예약을 취소합니다.

### 가용 회의실 조회
`find_available_rooms`
- 매개변수: 날짜, 시작 시간, 종료 시간
- 설명: 지정된 시간대에 가용한 회의실 목록을 조회합니다.

### 내 예약 조회
`get_my_reservations`
- 설명: 사용자의 예약 목록을 조회합니다.

## 의존성

- Spring Boot 3.x
- Spring AI 1.1.6
- Streamable HTTP Transport
- OkHttp3 Client

## 실행

```bash
mvn spring-boot:run
```

## 테스트

```bash
mvn test
```