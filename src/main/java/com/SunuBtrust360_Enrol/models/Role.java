package com.SunuBtrust360_Enrol.models;

import jakarta.persistence.*;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 21/08/2023 - 16:35
 */
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ERole name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ERole getName() {
        return name;
    }

    public void setName(ERole name) {
        this.name = name;
    }
}
