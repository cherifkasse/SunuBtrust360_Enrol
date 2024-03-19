package com.SunuBtrust360_Enrol.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 05/12/2023/12/2023 - 11:09
 */
@Entity
@Table(name="application")
public class Application {
    @Id
    private Long id;
    private long idApp;
    private String nom;

    public Application(Long id, long id_app, String nom) {
        this.id = id;
        this.idApp = id_app;
        this.nom = nom;
    }

    public Application(long id_app, String nom) {
        this.idApp = id_app;
        this.nom = nom;
    }

    public Application() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdApp() {
        return idApp;
    }

    public void setIdApp(Long id_app) {
        this.idApp = id_app;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
