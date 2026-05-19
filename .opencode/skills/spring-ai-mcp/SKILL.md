# spring-ai-mcp

Spring AI 1.1.x 기반 MCP Server 구현 규칙.

---

# 목적

본 skill은 다음 작업 시 자동 활성화된다:

- `@McpTool` 구현
- MCP endpoint 설정
- Streamable HTTP 설정
- Spring AI MCP Server 설정
- Tool description 설계
- Claude Desktop 연동

---

# 핵심 규칙

## MCP Tool은 얇게 유지

`@McpTool` 메서드는:

- 입력 검증
- Service 호출
- 결과 반환

만 수행한다.

비즈니스 로직 금지.

---

## Tool Naming 규칙

- 식별자: 영어 snake_case
- description: 한국어

예시:

```java
@McpTool(
    name = "create_reservation",
    description = "회의실 예약을 등록합니다."
)
````

---

# Description 작성 규칙

description은 Claude가 tool 선택에 사용하는 핵심 prompt다.

반드시 포함:

* 사용 목적
* 사용 예시
* destructive 여부
* 파라미터 포맷
* 주의사항

---

# destructive operation 규칙

다음 tool은 destructive operation이다:

* create_reservation
* cancel_reservation

description에 반드시:

* 사용자 확인 필요
* 알림 발송됨
* 그룹웨어 반영됨

명시.

---

# MCP 응답 규칙

응답은:

* deterministic
* stable
* JSON 직렬화 가능

해야 한다.

Map<String, Object> 남용 금지.

DTO 사용.

---

# Streamable HTTP 규칙

application.yml:

```yaml
spring:
  ai:
    mcp:
      server:
        protocol: STREAMABLE
```

endpoint:

```yaml
streamable-http:
  mcp-endpoint: /mcp
```

---

# 금지

* Tool 내부 HTTP 호출
* Tool 내부 DB 접근
* Tool 내부 ThreadLocal 조작
* any-like raw object 반환

---

# 권장 구조

```text
Tool
  -> Service
      -> BackendApiClient
```

---

# 검증 체크리스트

* MCP Inspector 연결 성공
* Claude Desktop tool 인식 성공
* tool schema 정상 노출
* header 전달 성공
* Streamable HTTP keepalive 정상