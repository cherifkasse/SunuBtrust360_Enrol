package com.SunuBtrust360_Enrol.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "demande_revocation")
@Getter
@Setter
public class DemandeRevocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer idSignataire;
    private String nomSignataire;
    private String signerKey;
    private String nomEntreprise;
    private int idMotif;
    private String motif;
    private String autreMotif;

}
