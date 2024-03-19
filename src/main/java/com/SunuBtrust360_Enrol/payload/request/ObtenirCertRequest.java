package com.SunuBtrust360_Enrol.payload.request;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 23/11/2023/11/2023 - 09:16
 */
public class ObtenirCertRequest {
    private String certificate_request;
    private String certificate_profile_name;
    private String end_entity_profile_name;
    private String certificate_authority_name;
    private boolean include_chain;
    private String username;
    private String password;

    public boolean isInclude_chain() {
        return include_chain;
    }

    public void setInclude_chain(boolean include_chain) {
        this.include_chain = include_chain;
    }

    public String getCertificate_request() {
        return certificate_request;
    }

    public void setCertificate_request(String certificate_request) {
        this.certificate_request = certificate_request;
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

    public String getCertificate_authority_name() {
        return certificate_authority_name;
    }

    public void setCertificate_authority_name(String certificate_authority_name) {
        this.certificate_authority_name = certificate_authority_name;
    }

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
}
