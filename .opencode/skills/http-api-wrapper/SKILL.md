# http-api-wrapper

기존 espora-min-ps-ofc API wrapping 규칙.

---

# 목적

본 skill은:

- RestClient 구현
- 기존 Tablet API 호출
- referer 처리
- form-urlencoded/multipart 처리
- 응답 wrapper 처리

작업 시 활성화된다.

---

# 핵심 원칙

본 프로젝트는 기존 API의 Wrapper다.

절대:

- 비즈니스 로직 재구현 금지
- DB 직접 접근 금지
- 예약 정책 재구현 금지

---

# HTTP Client 규칙

반드시 Spring RestClient 사용.

```java
RestClient.Builder
````

사용.

---

# 공통 헤더 규칙

모든 요청:

```http
Referer: {backend.api.referer}
```

자동 첨부.

---

# timeout 필수

설정 필수:

* connect timeout
* read timeout

무한 대기 금지.

---

# Content-Type 규칙

조회:

```http
application/x-www-form-urlencoded
```

예약 등록:

```http
multipart/form-data
```

---

# 응답 처리 규칙

ListVO:

```json
{
  "rows": [],
  "total": 0,
  "status": "000"
}
```

ResultVO:

```json
{
  "data": {},
  "status": "000"
}
```

---

# 상태값 처리

성공:

* status == "000"
* status empty

실패:

* 920
* 921
* 923-1
* 923-2
* 999

반드시 exception 변환.

---

# 예약 조회 규칙

scope=mine:

```text
rsvctmId = UserContext.empNo
```

주입.

---

# 가용 회의실 조회 규칙

반드시:

```text
whereType=available
```

사용.

직접 시간 충돌 계산 금지.

---

# create_reservation 규칙

고정값 반드시 세팅:

* confmAt=Y
* secretAt=N
* resveSttusCd=STT001
* day=[]
* reptitUnitCd=

---

# cancel_reservation 규칙

반드시:

```text
sttDiv=resveCancel
```

사용.

---

# fallback 규칙

forward dt 충돌 발생 시:

* tablet endpoint 실패
* 본 endpoint fallback

허용.

단:

* 실제 동작 검증 후 사용
* undocumented endpoint 추가 금지

---

# 금지

* SQL 직접 호출
* Mapper 직접 사용
* 기존 API 우회 구현
* referer bypass 시도

---

# 검증 체크리스트

* referer 헤더 정상
* UTF-8 한글 정상
* multipart 정상
* status 처리 정상
* timeout 처리 정상
* fallback 정상