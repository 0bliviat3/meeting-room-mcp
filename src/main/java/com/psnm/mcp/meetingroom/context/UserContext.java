package com.psnm.mcp.meetingroom.context;

public class UserContext {
    private static final ThreadLocal<String> EMP_NO = new ThreadLocal<>();

    public static void setEmpNo(String empNo) {
        EMP_NO.set(empNo);
    }

    public static String getEmpNo() {
        return EMP_NO.get();
    }

    public static void clear() {
        EMP_NO.remove();
    }
}