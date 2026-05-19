# spring-validation

DTO / Validation / Request Schema 규칙.

---

# 목적

다음 작업 시 활성화:

- DTO 정의
- validation annotation
- request/response schema
- 날짜/시간 validation

---

# DTO 원칙

모든 request/response:

- 명시적 DTO 사용
- raw Map 금지

---

# DTO 스타일

우선순위:

1. record
2. immutable class
3. lombok @Data

---

# Validation 필수

반드시 사용:

- @NotBlank
- @NotNull
- @Size
- @Pattern

---

# 날짜 포맷

```text
yyyy-MM-dd
````

Pattern 검증:

```java
@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
```

---

# 시간 포맷

```text
HH:mm
```

Pattern 검증:

```java
@Pattern(regexp = "\\d{2}:\\d{2}")
```

---

# nullable 처리

Optional parameter:

* nullable 허용
* required=false

필수 parameter:

* validation 필수

---

# enum 처리

예약 상태:

```java
ReservationStatus
```

enum 사용.

raw string 비교 최소화.

---

# JSON 직렬화 규칙

snake_case 유지 여부 확인.

백엔드 응답 필드명과 mismatch 금지.

---

# 금지

* implicit nullable
* validation 없는 입력
* raw Object
* unchecked cast

---

# 검증 체크리스트

* invalid date reject
* invalid time reject
* null reject 정상
* optional parameter 정상
* JSON serialize 정상