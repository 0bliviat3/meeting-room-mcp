package com.psnm.mcp.meetingroom.client;

import com.psnm.mcp.meetingroom.client.dto.ListVO;
import com.psnm.mcp.meetingroom.client.dto.OfficeDto;
import com.psnm.mcp.meetingroom.client.dto.ResultVO;
import com.psnm.mcp.meetingroom.context.UserContext;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendApiClient {

    private static final Logger logger = LoggerFactory.getLogger(BackendApiClient.class);

    private final RestClient restClient;
    private final String referer;

    public BackendApiClient(RestClient restClient, String referer) {
        this.restClient = restClient;
        this.referer = referer;
    }

    /**
     * 회의실 목록을 조회합니다.
     * 
     * @param lcCd 지역 코드
     * @param offmId 건물 ID
     * @return 회의실 목록
     */
    public ListVO<OfficeDto> findOffices(String lcCd, String offmId) {
        logger.info("회의실 목록 조회 시작 - 지역코드: {}, 건물ID: {}", lcCd, offmId);
        
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            if (lcCd != null) form.add("lcCd", lcCd);
            if (offmId != null) form.add("offmId", offmId);

            // API 호출
            ListVO<OfficeDto> response = restClient.post()
                    .uri("/com/smartofc/mtgTablet/selectOffmList.do")
                    .header("Referer", referer)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<ListVO<OfficeDto>>() {});

            logger.info("회의실 목록 조회 성공 - 건물 수: {}", 
                response != null && response.getRows() != null ? response.getRows().size() : 0);
                
            return response;
            
        } catch (HttpClientErrorException e) {
            logger.error("회의실 목록 조회 실패 - HTTP Client Error: {}", e.getMessage());
            throw new BackendApiException("회의실 목록 조회에 실패했습니다: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            logger.error("회의실 목록 조회 실패 - HTTP Server Error: {}", e.getMessage());
            throw new BackendApiException("회의실 목록 조회에 실패했습니다: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            logger.error("회의실 목록 조회 실패 - Network Error: {}", e.getMessage());
            throw new BackendApiException("회의실 목록 조회에 실패했습니다: 네트워크 문제가 발생했습니다.", e);
        } catch (Exception e) {
            logger.error("회의실 목록 조회 실패 - Unexpected Error: {}", e.getMessage());
            throw new BackendApiException("회의실 목록 조회에 실패했습니다: 알 수 없는 오류가 발생했습니다.", e);
        }
    }

    /**
     * 예약 가능한 회의실 목록을 조회합니다.
     * 
     * @param startDate 날짜 (yyyy-MM-dd 형식)
     * @param startTime 시작 시간 (HH:mm 형식)
     * @param endTime 종료 시간 (HH:mm 형식)
     * @return 예약 가능한 회의실 목록
     */
    public ListVO<OfficeDto> findAvailableRooms(String startDate, String startTime, String endTime) {
        logger.info("가용 회의실 조회 시작 - 날짜: {}, 시작시간: {}, 종료시간: {}", 
            startDate, startTime, endTime);
        
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("dt", startDate);
            form.add("bgnTime", startTime);
            form.add("endTime", endTime);
            form.add("whereType", "available");

            // API 호출
            ListVO<OfficeDto> response = restClient.post()
                    .uri("/com/smartofc/mtgTablet/selectMtgRmList.do")
                    .header("Referer", referer)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<ListVO<OfficeDto>>() {});

            logger.info("가용 회의실 조회 성공 - 회의실 수: {}", 
                response != null && response.getRows() != null ? response.getRows().size() : 0);
                
            return response;
            
        } catch (HttpClientErrorException e) {
            logger.error("가용 회의실 조회 실패 - HTTP Client Error: {}", e.getMessage());
            throw new BackendApiException("가용 회의실 조회에 실패했습니다: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            logger.error("가용 회의실 조회 실패 - HTTP Server Error: {}", e.getMessage());
            throw new BackendApiException("가용 회의실 조회에 실패했습니다: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            logger.error("가용 회의실 조회 실패 - Network Error: {}", e.getMessage());
            throw new BackendApiException("가용 회의실 조회에 실패했습니다: 네트워크 문제가 발생했습니다.", e);
        } catch (Exception e) {
            logger.error("가용 회의실 조회 실패 - Unexpected Error: {}", e.getMessage());
            throw new BackendApiException("가용 회의실 조회에 실패했습니다: 알 수 없는 오류가 발생했습니다.", e);
        }
    }

    /**
     * 내 예약 목록을 조회합니다.
     * 
     * @return 사용자의 예약 목록
     */
    public ListVO<OfficeDto> getMyReservations() {
        logger.info("내 예약 목록 조회 시작");
        
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("rsvctmId", UserContext.getEmpNo()); // 실제 구현 시 UserContext에서 가져오기
            form.add("scope", "mine");

            // API 호출
            ListVO<OfficeDto> response = restClient.post()
                    .uri("/com/smartofc/mtgTabletResve/selectResveList.do")
                    .header("Referer", referer)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<ListVO<OfficeDto>>() {});

            logger.info("내 예약 목록 조회 성공 - 예약 수: {}", 
                response != null && response.getRows() != null ? response.getRows().size() : 0);
                
            return response;
            
        } catch (HttpClientErrorException e) {
            logger.error("내 예약 목록 조회 실패 - HTTP Client Error: {}", e.getMessage());
            throw new BackendApiException("내 예약 목록 조회에 실패했습니다: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            logger.error("내 예약 목록 조회 실패 - HTTP Server Error: {}", e.getMessage());
            throw new BackendApiException("내 예약 목록 조회에 실패했습니다: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            logger.error("내 예약 목록 조회 실패 - Network Error: {}", e.getMessage());
            throw new BackendApiException("내 예약 목록 조회에 실패했습니다: 네트워크 문제가 발생했습니다.", e);
        } catch (Exception e) {
            logger.error("내 예약 목록 조회 실패 - Unexpected Error: {}", e.getMessage());
            throw new BackendApiException("내 예약 목록 조회에 실패했습니다: 알 수 없는 오류가 발생했습니다.", e);
        }
    }

    /**
     * 예약을 생성합니다.
     * 
     * @param meetingRoomId 회의실 ID
     * @param startDate 예약 날짜
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @param purpose 예약 목적
     * @return 예약 생성 결과
     */
    public ResultVO createReservation(String meetingRoomId, String startDate, String startTime, 
                                      String endTime, String purpose) {
        logger.info("회의실 예약 생성 시작 - 회의실 ID: {}, 날짜: {}, 시간: {}-{}", 
            meetingRoomId, startDate, startTime, endTime);
        
        try {
            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add("mtgrmId", meetingRoomId);
            form.add("dt", startDate);
            form.add("bgnTime", startTime);
            form.add("endTime", endTime);
            form.add("mtgSj", purpose);
            form.add("rsvctmId", UserContext.getEmpNo());
            form.add("lastUpdusrId", UserContext.getEmpNo());
            form.add("atdrnQty", "1");
            form.add("atdrnUserList", "[]");
            form.add("confmAt", "Y");
            form.add("secretAt", "N");
            form.add("resveSttusCd", "STT001");
            form.add("day", "[]");
            form.add("reptitUnitCd", "");

            // API 호출
            ResultVO response = restClient.post()
                    .uri("/com/smartofc/mtgTabletResve/insert.do")
                    .header("Referer", referer)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(form)
                    .retrieve()
                    .body(ResultVO.class);

            logger.info("회의실 예약 생성 성공 - 예약 ID: {}", 
                response != null ? "생성됨" : "없음");
                
            return response;
            
        } catch (HttpClientErrorException e) {
            logger.error("회의실 예약 생성 실패 - HTTP Client Error: {}", e.getMessage());
            if (e.getStatusCode().value() == 409) {
                throw new BackendApiException("선택한 시간대에 이미 예약된 회의실이 있습니다.", e);
            }
            throw new BackendApiException("회의실 예약 생성에 실패했습니다: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            logger.error("회의실 예약 생성 실패 - HTTP Server Error: {}", e.getMessage());
            throw new BackendApiException("회의실 예약 생성에 실패했습니다: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            logger.error("회의실 예약 생성 실패 - Network Error: {}", e.getMessage());
            throw new BackendApiException("회의실 예약 생성에 실패했습니다: 네트워크 문제가 발생했습니다.", e);
        } catch (Exception e) {
            logger.error("회의실 예약 생성 실패 - Unexpected Error: {}", e.getMessage());
            throw new BackendApiException("회의실 예약 생성에 실패했습니다: 알 수 없는 오류가 발생했습니다.", e);
        }
    }

    /**
     * 예약을 취소합니다.
     * 
     * @param reservationId 예약 ID
     * @return 예약 취소 결과
     */
    public ResultVO cancelReservation(String reservationId) {
        logger.info("회의실 예약 취소 시작 - 예약 ID: {}", reservationId);
        
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("resveId", reservationId);
            form.add("sttDiv", "resveCancel");

            // API 호출
            ResultVO response = restClient.post()
                    .uri("/com/smartofc/mtgTabletResve/updateSttus.do")
                    .header("Referer", referer)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(ResultVO.class);

            logger.info("회의실 예약 취소 성공");
                
            return response;
            
        } catch (HttpClientErrorException e) {
            logger.error("회의실 예약 취소 실패 - HTTP Client Error: {}", e.getMessage());
            throw new BackendApiException("회의실 예약 취소에 실패했습니다: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            logger.error("회의실 예약 취소 실패 - HTTP Server Error: {}", e.getMessage());
            throw new BackendApiException("회의실 예약 취소에 실패했습니다: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            logger.error("회의실 예약 취소 실패 - Network Error: {}", e.getMessage());
            throw new BackendApiException("회의실 예약 취소에 실패했습니다: 네트워크 문제가 발생했습니다.", e);
        } catch (Exception e) {
            logger.error("회의실 예약 취소 실패 - Unexpected Error: {}", e.getMessage());
            throw new BackendApiException("회의실 예약 취소에 실패했습니다: 알 수 없는 오류가 발생했습니다.", e);
        }
    }

    /**
     * Backend API 예외 클래스
     */
    public static class BackendApiException extends RuntimeException {
        public BackendApiException(String message) {
            super(message);
        }
        
        public BackendApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}