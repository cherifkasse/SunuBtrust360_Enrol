package com.SunuBtrust360_Enrol.payload.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter
@Setter
public class DemandeRevocationRequest {
    private Integer id;
    private Integer idSignataire;
    private String nomSignataire;
    private String signerKey;
    private String nomEntreprise;
    private String motif;
    private String autreMotif;

    public DemandeRevocationRequest() {
    }
}
