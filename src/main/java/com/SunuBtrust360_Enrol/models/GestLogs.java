package com.SunuBtrust360_Enrol.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gestion_logs")
@Getter
@Setter
public class GestLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String auteur;
    private String role;
    private String action;
    private String message;
    private String date;
    private String email;
}
