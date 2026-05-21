package com.psnm.mcp.meetingroom.client.dto;

public class MeetingRoomDto {
    private String mtgrmId;      // 회의실 ID
    private String mtgrmNm;      // 회의실 이름 (예: "1회의실", "프로젝트룸(화상)")
    private Integer seatQty;     // 좌석 수
    private String offmId;       // 소속 사무실(건물/층) ID
    private String videoMtgrmAt; // 화상회의 가능 여부 ("Y"/"N")
    private String mtgrmSeCd;    // 회의실 구분코드 (MTG001: 일반, MTG002: 대형/특수)
    private Integer alignNo;     // 정렬 순서
    private String useAt;        // 사용 여부 ("Y"/"N")
    private String fxtrsInfo;    // 비품 정보
    private String atchFileId;   // 첨부파일 ID (이미지)

    // Getters and setters
    public String getMtgrmId() {
        return mtgrmId;
    }

    public void setMtgrmId(String mtgrmId) {
        this.mtgrmId = mtgrmId;
    }

    public String getMtgrmNm() {
        return mtgrmNm;
    }

    public void setMtgrmNm(String mtgrmNm) {
        this.mtgrmNm = mtgrmNm;
    }

    public Integer getSeatQty() {
        return seatQty;
    }

    public void setSeatQty(Integer seatQty) {
        this.seatQty = seatQty;
    }

    public String getOffmId() {
        return offmId;
    }

    public void setOffmId(String offmId) {
        this.offmId = offmId;
    }

    public String getVideoMtgrmAt() {
        return videoMtgrmAt;
    }

    public void setVideoMtgrmAt(String videoMtgrmAt) {
        this.videoMtgrmAt = videoMtgrmAt;
    }

    public String getMtgrmSeCd() {
        return mtgrmSeCd;
    }

    public void setMtgrmSeCd(String mtgrmSeCd) {
        this.mtgrmSeCd = mtgrmSeCd;
    }

    public Integer getAlignNo() {
        return alignNo;
    }

    public void setAlignNo(Integer alignNo) {
        this.alignNo = alignNo;
    }

    public String getUseAt() {
        return useAt;
    }

    public void setUseAt(String useAt) {
        this.useAt = useAt;
    }

    public String getFxtrsInfo() {
        return fxtrsInfo;
    }

    public void setFxtrsInfo(String fxtrsInfo) {
        this.fxtrsInfo = fxtrsInfo;
    }

    public String getAtchFileId() {
        return atchFileId;
    }

    public void setAtchFileId(String atchFileId) {
        this.atchFileId = atchFileId;
    }
}