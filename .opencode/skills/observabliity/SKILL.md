# observability

로깅 / 에러 처리 / 감사 추적 규칙.

---

# 목적

다음 작업 시 활성화:

- logging
- exception handling
- audit logging
- tracing

---

# 로깅 필수 항목

반드시 로그:

- MCP tool invocation
- empNo
- request latency
- backend API 호출
- 실패 원인

---

# 감사 로그

형식:

```text
[timestamp] [empNo] [tool] [params]
````

---

# 절대 로그 금지

* credential
* token
* cookie
* 개인정보 전체
* 참석자 개인정보 과다 출력

---

# 예외 처리 규칙

외부 예외 그대로 노출 금지.

반드시:

* BackendApiException 변환
* 사용자 친화 메시지 제공

---

# HTTP 에러 처리

반드시 처리:

* timeout
* 4xx
* 5xx
* malformed response
* empty response

---

# 예약 충돌 메시지

사용자 친화적으로 변환:

```text
선택한 시간대에 이미 예약된 회의실입니다.
```

---

# logging framework

* slf4j
* logback

사용.

System.out.println 금지.

---

# MDC 권장

가능하면:

* empNo
* requestId

MDC 저장.

---

# 검증 체크리스트

* 에러 stacktrace 정상
* 민감정보 미노출
* audit log 정상
* latency 측정 정상
