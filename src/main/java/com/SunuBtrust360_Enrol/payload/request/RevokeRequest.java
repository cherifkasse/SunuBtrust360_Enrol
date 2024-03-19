package com.SunuBtrust360_Enrol.payload.request;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 05/12/2023/12/2023 - 15:27
 */
public class RevokeRequest {
    private int reason_code;
    private boolean delete;

    public RevokeRequest() {
    }

    public int getReason_code() {
        return reason_code;
    }

    public void setReason_code(int reason_code) {
        this.reason_code = reason_code;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }
}
