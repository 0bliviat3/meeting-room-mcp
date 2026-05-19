# docker-runtime

Docker / 배포 환경 규칙.

---

# 목적

다음 작업 시 활성화:

- Dockerfile
- docker-compose
- nginx reverse proxy
- 운영 배포

---

# Docker 이미지 규칙

반드시:

- Java 17 기반
- 경량 이미지 우선

권장:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
````

---

# jar 실행 규칙

```dockerfile
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

# 환경변수 규칙

반드시 외부화:

* BACKEND_BASE_URL
* BACKEND_REFERER
* TZ

---

# 포트 규칙

내부:

```text
8080
```

기본 사용.

---

# docker-compose 규칙

반드시:

* restart policy
* environment
* port mapping

포함.

---

# 운영 nginx 규칙

권장:

* HTTPS
* reverse proxy
* IP whitelist

---

# health check 권장

가능하면:

```text
/actuator/health
```

사용.

---

# 로그 규칙

container stdout/stderr 사용.

파일 로그 최소화.

---

# 금지

* credential hardcoding
* host network 남용
* privileged container

---

# 검증 체크리스트

* docker build 성공
* docker compose up 성공
* /mcp 접근 가능
* 환경변수 정상
* timezone 정상