package com.psnm.mcp.meetingroom.client.dto;

public class OfficeDto {
    private String offmId;
    private String offmNm;
    private String atchFileId;

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

    public String getAtchFileId() {
        return atchFileId;
    }

    public void setAtchFileId(String atchFileId) {
        this.atchFileId = atchFileId;
    }
}