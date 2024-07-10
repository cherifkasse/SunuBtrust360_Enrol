package com.SunuBtrust360_Enrol.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 13/02/2024/02/2024 - 15:56
 */
@Entity
@Table(name="signer",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "cni")
        }
)
@Getter
@Setter
public class Signataire_V2 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Le champ 'nomSignataire' est obligatoire")
    private String nomSignataire;

    @NotBlank(message = "Le champ 'cni' est obligatoire")
    private String cni;

    private String codePin;

    private String signerKey;

    @Column(name = "id_application", nullable = true)
    private Integer idApplication;

    private String dateCreation;
    private String dateRenouvellement;

    private String dateExpiration;

    private String nomWorker;

    @NotBlank(message = "Le champ 'telephone' est obligatoire")
    @Column(nullable = true)
    private String telephone;
    public Signataire_V2() {
    }

}
