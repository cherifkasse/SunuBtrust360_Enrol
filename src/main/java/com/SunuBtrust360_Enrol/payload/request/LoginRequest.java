package com.SunuBtrust360_Enrol.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 22/08/2023 - 14:21
 */

public class LoginRequest {
    @NotBlank
    private String email;

    @NotBlank
    @Size(max = 120,min=5)
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
