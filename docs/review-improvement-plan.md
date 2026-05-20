# PR #1 최신 리뷰 개선 계획

## 🎯 개선 목표
- 최신 리뷰에서 지적된 모든 문제 해결
- 빌드 및 실행 가능 상태 구현
- 보안 및 기능 완전성 확보

## 🔍 최신 리뷰 내용 (ID: 4325062228)

### 🚨 치명적 문제 (P0)
1. **BackendApiClient.java 중복 메서드**: 클래스 본문이 두 번 작성되어 빌드가 불가능
2. **psnetEventId 전달 미구현**: cancelReservation에서 그룹웨어 연동이 불가능
3. **예약 취소 본인 검증 미구현**: 보안 취약점

### ⚠️ 중요 문제 (P1)
4. **createReservation MultiValueMap 타입 불일치**: 런타임 오류 가능성

### ⚠️ 경미 문제 (P2)
5. **미사용 import**: 코드 정리 필요
6. **테스트 검증 방식**: Mockito 환경에서 검증이 제대로 동작하지 않음

## 🔧 개선 계획

### 1. 치명적 문제 해결 (P0)

#### 문제 1: BackendApiClient.java 중복 메서드
**개선 계획**: 중복된 메서드 제거
- **작업 내용**: 메서드 중복 부분 삭제
- **예상 소요 시간**: 1일

#### 문제 2: psnetEventId 전달 미구현  
**개선 계획**: psnetEventId 전달 구현
- **작업 내용**: 
  - BackendApiClient.cancelReservation() 메서드 시그니처 변경
  - MeetingRoomTools.cancelReservation()에서 psnetEventId 전달
- **예상 소요 시간**: 1일

#### 문제 3: 예약 취소 본인 검증 미구현
**개선 계획**: 본인 검증 로직 구현
- **작업 내용**:
  - BackendApiClient.findReservation() 메서드 추가
  - MeetingRoomTools.cancelReservation()에서 본인 검증 로직 구현
- **예상 소요 시간**: 1일

### 2. 중요 문제 해결 (P1)

#### 문제 4: MultiValueMap 타입 불일치
**개선 계획**: 타입 변경
- **작업 내용**: MultiValueMap<String, String> → MultiValueMap<String, Object>
- **예상 소요 시간**: 1일

### 3. 경미 문제 해결 (P2)

#### 문제 5: 미사용 import
**개선 계획**: 불필요 import 제거
- **작업 내용**: 필요 없는 import 삭제 (BackendApiConfig.java, MeetingRoomTools.java)
- **예상 소요 시간**: 1일

#### 문제 6: 테스트 검증 방식
**개선 계획**: 테스트 검증 방식 개선
- **작업 내용**: 유효성 검증 로직 추가 (MeetingRoomToolsTest.java)
- **예상 소요 시간**: 1일

## 📆 개발 일정

| 단계 | 작업 내용 | 예상 기간 |
|------|---------|----------|
| 1단계 | 치명적 문제 해결 | 3일 |
| 2단계 | 중요 문제 해결 | 1일 |
| 3단계 | 경미 문제 해결 | 1일 |

**총 예상 소요 시간**: 5일

## 🛠️ 기술적 요구사항

- Spring Boot 3.x + Spring AI 1.1.6
- REST client 통신 방식 유지
- 예외 처리 구조 개선
- 로깅 및 감사 추적 최적화

## 📋 변경사항 목록

### 1. BackendApiClient.java
- 중복 메서드 제거
- psnetEventId 전달 구현
- findReservation 메서드 추가
- MultiValueMap 타입 변경

### 2. MeetingRoomTools.java  
- cancelReservation 메서드 시그니처 변경
- 본인 검증 로직 추가
- psnetEventId 전달 구현

### 3. MeetingRoomToolsTest.java
- 테스트 검증 로직 개선

### 4. 문서 업데이트
- docs/pr1-improvement-plan.md 업데이트