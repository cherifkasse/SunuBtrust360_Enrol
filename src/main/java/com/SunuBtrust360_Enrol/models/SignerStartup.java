package com.SunuBtrust360_Enrol.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 10/09/2024/09/2024 - 18:36
 */
@Entity
@Getter
@Setter
public class SignerStartup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer idWorker;
    private String codePin;
}
