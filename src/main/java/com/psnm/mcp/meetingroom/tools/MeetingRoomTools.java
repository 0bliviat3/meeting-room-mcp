package com.psnm.mcp.meetingroom.tools;

import com.psnm.mcp.meetingroom.client.BackendApiClient;
import com.psnm.mcp.meetingroom.client.dto.OfficeDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MeetingRoomTools {

    private final BackendApiClient backendApiClient;

    public MeetingRoomTools(BackendApiClient backendApiClient) {
        this.backendApiClient = backendApiClient;
    }

    // Placeholder method until we figure out the right import
    public List<OfficeDto> listOffices() {
        // This will be implemented in Phase 2
        return null;
    }
}