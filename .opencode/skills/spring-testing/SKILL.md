# spring-testing

Spring Boot 테스트 작성 규칙.

---

# 목적

다음 작업 시 활성화:

- JUnit5 테스트
- BackendApiClient 테스트
- MCP Tool 테스트
- Service 테스트

---

# 테스트 우선순위

1. BackendApiClient
2. Service
3. Tool
4. 통합 테스트

---

# HTTP 테스트

우선 사용:

```java
MockRestServiceServer
````

필요 시:

* WireMock

---

# 테스트 대상

반드시 검증:

* referer 헤더
* form parameter
* multipart request
* status parsing
* timeout handling
* error handling

---

# UserContext 테스트

반드시:

* ThreadLocal isolation
* clear() 호출

검증.

---

# MCP Tool 테스트

검증:

* parameter binding
* validation
* service delegation
* exception handling

---

# 통합 테스트

검증:

* /mcp endpoint
* Streamable HTTP
* header propagation

---

# 금지

* 실제 운영 API 호출
* flaky test
* sleep 기반 테스트

---

# 테스트 명명 규칙

```text
should_{expected}_when_{condition}
```

예시:

```text
shouldReturnAvailableRoomsWhenTimeRangeValid
```

---

# 검증 체크리스트

* mvn test 통과
* deterministic
* timeout 없음
* 외부 의존성 없음