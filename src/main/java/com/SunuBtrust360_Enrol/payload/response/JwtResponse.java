package com.SunuBtrust360_Enrol.payload.response;

import java.util.List;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 22/08/2023 - 14:39
 */

public class JwtResponse {

    private String token;
    private String type = "Bearer";

    private long id;

    private String username;
    private String email;
    private List<String> roles;
    public JwtResponse(String accessToken, Long id, String username,String email,List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username= username;
        this.email=email;
        this.roles = roles;
    }

    public String getAccessToken() {
        return token;
    }

    public void setAccessToken(String token) {
        this.token = token;
    }

    public String getTokenType(){
        return type;
    }

    public void setTokenType(String tokenType) {
        this.token = tokenType;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public String getUsername() {
        return username;
    }

    public void setEmail(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getEmail() {
        return email;
    }
}
