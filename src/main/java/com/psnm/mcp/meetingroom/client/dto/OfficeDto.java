package com.psnm.mcp.meetingroom.client.dto;

public class OfficeDto {
    private String offmId;          // 사무실 ID (PK)
    private String offmNm;          // 사무실 이름 ← 실제 응답 필드명 확인 후 @JsonProperty 추가

    // 층/건물 정보 (Claude 자연어 매핑에 필수)
    private String flor;            // 층 번호 (예: "3", "B1")
    private String florNm;          // 층 이름 (예: "3층", "지하1층")
    private String buldNm;          // 건물명 (예: "본사", "A동")
    private String lcCd;            // 위치 코드
    private String lcNm;            // 위치 이름

    // 부가 정보
    private String useAt;           // 사용 여부 ("Y"/"N")
    private String atchFileId;      // 첨부파일 ID (이미지)

    // Getters and setters
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

    public String getFlor() {
        return flor;
    }

    public void setFlor(String flor) {
        this.flor = flor;
    }

    public String getFlorNm() {
        return florNm;
    }

    public void setFlorNm(String florNm) {
        this.florNm = florNm;
    }

    public String getBuldNm() {
        return buldNm;
    }

    public void setBuldNm(String buldNm) {
        this.buldNm = buldNm;
    }

    public String getLcCd() {
        return lcCd;
    }

    public void setLcCd(String lcCd) {
        this.lcCd = lcCd;
    }

    public String getLcNm() {
        return lcNm;
    }

    public void setLcNm(String lcNm) {
        this.lcNm = lcNm;
    }

    public String getUseAt() {
        return useAt;
    }

    public void setUseAt(String useAt) {
        this.useAt = useAt;
    }

    public String getAtchFileId() {
        return atchFileId;
    }

    public void setAtchFileId(String atchFileId) {
        this.atchFileId = atchFileId;
    }
}