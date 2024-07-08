package com.SunuBtrust360_Enrol.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "revoke")
@Getter
@Setter
public class Revoke {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    private int reasonCode ;
    private String reasonText;
}
