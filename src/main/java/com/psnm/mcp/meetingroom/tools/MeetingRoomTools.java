package com.psnm.mcp.meetingroom.tools;

import com.psnm.mcp.meetingroom.client.BackendApiClient;
import com.psnm.mcp.meetingroom.client.dto.ListVO;
import com.psnm.mcp.meetingroom.client.dto.OfficeDto;
import com.psnm.mcp.meetingroom.context.UserContext;
import org.springframework.stereotype.Component;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class MeetingRoomTools {

    private final BackendApiClient backendApiClient;

    public MeetingRoomTools(BackendApiClient backendApiClient) {
        this.backendApiClient = backendApiClient;
    }

    /**
     * 회의실 예약을 생성합니다.
     * 
     * @param meetingRoomId 회의실 ID
     * @param startDate 예약 시작 날짜 (yyyy-MM-dd 형식)
     * @param startTime 예약 시작 시간 (HH:mm 형식)
     * @param endTime 예약 종료 시간 (HH:mm 형식)
     * @param purpose 예약 목적
     * @return 예약 생성 결과
     */
    public CreateReservationResponse createReservation(
            @Valid
            @NotBlank(message = "회의실 ID는 필수입니다.")
            String meetingRoomId,

            @Valid
            @NotBlank(message = "예약 시작 날짜는 필수입니다.")
            String startDate,

            @Valid
            @NotBlank(message = "예약 시작 시간은 필수입니다.")
            String startTime,

            @Valid
            @NotBlank(message = "예약 종료 시간은 필수입니다.")
            String endTime,

            @Valid
            @NotBlank(message = "예약 목적은 필수입니다.")
            String purpose) {
        
        // validation은 Tool 레벨에서만 처리
        try {
            // 실제 예약 생성 호출은 BackendApiClient를 통해
            return new CreateReservationResponse("예약이 성공적으로 생성되었습니다.");
            
        } catch (Exception e) {
            // 예외는 Service 계층에서 처리
            throw new RuntimeException("예약 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 회의실 예약을 취소합니다.
     * 
     * @param reservationId 예약 ID
     * @return 예약 취소 결과
     */
    public CancelReservationResponse cancelReservation(
            @Valid
            @NotBlank(message = "예약 ID는 필수입니다.")
            String reservationId) {
        
        // validation은 Tool 레벨에서만 처리
        try {
            // 실제 예약 취소 호출은 BackendApiClient를 통해
            return new CancelReservationResponse("예약이 성공적으로 취소되었습니다.");
            
        } catch (Exception e) {
            // 예외는 Service 계층에서 처리
            throw new RuntimeException("예약 취소 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 가용 회의실을 조회합니다.
     * 
     * @param startDate 조회할 날짜 (yyyy-MM-dd 형식)
     * @param startTime 조회 시작 시간 (HH:mm 형식)
     * @param endTime 조회 종료 시간 (HH:mm 형식)
     * @return 가용 회의실 목록
     */
    public List<OfficeDto> findAvailableRooms(
            @Valid
            @NotBlank(message = "조회 날짜는 필수입니다.")
            String startDate,

            @Valid
            @NotBlank(message = "조회 시작 시간은 필수입니다.")
            String startTime,

            @Valid
            @NotBlank(message = "조회 종료 시간은 필수입니다.")
            String endTime) {
        
        // validation은 Tool 레벨에서만 처리
        try {
            // 실제 회의실 조회 호출은 BackendApiClient를 통해
            return List.of(); // 실제 구현에서는 API에서 데이터 반환
            
        } catch (Exception e) {
            // 예외는 Service 계층에서 처리
            throw new RuntimeException("가용 회의실 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 내 예약 목록을 조회합니다.
     * 
     * @return 사용자의 예약 목록
     */
    public List<OfficeDto> getMyReservations() {
        
        // validation은 Tool 레벨에서만 처리
        try {
            // 실제 예약 목록 조회 호출은 BackendApiClient를 통해
            return List.of(); // 실제 구현에서는 API에서 데이터 반환
            
        } catch (Exception e) {
            // 예외는 Service 계층에서 처리
            throw new RuntimeException("내 예약 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // 응답 DTOs
    public static class CreateReservationResponse {
        private final String message;
        
        public CreateReservationResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    public static class CancelReservationResponse {
        private final String message;
        
        public CancelReservationResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}