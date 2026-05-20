# PR #1 개선 계획 문서

## 🎯 개선 목표
- 모든 리뷰에서 지적된 문제 해결
- 기능 정상 동작 보장
- 빌드 및 실행 가능 상태 구현

## 🔧 1단계 개선 완료

### 1. 빌드 실패 문제 해결
**문제**: OkHttp3 의존성 누락, Logger import 누락, 여분 중괄호
**조치**:
- BackendApiConfig.java에서 OkHttp3ClientHttpRequestFactory 제거
- SimpleClientHttpRequestFactory로 변경
- BackendApiClient.java의 Logger import 추가
- 여분 중괄호 제거

### 2. 기능 미동작 문제 해결
**문제**: createReservation/cancelReservation stub 상태
**조치**:
- MeetingRoomTools.java에서 실제 API 호출 연결
- createReservation: backendApiClient.createReservation() 호출
- cancelReservation: backendApiClient.cancelReservation() 호출
- 예외 처리 추가 (실패 시 RuntimeException)

### 3. 오동작 문제 해결
**문제**: x-emp-no 헤더 백엔드 전달, Referrer 헤더명, scope 파라미터
**조치**:
- BackendApiClient.java에서 x-emp-no 헤더 제거
- "Referrer" → "Referer"로 헤더명 변경
- scope=mine 파라미터 제거
- cancelReservation에 lastUpdusrId, psnetEventId 추가

### 4. 타입 불일치 문제 해결
**문제**: MultiValueMap 타입 불일치
**조치**:
- BackendApiClient.java에서 MultiValueMap<String, Object>로 변경

### 5. 테스트 코드 개선
**문제**: import 오류, 기대값 불일치
**조치**:
- MeetingRoomToolsTest.java에서 import 경로 수정
- 테스트 기대값 수정
- 유효성 검증 로직 추가

### 6. 명세 불일치 개선
**문제**: 도구명/파라미터 불일치
**조치**:
- MCP 도구명과 파라미터 명세에 맞춤
- list_reservations → getMyReservations
- check_availability → findAvailableRooms
- list_offices → listOffices

## 📆 개발 일정

| 단계 | 작업 내용 | 예상 기간 | 상태 |
|------|-----------|-----------|------|
| 1단계 | 빌드 문제 해결 | 1일 | ✅ 완료 |
| 2단계 | 기능 동작 개선 | 2일 | ✅ 완료 |
| 3단계 | 오동작 문제 해결 | 1일 | ✅ 완료 |
| 4단계 | 테스트 코드 개선 | 1일 | ✅ 완료 |
| 5단계 | 명세 일치 확인 | 1일 | ✅ 완료 |

## 🛠️ 기술적 요구사항

- Spring Boot 3.x + Spring AI 1.1.6
- REST client 통신 방식 유지
- 기존 Tablet API 연동 유지
- 예외 처리 구조 개선
- 로깅 및 감사 추적 최적화