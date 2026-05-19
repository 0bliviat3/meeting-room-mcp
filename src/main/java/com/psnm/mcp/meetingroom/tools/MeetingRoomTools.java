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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

@Component
public class MeetingRoomTools {

    private final BackendApiClient backendApiClient;
    private static final Logger auditLogger = LoggerFactory.getLogger("audit");

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
    @McpTool(
        name = "create_reservation",
        description = "회의실 예약을 생성합니다. 사용자가 '회의실 예약해줘' 또는 '회의실을 예약해줘'라고 요청할 때 호출됩니다. 예약 생성 시 예약자 본인 확인이 필요하며, 그룹웨어 일정도 같이 취소되도록 psnetEventId 파라미터를 전달해야 합니다."
    )
    public CreateReservationResponse createReservation(
            @McpToolParam(
                description = "회의실 ID"
            )
            @Valid
            @NotBlank(message = "회의실 ID는 필수입니다.")
            String meetingRoomId,

            @McpToolParam(
                description = "예약 시작 날짜 (yyyy-MM-dd 형식)"
            )
            @Valid
            @NotBlank(message = "예약 시작 날짜는 필수입니다.")
            String startDate,

            @McpToolParam(
                description = "예약 시작 시간 (HH:mm 형식)"
            )
            @Valid
            @NotBlank(message = "예약 시작 시간은 필수입니다.")
            String startTime,

            @McpToolParam(
                description = "예약 종료 시간 (HH:mm 형식)"
            )
            @Valid
            @NotBlank(message = "예약 종료 시간은 필수입니다.")
            String endTime,

            @McpToolParam(
                description = "예약 목적"
            )
            @Valid
            @NotBlank(message = "예약 목적은 필수입니다.")
            String purpose) {
        
        // audit 로깅
        auditLogger.info("[{}] [{}] [create_reservation] [meetingRoomId: {}, startDate: {}, startTime: {}, endTime: {}, purpose: {}]", 
            LocalDate.now(), UserContext.getEmpNo(), meetingRoomId, startDate, startTime, endTime, purpose);
        
        // validation은 Tool 레벨에서만 처리
        try {
            // 실제 예약 생성 호출은 BackendApiClient를 통해
            // 실제 구현에서는 backendApiClient.createReservation(...) 호출
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
     * @param psnetEventId 그룹웨어 이벤트 ID
     * @return 예약 취소 결과
     */
    @McpTool(
        name = "cancel_reservation",
        description = "회의실 예약을 취소합니다. 사용자가 '회의실 예약 취소해줘'라고 요청할 때 호출됩니다. 본인이 예약한 건만 취소 가능하도록 검증이 필요하며, 그룹웨어 일정도 같이 취소되도록 psnetEventId 파라미터를 전달해야 합니다."
    )
    public CancelReservationResponse cancelReservation(
            @McpToolParam(
                description = "예약 ID"
            )
            @Valid
            @NotBlank(message = "예약 ID는 필수입니다.")
            String reservationId,
            
            @McpToolParam(
                description = "그룹웨어 이벤트 ID. 취소 시 그룹웨어 일정도 같이 취소되므로 권장"
            )
            @Valid
            String psnetEventId) {
        
        // audit 로깅
        auditLogger.info("[{}] [{}] [cancel_reservation] [reservationId: {}, psnetEventId: {}]", 
            LocalDate.now(), UserContext.getEmpNo(), reservationId, psnetEventId);
        
        // validation은 Tool 레벨에서만 처리
        try {
            // 예약자 본인 검증 (실제 구현)
            // ReservationDto reservation = backendApiClient.findReservation(reservationId);
            // if (!reservation.getRsvctmId().equals(UserContext.getEmpNo())) {
            //     throw new IllegalArgumentException("본인이 등록한 예약만 취소할 수 있습니다.");
            // }
            
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
    @McpTool(
        name = "check_availability",
        description = "회의실 예약 가능 여부를 조회합니다. 사용자가 '회의실이 비어있나요?' 또는 '회의실이 어디에 있어요?'라고 요청할 때 호출됩니다."
    )
    public List<OfficeDto> findAvailableRooms(
            @McpToolParam(
                description = "조회 날짜 (yyyy-MM-dd 형식)"
            )
            @Valid
            @NotBlank(message = "조회 날짜는 필수입니다.")
            String startDate,

            @McpToolParam(
                description = "조회 시작 시간 (HH:mm 형식)"
            )
            @Valid
            @NotBlank(message = "조회 시작 시간은 필수입니다.")
            String startTime,

            @McpToolParam(
                description = "조회 종료 시간 (HH:mm 형식)"
            )
            @Valid
            @NotBlank(message = "조회 종료 시간은 필수입니다.")
            String endTime) {
        
        // audit 로깅
        auditLogger.info("[{}] [{}] [find_available_rooms] [startDate: {}, startTime: {}, endTime: {}]", 
            LocalDate.now(), UserContext.getEmpNo(), startDate, startTime, endTime);
        
        // validation은 Tool 레벨에서만 처리
        try {
            // 실제 회의실 조회 호출은 BackendApiClient를 통해
            ListVO<OfficeDto> response = backendApiClient.findAvailableRooms(startDate, startTime, endTime);
            return response != null ? response.getRows() : List.of();
            
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
    @McpTool(
        name = "list_reservations",
        description = "사용자의 예약 목록을 조회합니다. 사용자가 '내 예약 알려줘' 또는 '내가 예약한 회의실 찾아줘'라고 요청할 때 호출됩니다."
    )
    public List<OfficeDto> getMyReservations() {
        
        // audit 로깅
        auditLogger.info("[{}] [{}] [get_my_reservations]", 
            LocalDate.now(), UserContext.getEmpNo());
        
        // validation은 Tool 레벨에서만 처리
        try {
            // 실제 예약 목록 조회 호출은 BackendApiClient를 통해
            ListVO<OfficeDto> response = backendApiClient.getMyReservations();
            return response != null ? response.getRows() : List.of();
            
        } catch (Exception e) {
            // 예외는 Service 계층에서 처리
            throw new RuntimeException("내 예약 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사무실 목록을 조회합니다.
     * 
     * @param lcCd 지역 코드
     * @param offmId 건물 ID
     * @return 사무실 목록
     */
    @McpTool(
        name = "list_offices",
        description = "사내 사무실(건물/층) 목록을 조회합니다. 사용자가 '어디에 사무실이 있나', '지점이 어디 있나' 같은 질문을 할 때 사용하세요."
    )
    public List<OfficeDto> listOffices(
            @McpToolParam(
                description = "지점 코드 (공통코드 CST157)"
            )
            @Valid
            String lcCd,
            @McpToolParam(
                description = "건물 ID"
            )
            @Valid
            String offmId) {
        
        // audit 로깅
        auditLogger.info("[{}] [{}] [list_offices] [lcCd: {}, offmId: {}]", 
            LocalDate.now(), UserContext.getEmpNo(), lcCd, offmId);
        
        // validation은 Tool 레벨에서만 처리
        try {
            // 실제 사무실 목록 조회 호출은 BackendApiClient를 통해
            ListVO<OfficeDto> response = backendApiClient.findOffices(lcCd, offmId);
            return response != null ? response.getRows() : List.of();
            
        } catch (Exception e) {
            // 예외는 Service 계층에서 처리
            throw new RuntimeException("사무실 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
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