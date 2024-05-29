package com.SunuBtrust360_Enrol.controller;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 27/05/2024/05/2024 - 14:47
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {
    public static void main(String[] args) {
        System.out.println(appelApiEncrypt("488084"));
    }

    public static StringBuilder appelApiEncrypt(String code) {
        String url = "http://10.10.1.13:8080/sunubtrust360/signataire/encrypt/" + code;
        StringBuilder pinCrypte = new StringBuilder();
        try {

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();


            con.setRequestMethod("POST");


            con.setRequestProperty("Content-Type", "application/json");


            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write("".getBytes());
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();
           // System.out.println("POST Response Code :: " + responseCode);


            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String ligneEntree;

                while ((ligneEntree = in.readLine()) != null) {
                    pinCrypte.append(ligneEntree);
                }
                in.close();

                //System.out.println("Response: " + pinCrypte);
            } else {
                System.out.println("Erreur");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pinCrypte;
    }
}

