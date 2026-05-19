package com.psnm.mcp.meetingroom.client.dto;

import java.util.List;

public class ListVO<T> {
    private List<T> rows;
    private Integer total;
    private String status;
    private String message;

    // Getters and setters
    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}