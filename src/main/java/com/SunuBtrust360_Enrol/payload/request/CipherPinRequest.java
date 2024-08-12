package com.SunuBtrust360_Enrol.payload.request;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 14/12/2023/12/2023 - 20:28
 */
public class CipherPinRequest {
    private String code_pin_encrypted;

    public CipherPinRequest(String code_pin_encrypted) {
        this.code_pin_encrypted = code_pin_encrypted;
    }

    public CipherPinRequest() {
    }

    public String getCode_pin_encrypted() {
        return code_pin_encrypted;
    }

    public void setCode_pin_encrypted(String code_pin_encrypted) {
        this.code_pin_encrypted = code_pin_encrypted;
    }
}
