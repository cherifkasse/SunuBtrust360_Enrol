package com.SunuBtrust360_Enrol.payload.response;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 22/08/2023 - 14:45
 */

public class MessageResponse {
    private String message;
    public MessageResponse(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
