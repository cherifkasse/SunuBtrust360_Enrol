package com.SunuBtrust360_Enrol.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 15/02/2024/02/2024 - 13:58
 */
@Entity
@Table(name="depot_justificatif_signataire")
@Getter
@Setter
public class PieceIdentite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer idSignataire;

    @Lob
    private byte[] scan;

}
