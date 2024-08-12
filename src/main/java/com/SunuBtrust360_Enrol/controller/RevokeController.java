package com.SunuBtrust360_Enrol.controller;

import com.SunuBtrust360_Enrol.config.CustomHttpRequestFactory;
import com.SunuBtrust360_Enrol.models.*;
import com.SunuBtrust360_Enrol.payload.request.DemandeRevocationRequest;
import com.SunuBtrust360_Enrol.payload.request.RevokeRequest;
import com.SunuBtrust360_Enrol.repository.*;
import com.SunuBtrust360_Enrol.wsdl.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@RestController
@RequestMapping("/v0.0.2/revoke/")
public class RevokeController {
    AdminWS port = null;
    Properties prop = null;
    Logger log = null;
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(RestController.class);
    @Autowired
    RevokeRepository revokeRepository;

    @Autowired
    SignataireRepository signataireRepository;

    @Autowired
    WorkerRepository workerRepository;

    @Autowired
    DemandeRevocationRepository demandeRevocationRepository;

    @Autowired
    ListeRevocationRepository listeRevocationRepository;

    CustomHttpRequestFactory customFact = new CustomHttpRequestFactory();

    @Autowired
    private final GestLogsRepository gestLogsRepository;

    public RevokeController(RevokeRepository revokeRepository, DemandeRevocationRepository demandeRevocationRepository,ListeRevocationRepository listeRevocationRepository,SignataireRepository signataireRepository,WorkerRepository workerRepository,GestLogsRepository gestLogsRepository) {
        this.revokeRepository = revokeRepository;
        this.workerRepository = workerRepository;
        this.demandeRevocationRepository = demandeRevocationRepository;
        this.listeRevocationRepository = listeRevocationRepository;
        this.signataireRepository = signataireRepository;
        this.gestLogsRepository = gestLogsRepository;
        //log = (Logger) LogManager.getLogger(RevokeController.class);
        //log.debug("Registration class constructor");
        try (InputStream input = SignataireController.class.getClassLoader().getResourceAsStream("configWin.properties")) {

            prop = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            //load a properties file from class path, inside static method
            prop.load(input);
            //log.debug("Registration class constructor: prop.loaded");


            //log.debug("Registration class constructor: Identifier_lApplication()");

        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }

    /**
     * Methode pour la gestion des logs
     * **/
    public void gestLogs(HttpServletRequest httpServletRequest, String action, String message){
        GestLogs gestLogs = new GestLogs();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date_logs = new Date();
        String role = "OPERATEUR(TRICE)";
        if(httpServletRequest.getAttribute("role").toString().contains("ADMIN")){
            role = "ADMINISTRATEUR";
        }
        gestLogs.setAuteur(httpServletRequest.getAttribute("email").toString());
        gestLogs.setDate(sdf.format(date_logs));
        gestLogs.setEmail(httpServletRequest.getAttribute("username").toString());
        gestLogs.setRole(role);
        gestLogs.setAction(action);
        gestLogs.setMessage(message);
        gestLogsRepository.save(gestLogs);
    }

    @GetMapping("allReasons")
    public List<Revoke> getAllReasonRevoke(){
        return this.revokeRepository.findAll();
    }

    @GetMapping("allDemandes")
    public List<DemandeRevocation> getAllDemandes(){
        return this.demandeRevocationRepository.findAll();
    }

    @GetMapping("listeRevoke")
    public List<ListeRevocation> getlisteRevoke(){
        return this.listeRevocationRepository.findAll();
    }


    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("insertDemandeRevoke")
    public ResponseEntity<?> insertDemandeRevoke(@RequestBody DemandeRevocationRequest demandeRevocationRequest, HttpServletRequest httpServletRequest){
        logger.info("#######Demande Révocation#######");
        String action = "Action Demande de révocation";
        if(demandeRevocationRequest.getIdSignataire() == null || demandeRevocationRequest.getNomSignataire()==null
        || demandeRevocationRequest.getSignerKey()== null || demandeRevocationRequest.getMotif()==null){
            String retourMessage = "Veuillez remplir tous les champs";
            logger.info(retourMessage);
            gestLogs(httpServletRequest, action,"Echec: "+retourMessage);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(retourMessage);
        }
        try{
            List<Revoke> revokeList = revokeRepository.findByReasonTextContaining(demandeRevocationRequest.getMotif());
            Optional<Signataire> signataireList = signataireRepository.findById(demandeRevocationRequest.getIdSignataire());
            DemandeRevocation demandeRevocation = new DemandeRevocation();
            ListeRevocation listeRevocation = new ListeRevocation();
            //CREATION DE LA DEMANDE DE REVOCATION
            String messageAjout = "CREATION DE LA DEMANDE DE REVOCATION";
            logger.info(messageAjout);
            demandeRevocation.setNomSignataire(demandeRevocationRequest.getNomSignataire());
            demandeRevocation.setIdSignataire(demandeRevocationRequest.getIdSignataire());
            demandeRevocation.setSignerKey(demandeRevocationRequest.getSignerKey());
            demandeRevocation.setIdMotif(revokeList.get(0).getReasonCode());
            demandeRevocation.setMotif(demandeRevocationRequest.getMotif());
            demandeRevocation.setAutreMotif(demandeRevocationRequest.getAutreMotif());
            demandeRevocation.setNomEntreprise(demandeRevocationRequest.getNomEntreprise());
            signataireList.get().setDisabled(true);
            demandeRevocationRepository.save(demandeRevocation);
            //AJOUT DE LA DEMANDE DANS LA LISTE
            listeRevocation.setNomSignataire(demandeRevocationRequest.getNomSignataire());
            listeRevocation.setSignerKey(demandeRevocationRequest.getSignerKey());
            listeRevocation.setIdSignataire(demandeRevocationRequest.getIdSignataire());
            listeRevocation.setIdMotif(revokeList.get(0).getReasonCode());
            listeRevocation.setMotif(demandeRevocationRequest.getMotif());
            listeRevocation.setStatus("En cours...");
            listeRevocation.setAutreMotif(demandeRevocationRequest.getAutreMotif());
            listeRevocation.setNomEntreprise(demandeRevocationRequest.getNomEntreprise());
            listeRevocation.setDateRevocation("-");
            listeRevocationRepository.save(listeRevocation);
            messageAjout = "Ajout demande de révocation terminée avec succès";
            logger.info(messageAjout);
            gestLogs(httpServletRequest, action,messageAjout);
            return ResponseEntity.status(HttpStatus.OK).body("Demande de révocation envoyée!");
        }catch (HttpStatusCodeException e) {
            String errorMessage = "Erreur HTTP survenue: " + e.getResponseBodyAsString();
            logger.error(errorMessage, e);
            gestLogs(httpServletRequest, action,errorMessage);
            return ResponseEntity.status(e.getStatusCode()).body(errorMessage);
        } catch (Exception e) {
            String generalErrorMessage = "Une erreur inattendue est apparue: " + e.getMessage();
            logger.error(generalErrorMessage, e);
            gestLogs(httpServletRequest, action,generalErrorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(generalErrorMessage);
        }

    }
    ////////////////////////////////////////////////////////////ANNULER LA DEMANDE//////////////////////
    @PostMapping("annulerDemandeRevoke")
    public ResponseEntity<?> annulerDemandeRevoke(@RequestBody DemandeRevocationRequest demandeRevocationRequest,HttpServletRequest httpServletRequest){
        String action = "Action Annulation demande de révocation";
        try {

            List<ListeRevocation> listeRevocationList = listeRevocationRepository.findBySignerKey(demandeRevocationRequest.getSignerKey());
            ListeRevocation listeRevocation = listeRevocationList.get(listeRevocationList.size()-1);
            listeRevocation.setStatus("Annulé");
            Optional<Signataire> signataireList = signataireRepository.findById(demandeRevocationRequest.getIdSignataire());
            signataireList.get().setDisabled(false);
            demandeRevocationRepository.deleteBySignerKey(demandeRevocationRequest.getSignerKey());
            gestLogs(httpServletRequest, action,"Annulation de la demande de révocation reussie!");
            return ResponseEntity.status(HttpStatus.OK).body("Annulation de la demande de révocation reussie!");

            }catch (Exception e) {
            logger.error("Erreur lors de l'annulation de la demande de révocation : ", e);
            gestLogs(httpServletRequest, action,"Echec: Erreur lors de l'annulation de la demande de révocation: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'annulation de la demande de révocation. "+ e);
        }

    }

    //////////////////////////PARTIE REVOCATION//////////////////////////////////////////////////////
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("revoke")
    public ResponseEntity<?> revokeSigner(@RequestBody DemandeRevocationRequest demandeRevocationRequest, HttpServletRequest httpServletRequest){
        String action = "Action Revocation";
        try {
            logger.info("#############DEBUT REVOCATION########");
            List<Revoke> revokeList = revokeRepository.findByReasonTextContaining(demandeRevocationRequest.getMotif());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RevokeRequest revokeRequest = new RevokeRequest();
            revokeRequest.setDelete(true);
            if(revokeList.get(0).getReasonText().contains("Autres")){
                revokeRequest.setReason_code(0);
            }else{
                revokeRequest.setReason_code(revokeList.get(0).getReasonCode());
            }


            String lien_revoke = prop.getProperty("lien_api_revoke");
            String username = demandeRevocationRequest.getNomSignataire() + "_" + demandeRevocationRequest.getNomEntreprise().toUpperCase().replaceAll("\\s+", "_");
            String lien_revoke_final = String.format(lien_revoke, username);
            HttpEntity<RevokeRequest> httpEntity = new HttpEntity<>(revokeRequest, headers);
            RestTemplate restTemplate = new RestTemplate(customFact.getClientHttpRequestFactory());
            ResponseEntity<String> response = restTemplate.exchange(lien_revoke_final, HttpMethod.PUT, httpEntity, String.class);
            HttpStatus statusCode = (HttpStatus) response.getStatusCode();
            int statusCodeValue = statusCode.value();
            if (statusCodeValue == 200) {
                List<ListeRevocation> listeRevocationList = listeRevocationRepository.findBySignerKey(demandeRevocationRequest.getSignerKey());
                int tailleListeRevocationList = listeRevocationList.size();
                ListeRevocation listeRevocation = listeRevocationList.get(tailleListeRevocationList-1);
                System.out.println("NOMMMMMM "+listeRevocationList.get(0).getNomSignataire());
                Date date_revocation = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                listeRevocation.setStatus("Révoqué");
                listeRevocation.setDateRevocation(sdf.format(date_revocation));
                logger.info("Suppression signataire et demande ########");
                List<Signataire> signataireList = signataireRepository.findBySignerKey(demandeRevocationRequest.getSignerKey());
                Signataire signataire = signataireList.get(0);
                String nomWorker = signataire.getNomApplication();
                List<Worker> workerList = workerRepository.findWorkersByNomWorker(nomWorker);
                Worker worker = workerList.get(0);
                deleteKeySigner(worker.getIdWorker(), signataire.getSignerKey());
                signataireRepository.deleteBySignerKey(demandeRevocationRequest.getSignerKey());
                demandeRevocationRepository.deleteBySignerKey(demandeRevocationRequest.getSignerKey());
                gestLogs(httpServletRequest, action,"Revocation reussie!");
                logger.info("Revocation avec succès ########");
                return ResponseEntity.status(HttpStatus.OK).body("Revocation reussie!");

            }else {
                logger.error("Erreur lors de l'appel API de révocation : " + response.getBody());
                gestLogs(httpServletRequest, action,"Echec: Erreur lors de l'appel API de révocation: " + response.getBody());
                return ResponseEntity.status(statusCode).body("Erreur lors de l'appel API de révocation. "+ response.getBody());
            }
        }catch (Exception e) {
            logger.error("Erreur lors de la révocation : ", e);
            gestLogs(httpServletRequest, action,"Echec: Erreur lors de la révocation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne du serveur. "+ e);
        }
    }

    private void connectionSetup(AdminWS port)
            throws IOException, GeneralSecurityException {

        HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();

        TLSClientParameters tlsCP = new TLSClientParameters();
        String keyPassword = prop.getProperty("password_keystore");
        //System.out.println(keyPassword);
        KeyStore keyStore = KeyStore.getInstance("JKS");
        String keyStoreLoc = prop.getProperty("keystore");
        keyStore.load(new FileInputStream(keyStoreLoc), keyPassword.toCharArray());
        KeyManager[] myKeyManagers = getKeyManagers(keyStore, keyPassword);
        tlsCP.setKeyManagers(myKeyManagers);

        KeyStore trustStore = KeyStore.getInstance("JKS");
        String trustStoreLoc = prop.getProperty("trustore1");
        trustStore.load(new FileInputStream(trustStoreLoc), keyPassword.toCharArray());
        TrustManager[] myTrustStoreKeyManagers = getTrustManagers(trustStore);
        tlsCP.setTrustManagers(myTrustStoreKeyManagers);

        tlsCP.setDisableCNCheck(true);

        httpConduit.setTlsClientParameters(tlsCP);

    }
    private static TrustManager[] getTrustManagers(KeyStore trustStore)
            throws NoSuchAlgorithmException, KeyStoreException {
        String alg = KeyManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
        fac.init(trustStore);
        return fac.getTrustManagers();
    }

    private static KeyManager[] getKeyManagers(KeyStore keyStore, String keyPassword)
            throws GeneralSecurityException, IOException {
        String alg = KeyManagerFactory.getDefaultAlgorithm();
        char[] keyPass = keyPassword != null
                ? keyPassword.toCharArray()
                : null;
        KeyManagerFactory fac = KeyManagerFactory.getInstance(alg);
        fac.init(keyStore, keyPass);
        return fac.getKeyManagers();
    }

    public boolean deleteKeySigner(@PathVariable int idWorker, @PathVariable String alias) throws MalformedURLException {
        boolean  result=false;
        System.setProperty("javax.net.ssl.keyStore", prop.getProperty("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("password_keystore"));
        System.setProperty("javax.net.ssl.trustStore", prop.getProperty("trustore1"));
        System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("password_keystore"));
        URL wsdlLocation = new URL(prop.getProperty("wsdlUrl") );
        AdminWSService service = new AdminWSService(wsdlLocation);
        port = service.getPort(AdminWS.class);
        try {
            connectionSetup(port);
        } catch (IOException | GeneralSecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            result=port.removeKey(idWorker,alias);

            return result;

        } catch (AdminNotAuthorizedException_Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CryptoTokenOfflineException_Exception e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException_Exception e) {
            throw new RuntimeException(e);
        } catch (SignServerException_Exception e) {
            throw new RuntimeException(e);
        } catch (InvalidWorkerIdException_Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
