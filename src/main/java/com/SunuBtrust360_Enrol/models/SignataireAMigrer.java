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
 * @created 15/05/2024/05/2024 - 12:50
 */
@Entity
@Getter
@Setter
public class SignataireAMigrer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer oldid;

    private Integer newid;

    private String signaturekey;

    public SignataireAMigrer() {
    }
}
