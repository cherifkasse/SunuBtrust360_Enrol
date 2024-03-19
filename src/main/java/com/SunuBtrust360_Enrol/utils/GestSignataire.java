package com.SunuBtrust360_Enrol.utils;

import com.SunuBtrust360_Enrol.models.Signataire;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 21/09/2023/09/2023 - 15:00
 */
public class GestSignataire {
    public String Enregistrer_Enroler(Signataire signer)  {
        String succes_resp = "";
        long signer_id = rechercher_email(signer.getEmail(), signer.getCleDeSignature());

        if (signer_id > 0) {

            succes_resp = "" + signer_id;
        } else {
            try {
                GestBD gd = new GestBD();
                gd.OpenConnection();
                gd.insertSignataire(signer);
                gd.CloseConnection();
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
            }
            System.out.println("succes_resp: " + succes_resp);
        }


        return succes_resp;
    }
    public long rechercher_email(String email, String cle) {
        GestBD gd = new GestBD();
        gd.OpenConnection();
        long idUser = gd.rechercherSignerAvecEmail(email, cle);
        gd.CloseConnection();
        return idUser;
    }

    public long retrouverIdApp(String nom_app){
        GestBD gd = new GestBD();
        gd.OpenConnection();
        long id_app= gd.getIdApp(nom_app);
        gd.CloseConnection();
        return id_app;
    }

    public void affecterGroupe(String cle,String nom){
        GestBD gd = new GestBD();
        gd.OpenConnection();
        //long idapp=retrouverIdApp(nom);
        gd.updateGroupe(cle,nom);
        gd.CloseConnection();
    }

    public void updateRenouveler(String cle,String pin,String dateRenew, String exp){
        GestBD gd = new GestBD();
        gd.OpenConnection();
        //long idapp=retrouverIdApp(nom);
        gd.updateRenouveler(cle,pin,dateRenew,exp);
        gd.CloseConnection();
    }

    public long deleteSigner(String cle){
        GestBD gd = new GestBD();
        gd.OpenConnection();
        long nbre=gd.supSignataire(cle);
        gd.CloseConnection();
        return nbre;

    }
}
