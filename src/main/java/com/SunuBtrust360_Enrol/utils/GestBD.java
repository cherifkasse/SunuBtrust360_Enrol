package com.SunuBtrust360_Enrol.utils;

import com.SunuBtrust360_Enrol.models.Signataire;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 21/09/2023/09/2023 - 13:06
 */
public class GestBD {
    Properties prop = null;
    protected Connection conn = null;
    //String connectionUrl = prop.getProperty("urlDB");
    public GestBD() {

        try (InputStream input = GestBD.class.getClassLoader().getResourceAsStream("configWin.properties")) {

            prop = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            //load a properties file from class path, inside static method
            prop.load(input);


        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }
    public void OpenConnection(){
        //String connectionUrl = prop.getProperty("urlDB");

        try{
            conn = DriverManager.getConnection(prop.getProperty("urlDB"));
            System.out.println("Connection to Sqlite has been established.");


        } catch (SQLException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();

        }
    }

    public void CloseConnection() {
        try {
            if (!conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();

        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {

                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();

            }
        }
    }
    public int insertSignataire(Signataire signer) {


        try {

            String sqlParam = "INSERT INTO signataire (application_rattachee,categorie,cle_de_signature,code_pin,date_creation,date_expiration,email,nom_application,nom_entreprise,prenom_nom) VALUES (?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement  stmt = conn.prepareStatement(sqlParam);
            //stmt.setString(1, String.format("%1$s", signer.getApplication_rattachee()));
            stmt.setString(2, String.format("%1$s", signer.getCategorie()));
            stmt.setString(3, String.format("%1$s", signer.getCleDeSignature()));
            stmt.setInt(4, Integer.parseInt(String.format("%1$d", signer.getCode_pin())));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateCreation = (Date) sdf.parse(signer.getDateCreation());
            Date dateExpiration = (Date) sdf.parse(signer.getDate_expiration());
            stmt.setString(5, String.format("%1$s", dateCreation));
            stmt.setString(6, String.format("%1$s", dateExpiration));
            stmt.setString(7,String.format("%1$s", signer.getEmail()));
            stmt.setString(8, String.format("%1$s", signer.getNomApplication()));
            stmt.setString(9, String.format("%1$s", signer.getNomEntreprise()));
            stmt.setString(10, String.format("%1$s", signer.getNomSignataire()));

            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return 1;
    }

    public long rechercherSignerAvecEmail(String email, String cle) {

        long resultat = 0;
        try {

            String sqlParam = "SELECT id  FROM signataire WHERE email = ? and upper(cle_de_signature) = ?";

            PreparedStatement stmt = conn.prepareStatement(sqlParam);
            stmt.setString(1, email.trim());
            stmt.setString(2, cle.trim().toUpperCase());


            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                resultat = rs.getLong("id");
                System.out.println("Id signataire:"+resultat);
            }
            stmt.close();
            conn.close();
            return resultat;

        } catch (SQLException ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
        }

        return resultat;
    }

    public void updateGroupe(String cle,String nom) {

        try (Connection conn = DriverManager.getConnection(prop.getProperty("urlDB"))) {
            if (conn != null) {
                System.out.println("Connexion à SQLite réussie.");

                // Exemple de requête SQL paramétrée
                String sqlParam = "update signataire set nom_application = ? where cle_de_signature = ?";

                try ( PreparedStatement stmt = conn.prepareStatement(sqlParam)) {
                    // Remplacer les paramètres par les valeurs réelles
                    stmt.setString(2, String.format("%1$s", cle));
                    stmt.setString(1, String.format("%1$s", nom));
                    stmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public long getIdApp(String nom){
        long id_app=0;
        try (Connection conn = DriverManager.getConnection(prop.getProperty("urlDB"))) {
            if (conn != null) {
                System.out.println("Connexion à SQLite réussie.");

                // Exemple de requête SQL paramétrée
                String sqlParam = "select id_app from application WHERE nom =?";

                try (PreparedStatement stmt = conn.prepareStatement(sqlParam)) {
                    // Remplacer les paramètres par les valeurs réelles
                    stmt.setString(1, String.format("%1$s", nom));

                    // Exécuter la requête
                    ResultSet resultSet = stmt.executeQuery();

                    // Traiter les résultats
                    while (resultSet.next()) {
                        id_app = resultSet.getLong("id_app");
                    }

                    // Vérifier le nombre de lignes affectées

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id_app;
    }

    public long supSignataire(String cle){
        long id_app=0;
        try (Connection conn = DriverManager.getConnection(prop.getProperty("urlDB"))) {
            if (conn != null) {
                System.out.println("Connexion à SQLite réussie.");

                // Exemple de requête SQL paramétrée
                String sqlParam = "delete from signataire WHERE cle_de_signature =?";

                try (PreparedStatement stmt = conn.prepareStatement(sqlParam)) {
                    // Remplacer les paramètres par les valeurs réelles
                    stmt.setString(1, String.format("%1$s", cle));

                    // Exécuter la requête
                    stmt.executeUpdate();
                    id_app=1;
                    // Traiter les résultats


                    // Vérifier le nombre de lignes affectées

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id_app;
    }

    public void updateRenouveler(String cle,String pin, String dateRenew, String exp) {

        try (Connection conn = DriverManager.getConnection(prop.getProperty("urlDB"))) {
            if (conn != null) {
                System.out.println("Connexion à SQLite réussie.");

                // Exemple de requête SQL paramétrée
                String sqlParam = "update signataire set code_pin = ?, date_renouvellement=?, date_expiration=?  where cle_de_signature = ?";

                try ( PreparedStatement stmt = conn.prepareStatement(sqlParam)) {
                    // Remplacer les paramètres par les valeurs réelles
                    stmt.setString(4, String.format("%1$s", cle));
                    stmt.setString(1, String.format("%1$s", pin));
                    stmt.setString(2, String.format("%1$s", dateRenew));
                    stmt.setString(3, String.format("%1$s", exp));
                    stmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void updateRenouveler2(String cle,String pin, String dateRenew, String exp) {

        try (Connection conn = DriverManager.getConnection(prop.getProperty("urlDB"))) {
            if (conn != null) {
                System.out.println("Connexion à SQLite réussie.");

                // Exemple de requête SQL paramétrée
                String sqlParam = "update signer set code_pin = ?, date_renouvellement=?, date_expiration=?  where signer_key = ?";

                try ( PreparedStatement stmt = conn.prepareStatement(sqlParam)) {
                    // Remplacer les paramètres par les valeurs réelles
                    stmt.setString(4, String.format("%1$s", cle));
                    stmt.setString(1, String.format("%1$s", pin));
                    stmt.setString(2, String.format("%1$s", dateRenew));
                    stmt.setString(3, String.format("%1$s", exp));
                    stmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
