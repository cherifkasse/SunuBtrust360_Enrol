package com.SunuBtrust360_Enrol.payload.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 13/02/2024/02/2024 - 16:18
 */
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignataireRequest_V2 {

    @NotBlank(message = "Le champ 'nomSignataire' est obligatoire")
    private String nomSignataire;

    @NotBlank(message = "Le champ 'cni' est obligatoire")
    private String cni;

    @NotBlank(message = "Le champ 'telephone' est obligatoire")
    private String telephone;


    public SignataireRequest_V2() {

    }

    public SignataireRequest_V2(String nomSignataire, String cni, String telephone) {
        this.nomSignataire = nomSignataire;
        this.cni = cni;
        this.telephone = telephone;
    }

    public SignataireRequest_V2(String nomSignataire, String cni) {
        this.nomSignataire = nomSignataire;
        this.cni = cni;

    }

}
