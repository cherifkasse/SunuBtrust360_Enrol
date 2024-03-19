package com.SunuBtrust360_Enrol.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.Set;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 22/08/2023 - 14:36
 */
@Getter

public class SignupRequest {
    @NotBlank
    @Size(min=3, max=50)
    private String username;
    @NotBlank
    @Size(min=3, max=50)
    private String email;

    @NotBlank
    @Size(min=6, max=50)
    private String password;

    private Set<String> role;



    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(Set<String> role) {
        this.role = role;
    }
}
