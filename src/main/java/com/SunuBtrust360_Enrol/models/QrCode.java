package com.SunuBtrust360_Enrol.models;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;



@Entity
@Getter
@Setter
public class QrCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomSignataire;
    private String cni;
    private String telephone;
    private String signerKey;
    private String workerName;
    private String dateSignature;
    private String nomDocument;



}
