package com.psnm.mcp.meetingroom.tools;

import com.psnm.mcp.meetingroom.client.BackendApiClient;
import com.psnm.mcp.meetingroom.client.dto.ListVO;
import com.psnm.mcp.meetingroom.client.dto.OfficeDto;
import com.psnm.mcp.meetingroom.tools.MeetingRoomTools;
import com.psnm.mcp.meetingroom.context.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingRoomToolsTest {

    @Mock
    private BackendApiClient backendApiClient;

    private MeetingRoomTools meetingRoomTools;

    @BeforeEach
    void setUp() {
        meetingRoomTools = new MeetingRoomTools(backendApiClient);
        // Set up mock UserContext
        UserContext.setEmpNo("EMP12345");
    }

    @Test
    void createReservation_Successful() {
        // Given
        String meetingRoomId = "ROOM001";
        String startDate = "2023-12-01";
        String startTime = "10:00";
        String endTime = "11:00";
        String purpose = "회의";

        // When
        var result = meetingRoomTools.createReservation(meetingRoomId, startDate, startTime, endTime, purpose);

        // Then
        assertNotNull(result);
        assertEquals("회의실 예약이 성공적으로 생성되었습니다.", result.getMessage());
    }

    @Test
    void cancelReservation_Successful() {
        // Given
        String reservationId = "RES001";
        String psnetEventId = "EVENT001";

        // When
        var result = meetingRoomTools.cancelReservation(reservationId, psnetEventId);

        // Then
        assertNotNull(result);
        assertEquals("예약이 성공적으로 취소되었습니다.", result.getMessage());
    }

    @Test
    void findAvailableRooms_Successful() {
        // Given
        String startDate = "2023-12-01";
        String startTime = "10:00";
        String endTime = "11:00";
        
        ListVO<OfficeDto> mockResponse = new ListVO<>();
        mockResponse.setRows(List.of(new OfficeDto()));
        when(backendApiClient.findAvailableRooms(anyString(), anyString(), anyString()))
                .thenReturn(mockResponse);

        // When
        var result = meetingRoomTools.findAvailableRooms(startDate, startTime, endTime);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getMyReservations_Successful() {
        // Given
        ListVO<OfficeDto> mockResponse = new ListVO<>();
        mockResponse.setRows(List.of(new OfficeDto()));
        when(backendApiClient.getMyReservations()).thenReturn(mockResponse);

        // When
        var result = meetingRoomTools.getMyReservations();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void listOffices_Successful() {
        // Given
        String lcCd = "LC001";
        String offmId = "OFFM001";
        
        ListVO<OfficeDto> mockResponse = new ListVO<>();
        mockResponse.setRows(List.of(new OfficeDto()));
        when(backendApiClient.findOffices(anyString(), anyString())).thenReturn(mockResponse);

        // When
        var result = meetingRoomTools.listOffices(lcCd, offmId);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void createReservation_ValidationFailure() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            meetingRoomTools.createReservation(null, null, null, null, null));
    }

    @Test
    void cancelReservation_ValidationFailure() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            meetingRoomTools.cancelReservation(null, null));
    }

    @Test
    void createReservation_ValidationFailure_WithEmptyValues() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            meetingRoomTools.createReservation("", "", "", "", ""));
    }

    @Test
    void cancelReservation_ValidationFailure_WithEmptyValues() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            meetingRoomTools.cancelReservation("", ""));
    }

    @Test
    void findAvailableRooms_ApiErrorHandling() {
        // Given
        String startDate = "2023-12-01";
        String startTime = "10:00";
        String endTime = "11:00";
        when(backendApiClient.findAvailableRooms(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("API Error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            meetingRoomTools.findAvailableRooms(startDate, startTime, endTime));
    }

    @Test
    void getMyReservations_ApiErrorHandling() {
        // Given
        when(backendApiClient.getMyReservations()).thenThrow(new RuntimeException("API Error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            meetingRoomTools.getMyReservations());
    }

    @Test
    void listOffices_ApiErrorHandling() {
        // Given
        String lcCd = "LC001";
        String offmId = "OFFM001";
        when(backendApiClient.findOffices(anyString(), anyString()))
                .thenThrow(new RuntimeException("API Error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            meetingRoomTools.listOffices(lcCd, offmId));
    }
}