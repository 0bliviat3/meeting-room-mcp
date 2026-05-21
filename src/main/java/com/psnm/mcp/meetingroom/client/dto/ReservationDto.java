package com.psnm.mcp.meetingroom.client.dto;

public class ReservationDto {

    // ── 예약 식별 (취소 시 필수) ──────────────────────────
    private String resveId;         // 예약 ID (취소 API의 resveId)
    private String psnetEventId;    // PSNET 이벤트 ID (취소 API의 psnetEventId, null 가능)

    // ── 회의 기본 정보 ─────────────────────────────────────
    private String mtgSj;           // 회의 제목 (예: "그룹웨어 미팅")
    private String dt;              // 예약 날짜 (yyyy-MM-dd)
    private String bgnTime;         // 시작 시간 (HH:mm)
    private String endTime;         // 종료 시간 (HH:mm)
    private String mtgCtt;          // 회의 내용 (null 가능)
    private Integer atdrnQty;       // 참석 인원 수
    private String secretAt;        // 비밀 여부 ("Y"/"N")

    // ── 예약 상태 ──────────────────────────────────────────
    private String resveSttusCd;    // 예약 상태 코드 (STT001: 예약완료, STT002: 취소 등)
    private String confmAt;         // 확인 여부 ("Y"/"N")

    // ── 회의실 정보 ────────────────────────────────────────
    private String mtgrmId;         // 회의실 ID
    private String mtgrmNm;         // 회의실 이름 (예: "2회의실(화상)")
    private String videoMtgrmAt;    // 화상회의 가능 여부 ("Y"/"N")
    private String mtgrmSeCd;       // 회의실 구분코드 (MTG001: 일반, MTG002: 대형)

    // ── 사무실(층/건물) 정보 ───────────────────────────────
    private String offmId;          // 사무실 ID
    private String offmNm;          // 사무실 이름 (예: "마포티타운 3F")

    // ── 예약자 정보 ────────────────────────────────────────
    private String rsvctmId;        // 예약자 사번
    private String rsvctmNm;        // 예약자 이름 (예: "송재완")
    private String userInfo;        // 예약자 요약 (예: "송재완(BP231205) / 현장대리인(그룹웨어)")

    // ── 부가 정보 ──────────────────────────────────────────
    private String colorCd;         // 캘린더 색상 코드 (예: "MTR003")

    // Getters and setters
    public String getResveId() {
        return resveId;
    }

    public void setResveId(String resveId) {
        this.resveId = resveId;
    }

    public String getPsnetEventId() {
        return psnetEventId;
    }

    public void setPsnetEventId(String psnetEventId) {
        this.psnetEventId = psnetEventId;
    }

    public String getMtgSj() {
        return mtgSj;
    }

    public void setMtgSj(String mtgSj) {
        this.mtgSj = mtgSj;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getBgnTime() {
        return bgnTime;
    }

    public void setBgnTime(String bgnTime) {
        this.bgnTime = bgnTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getMtgCtt() {
        return mtgCtt;
    }

    public void setMtgCtt(String mtgCtt) {
        this.mtgCtt = mtgCtt;
    }

    public Integer getAtdrnQty() {
        return atdrnQty;
    }

    public void setAtdrnQty(Integer atdrnQty) {
        this.atdrnQty = atdrnQty;
    }

    public String getSecretAt() {
        return secretAt;
    }

    public void setSecretAt(String secretAt) {
        this.secretAt = secretAt;
    }

    public String getResveSttusCd() {
        return resveSttusCd;
    }

    public void setResveSttusCd(String resveSttusCd) {
        this.resveSttusCd = resveSttusCd;
    }

    public String getConfmAt() {
        return confmAt;
    }

    public void setConfmAt(String confmAt) {
        this.confmAt = confmAt;
    }

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

    public String getOffmId() {
        return offmId;
    }

    public void setOffmId(String offmId) {
        this.offmId = offmId;
    }

    public String getOffmNm() {
        return offmNm;
    }

    public void setOffmNm(String offmNm) {
        this.offmNm = offmNm;
    }

    public String getRsvctmId() {
        return rsvctmId;
    }

    public void setRsvctmId(String rsvctmId) {
        this.rsvctmId = rsvctmId;
    }

    public String getRsvctmNm() {
        return rsvctmNm;
    }

    public void setRsvctmNm(String rsvctmNm) {
        this.rsvctmNm = rsvctmNm;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getColorCd() {
        return colorCd;
    }

    public void setColorCd(String colorCd) {
        this.colorCd = colorCd;
    }
}