package com.psnm.mcp.meetingroom.client;

import com.psnm.mcp.meetingroom.client.dto.ListVO;
import com.psnm.mcp.meetingroom.client.dto.OfficeDto;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BackendApiClient {

    private final RestClient restClient;

    public BackendApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public ListVO<OfficeDto> findOffices(String lcCd, String offmId) {
        Map<String, String> form = new HashMap<>();
        if (lcCd != null) form.put("lcCd", lcCd);
        if (offmId != null) form.put("offmId", offmId);

        return restClient.post()
                .uri("/com/smartofc/mtgTablet/selectOffmList.do")
                .body(form)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<ListVO<OfficeDto>>() {});
    }
}