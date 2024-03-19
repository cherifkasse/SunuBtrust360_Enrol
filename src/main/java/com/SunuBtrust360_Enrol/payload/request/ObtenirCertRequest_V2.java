package com.SunuBtrust360_Enrol.payload.request;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 13/02/2024/02/2024 - 16:30
 */
@Getter
@Setter
public class ObtenirCertRequest_V2 {
    private String certificate_request;
    private String certificate_profile_name;
    private String end_entity_profile_name;
    private String certificate_authority_name;
    private boolean include_chain;
    private String username;
    private String password;
}
