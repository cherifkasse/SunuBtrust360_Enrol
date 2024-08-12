package com.SunuBtrust360_Enrol.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "liste_revocation")
@Getter
@Setter
public class ListeRevocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer idSignataire;
    private String nomSignataire;
    private String signerKey;
    private int idMotif;
    private String motif;
    private String autreMotif;
    private String nomEntreprise;
    private String DateRevocation;
    private String status;
}
