package com.SunuBtrust360_Enrol.payload.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 26/10/2023/10/2023 - 12:32
 */
public class SignataireRequest {
    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private String application_rattachee;

    @JsonIgnore
    private String nom_application;

    @Schema(description = "Le nom du signataire")
    private String nomSignataire;

    @Schema(description = "Le code pin du signataire")
    private String code_pin;

    @Schema(description = "L'email du signataire")
    private String email;
    @Schema(description = "La categorie du signataire")
    private String categorie ;

    @Schema(description = "Nom de l'entreprise du signataire")
    private String nom_entreprise;

    @JsonIgnore
    private String cle_de_signature;

    @JsonIgnore
    private String dateCreation;
    @JsonIgnore
    private String date_expiration;

    @Schema(description = "Nom de l'application appelante")
    private String trustedApp;

    @JsonIgnore
    private String certificate_request;
    @JsonIgnore
    private String certificate_profile_name;
    @JsonIgnore
    private String end_entity_profile_name;
    @JsonIgnore
    private String certificate_authority_name;
    @JsonIgnore
    private String username;
    @JsonIgnore
    private String password;

    @Schema(description = "Nom de le numéro de CNI ou du passport du signataire")
    private String cniPassport;

    public SignataireRequest() {
    }

    public SignataireRequest(Integer id, String application_rattachee, String nom_application, String prenom_nom, String code_pin, String email, String categorie, String nom_entreprise, String cle_de_signature, String date_creation, String date_expiration, String certificate_request, String certificate_profile_name, String end_entity_profile_name, String certificate_authority_name, String username, String password) {
        this.id = id;
        this.application_rattachee = application_rattachee;
        this.nom_application = nom_application;
        this.nomSignataire = prenom_nom;
        this.code_pin = code_pin;
        this.email = email;
        this.categorie = categorie;
        this.nom_entreprise = nom_entreprise;
        this.cle_de_signature = cle_de_signature;
        this.dateCreation = date_creation;
        this.date_expiration = date_expiration;
        this.certificate_request = certificate_request;
        this.certificate_profile_name = certificate_profile_name;
        this.end_entity_profile_name = end_entity_profile_name;
        this.certificate_authority_name = certificate_authority_name;
        this.username = username;
        this.password = password;
    }

    public SignataireRequest(String trustedApp, String prenom_nom, String code_pin, String email, String categorie, String nom_entreprise) {
        this.trustedApp=trustedApp;
        this.nomSignataire = prenom_nom;
        this.code_pin = code_pin;
        this.email = email;
        this.categorie = categorie;
        this.nom_entreprise = nom_entreprise;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCertificate_request() {
        return certificate_request;
    }

    public void setCertificate_request(String certificate_request) {
        this.certificate_request = certificate_request;
    }

    public String getCertificate_profile_name() {
        return certificate_profile_name;
    }

    public void setCertificate_profile_name(String certificate_profile_name) {
        this.certificate_profile_name = certificate_profile_name;
    }

    public String getTrustedApp() {
        return trustedApp;
    }

    public void setTrustedApp(String trustedApp) {
        this.trustedApp = trustedApp;
    }

    public String getEnd_entity_profile_name() {
        return end_entity_profile_name;
    }

    public void setEnd_entity_profile_name(String end_entity_profile_name) {
        this.end_entity_profile_name = end_entity_profile_name;
    }

    public String getCertificate_authority_name() {
        return certificate_authority_name;
    }

    public void setCertificate_authority_name(String certificate_authority_name) {
        this.certificate_authority_name = certificate_authority_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNomSignataire() {
        return nomSignataire;
    }

    public void setNomSignataire(String nomSignataire) {
        this.nomSignataire = nomSignataire;
    }

    public String getCode_pin() {
        return code_pin;
    }

    public void setCode_pin(String code_pin) {
        this.code_pin = code_pin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCniPassport() {
        return cniPassport;
    }

    public void setCniPassport(String cniPassport) {
        this.cniPassport = cniPassport;
    }

    public String getApplication_rattachee() {
        return application_rattachee;
    }

    public void setApplication_rattachee(String application_rattachee) {
        this.application_rattachee = application_rattachee;
    }

    public String getNom_application() {
        return nom_application;
    }

    public void setNom_application(String nom_application) {
        this.nom_application = nom_application;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getDate_expiration() {
        return date_expiration;
    }

    public void setDate_expiration(String date_expiration) {
        this.date_expiration = date_expiration;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getNom_entreprise() {
        return nom_entreprise;
    }

    public void setNom_entreprise(String nom_entreprise) {
        this.nom_entreprise = nom_entreprise;
    }

    public String getCle_de_signature() {
        return cle_de_signature;
    }

    public void setCle_de_signature(String cle_de_signature) {
        this.cle_de_signature = cle_de_signature;
    }
}
