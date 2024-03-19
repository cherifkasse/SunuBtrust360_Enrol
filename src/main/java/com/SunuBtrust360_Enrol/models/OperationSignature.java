package com.SunuBtrust360_Enrol.models;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 05/03/2024/03/2024 - 11:49
 */

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="operationsSignature")
public class OperationSignature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer idSigner;
    private String codePin;

    private String signerKey;
    private String dateOperation;


}
