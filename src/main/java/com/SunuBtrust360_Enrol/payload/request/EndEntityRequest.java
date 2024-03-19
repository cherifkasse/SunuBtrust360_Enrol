package com.SunuBtrust360_Enrol.payload.request;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 31/10/2023/10/2023 - 14:14
 */
public class EndEntityRequest {
    private String username;
    private String password;
    private String subject_dn;
    private String ca_name;
    private String certificate_profile_name;
    private String end_entity_profile_name;
    private String token;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSubject_dn() {
        return subject_dn;
    }

    public void setSubject_dn(String subject_dn) {
        this.subject_dn = subject_dn;
    }

    public String getCa_name() {
        return ca_name;
    }

    public void setCa_name(String ca_name) {
        this.ca_name = ca_name;
    }

    public String getCertificate_profile_name() {
        return certificate_profile_name;
    }

    public void setCertificate_profile_name(String certificate_profile_name) {
        this.certificate_profile_name = certificate_profile_name;
    }

    public String getEnd_entity_profile_name() {
        return end_entity_profile_name;
    }

    public void setEnd_entity_profile_name(String end_entity_profile_name) {
        this.end_entity_profile_name = end_entity_profile_name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
