package com.SunuBtrust360_Enrol.controller;

import com.SunuBtrust360_Enrol.config.CustomHttpRequestFactory;
import com.SunuBtrust360_Enrol.models.GestLogs;
import com.SunuBtrust360_Enrol.models.Signataire;

import com.SunuBtrust360_Enrol.models.User;
import com.SunuBtrust360_Enrol.models.Worker;
import com.SunuBtrust360_Enrol.payload.request.ObtenirCertRequest;
import com.SunuBtrust360_Enrol.payload.request.RevokeRequest;
import com.SunuBtrust360_Enrol.payload.request.SignataireRequest;
import com.SunuBtrust360_Enrol.repository.GestLogsRepository;
import com.SunuBtrust360_Enrol.repository.UserRepository;
import com.SunuBtrust360_Enrol.repository.WorkerRepository;
import com.SunuBtrust360_Enrol.repository.SignataireRepository;

import com.SunuBtrust360_Enrol.utils.GestSignataire;
import com.SunuBtrust360_Enrol.wsdl.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.bootstrap.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.SunuBtrust360_Enrol.controller.SignerController.convertStringToX509;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 26/10/2023/10/2023 - 12:57
 */
@RestController
@RequestMapping("/v0.0.2/signataire/")
@CrossOrigin(origins = "http://localhost:8080")
@Component
@Tag(name = "Signataire")
@SecurityRequirement(name = "bearerAuth")
public class SignataireController {


    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    private static final String ALGORITHM = "AES";
    private static final String MODE = "CBC";
    private static final String PADDING = "PKCS5Padding";
    private static final String CIPHER_TRANSFORMATION = ALGORITHM + "/" + MODE + "/" + PADDING;
    private static final byte[] IV = new byte[16];
    @Autowired
    private final SignataireRepository signataireRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final WorkerRepository workerRepository;
    @Autowired
    private final GestLogsRepository gestLogsRepository;
    private final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(SignataireController.class);
    Properties prop = null;
    Logger log = null;
    CustomHttpRequestFactory httpRequestFactory = new CustomHttpRequestFactory();
    CustomHttpRequestFactory customFact = new CustomHttpRequestFactory();
    Asn1Set attributes; // Assurez-vous de définir ces attributs correctement
    String signatureAlgorithm = "SHA256withRSA"; // L'algorithme de signature que vous souhaitez utiliser
    String subjectDN = "CN=moi_cherif kasse,O=GAINDE 2000,C=SN,L=DAKAR,ST=DAKAR";
    //Pkcs10CertReqInfo csrInfo = new Pkcs10CertReqInfo(null, signatureAlgorithm, subjectDN);
    /////////////////////GENERATION PDF//////////////////////////////////////////
    @JsonIgnore
    PDDocument document;
    /////////////////////////////////////////////////GENERATION CSR SIGNSERVER /////////////////////////////////////////////////
    AdminWS port = null;
    /////////////////////////////////CSR/////////////////////////////////////////////////////////////////////////////////////
    @Value("${application.security.jwt.secret-key}")
    private String jwtSecret;


    public SignataireController(SignataireRepository signataireRepository, WorkerRepository workerRepository, UserRepository userRepository, GestLogsRepository gestLogsRepository) {
        this.signataireRepository = signataireRepository;
        this.workerRepository = workerRepository;
        this.userRepository = userRepository;
        this.gestLogsRepository = gestLogsRepository;
        log = LogManager.getLogger(SignataireController.class);
        log.debug("Registration class constructor");
        try (InputStream input = SignataireController.class.getClassLoader().getResourceAsStream("configWin.properties")) {

            prop = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            //load a properties file from class path, inside static method
            prop.load(input);
            log.debug("Registration class constructor: prop.loaded");


            log.debug("Registration class constructor: Identifier_lApplication()");

        } catch (IOException ex) {
            ex.printStackTrace();
            log.error(ex);
        }
    }

    public static String encryptPin(String pin) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(pin);
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    //////////////////////////////////////////////////////////////////////////
    ////////////////////////Recherche///////////////////////////////
    // Méthode pour effectuer une recherche en fonction des critères

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
    //Pkcs10CertReqInfo csrInfo= new Pkcs10CertReqInfo();

    //////////////////////////AFFECTATION UTILISATEUR//////////////////////////////////////////////
    @Operation(hidden = true)
    @PostMapping("affectation/{cleDeSignature}/{nom_app}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> affectationSignataire(@PathVariable String cleDeSignature, @PathVariable String nom_app,HttpServletRequest httpServletRequest) {
        String action = "Opération Affectation";
        if (!signataireRepository.findByCleDeSignature(cleDeSignature).isEmpty()) {
            GestSignataire gst = new GestSignataire();
            gst.affecterGroupe(cleDeSignature, nom_app);
            gestLogs(httpServletRequest, action, "Affectation réussie !");
            return ResponseEntity.ok().body("Affectation réussie !");

        }
            gestLogs(httpServletRequest, action, "Echec : Probléme lors de l'affectation");
        return ResponseEntity.badRequest().body("Problème lors de l'affectation !");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////

    ///////////////////////RENOUVELLEMENT PREMIERE VERSION//////////////////////////////////
    @PostMapping("renew")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> renouvellerSignataire(@Valid @RequestBody SignataireRequest signataireRequest, HttpServletRequest httpServletRequest) throws Exception {
        GestSignataire gst = new GestSignataire();
        ObjectMapper objectMapper = new ObjectMapper();
        String cle_de_signature = "";
        String aliasCle = cle_de_signature;
        String action = "Opération Renouvellement";

        try {
            if (signataireRepository.existsByNomSignataire(signataireRequest.getNomSignataire())
                    && signataireRepository.existsByEmail(signataireRequest.getEmail())
                    && signataireRepository.existsByNomEntreprise(signataireRequest.getNom_entreprise())
                    && signataireRepository.existsByNomApplication(signataireRequest.getTrustedApp())) {
                List<Signataire> signataireList = signataireRepository.findByNomSignataire(signataireRequest.getNomSignataire());
                Signataire signer = signataireList.get(0);
                cle_de_signature = signer.getCleDeSignature();
                aliasCle = signer.getSignerKey();
            }

            String username = signataireRequest.getNomSignataire() + "_" + signataireRequest.getNom_entreprise().toUpperCase().replaceAll("\\s+", "_");
            List<Worker> workerList = workerRepository.findWorkersByNomWorker(signataireRequest.getTrustedApp());
            Worker worker = workerList.get(0);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (!validateEmail(signataireRequest.getEmail())) {
                String badRequestMessage = "Vérifiez le format de l'email";
                logger.warn(badRequestMessage);
                gestLogs(httpServletRequest, action, "Echec :"+badRequestMessage);
                return ResponseEntity.badRequest().body(badRequestMessage);
            }

            Signataire signataire = new Signataire();
            signataire.setNomSignataire(signataireRequest.getNomSignataire());
            signataire.setCategorie(signataireRequest.getCategorie());
            signataire.setNomApplication(signataireRequest.getTrustedApp());
            signataire.setCode_pin(encrypterPin(signataireRequest.getCode_pin()));
            signataire.setEmail(signataireRequest.getEmail());
            signataire.setNomEntreprise(signataireRequest.getNom_entreprise());
            signataire.setCleDeSignature(cle_de_signature);
            signataire.setCniPassport(signataireRequest.getCniPassport());

            ObtenirCertRequest obtenirCertRequest = new ObtenirCertRequest();
            obtenirCertRequest.setCertificate_authority_name(prop.getProperty("certificate_authority_name"));
            obtenirCertRequest.setCertificate_profile_name(prop.getProperty("certificate_profile_name"));
            obtenirCertRequest.setEnd_entity_profile_name(prop.getProperty("end_entity_profile_name"));
            obtenirCertRequest.setUsername(username);
            obtenirCertRequest.setInclude_chain(true);
            obtenirCertRequest.setPassword(signataireRequest.getPassword());
            String subjectDN = "CN=" + signataireRequest.getNomSignataire() + ",O=" + signataireRequest.getNom_entreprise() + ",C=SN";

            deleteKeySigner(worker.getIdWorker(), aliasCle);
            String signKey = generateCryptoToken(aliasCle);
            signataire.setSignerKey(aliasCle);
            obtenirCertRequest.setCertificate_request(webServiceConnect(subjectDN, signKey));

            HttpEntity<ObtenirCertRequest> httpEntity = new HttpEntity<>(obtenirCertRequest, headers);
            RestTemplate restTemplate = new RestTemplate(customFact.getClientHttpRequestFactory());
            ResponseEntity<String> response = null;
            try{
                response  = restTemplate.postForEntity(prop.getProperty("lien_api_ejbca_enroll"), httpEntity, String.class);
            }catch (Exception e){
                String errorMessage = "An error has occcured. Veuillez réessayer.";
                if(isExistSignerKey(aliasCle)){
                    logger.info("Suppression de la clé existante pour l'alias : {}", aliasCle);
                    deleteKeySigner(Integer.parseInt(prop.getProperty("idWorkerPourSupprimerSignerKey")),aliasCle);
                    System.out.println("Suppression de la clé existante pour l'alias :" +aliasCle);
                }
                System.out.println(errorMessage);
                logger.error(errorMessage);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorMessage);
            }

            EnrollResponse enrollResponse = objectMapper.readValue(response.getBody(), EnrollResponse.class);
            List<String> certificateListPem = new ArrayList<>();
            List<String> certificateChain = enrollResponse.getCertificate_chain();
            certificateListPem.add(enrollResponse.getCertificate());
            if(certificateChain.size() == 2){
                // System.out.println("Import chaine");
                certificateListPem.add(certificateChain.get(0));
                certificateListPem.add(certificateChain.get(1));

            }
            importChaine(certificateListPem, aliasCle);

            HttpStatus statusCode = (HttpStatus) response.getStatusCode();
            int statusCodeValue = statusCode.value();

            if (statusCodeValue == 201) {
                Date date_creation = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                setDateSignataire(sdf, signataire, enrollResponse, date_creation);
                //signataire.setDate_expiration(calculerDateExpiration2(sdf.format(date_creation)));

                gst.updateRenouveler(signataire.getCleDeSignature(), signataire.getCode_pin(), sdf.format(date_creation), calculerDateExpiration2(sdf.format(date_creation)));
                logger.info("Renouvellement réussi avec succès : " + response.getBody());
                gestLogs(httpServletRequest, action, "Renouvellement réussi");
            } else {
                logger.error("Renouvellement echoué: " + response.getBody());
                gestLogs(httpServletRequest, action, "Echec : Erreur Server");
            }

            return response;

        }catch (HttpServerErrorException e) {
            String errorMessage = "Opération échouée! Veuillez réessayer.";
            if(isExistSignerKey(aliasCle)){
                deleteKeySigner(Integer.parseInt(prop.getProperty("idWorkerPourSupprimerSignerKey")),aliasCle);
            }
            logger.error(errorMessage);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorMessage);
        }
        catch (HttpStatusCodeException e) {
            String errorMessage2 = "Erreur HTTP survenue: \nOpération échouée! Veuillez réessayer.";
            String errorMessage = "Erreur HTTP survenue: " + e.getResponseBodyAsString();
            if(isExistSignerKey(aliasCle)){
                deleteKeySigner(Integer.parseInt(prop.getProperty("idWorkerPourSupprimerSignerKey")),aliasCle);
            }
            logger.error(errorMessage, e);
            return ResponseEntity.status(e.getStatusCode()).body(errorMessage2);
        } catch (Exception e) {
            String errorMessage2 = "Erreur HTTP survenue: \nOpération échouée! Veuillez réessayer.";
            String generalErrorMessage = "Une erreur inattendue est apparue: " + e.getMessage();
            if(isExistSignerKey(aliasCle)){
                deleteKeySigner(Integer.parseInt(prop.getProperty("idWorkerPourSupprimerSignerKey")),aliasCle);
            }
            logger.error(generalErrorMessage, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage2);
        }
    }

    ///////////////////////////AVOIR LA LISTE DES SIGNATAIRES//////////////////////////////////////////
    @GetMapping("tous_signataires")
    @Operation(hidden = true)
    public List<Signataire> getAllSigners() {
        return signataireRepository.findAll();
    }
    /////////////////////////////Jours restants expiration certificate/////////////////////////////////////
    @Operation(hidden = true)
    @GetMapping("jours_restants/{cle}")
    public String getJoursRestants(@PathVariable String cle) {
        String joursRestants = "";
        if (!signataireRepository.findByCleDeSignature(cle).isEmpty()) {
            Signataire signer = signataireRepository.findByCleDeSignature(cle).get(0);
            // Formatter pour convertir la chaîne en LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Convertir les chaînes en LocalDateTime
            LocalDateTime dateTime1 = LocalDateTime.parse(signer.getDateCreation(), formatter);
            LocalDateTime dateTime2 = LocalDateTime.parse(signer.getDate_expiration(), formatter);
            LocalDateTime currentDate = LocalDateTime.now();
            // Calculer la différence entre les dates
            long differenceInYears = ChronoUnit.YEARS.between(dateTime1, dateTime2);
            long differenceInMonths = ChronoUnit.MONTHS.between(dateTime1, dateTime2);
            long differenceInDays = ChronoUnit.DAYS.between(currentDate, dateTime2);
            long differenceInHours = ChronoUnit.HOURS.between(dateTime1, dateTime2);
            long differenceInMinutes = ChronoUnit.MINUTES.between(dateTime1, dateTime2);
            long differenceInSeconds = ChronoUnit.SECONDS.between(dateTime1, dateTime2);
            joursRestants = "Le certificat expire dans" + differenceInDays + " jours";
        }
        return joursRestants;
    }

    //////////////////////////////////////////////////////////////////////////
    //////////////////////////GENERATION PIN//////////////////////////////////////////////////
    @PostMapping("generer_pin")
    @Operation(hidden = true)
    // @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public long pin_generation() throws NoSuchAlgorithmException {
        final int taille_pin = 6;
        Random random = new Random();
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        final StringBuilder pin = new StringBuilder(taille_pin);
        for (int i = 0; i < taille_pin; i++) {
            pin.append(secureRandom.nextInt(10));
        }
        if(pin.charAt(0) == '0' ) {
            int chiffre = random.nextInt(9) + 1;
            // Remplacer le premier caractère par le chiffre généré
            pin.setCharAt(0, (char) (chiffre + '0'));
        }
        return Long.parseLong(pin.toString());
    }

    @RequestMapping("/")
    @Operation(hidden = true)
    public String hello() {
        return "Goooooooooood";
    }

    public String convertObjectToJson(Object endEntityRequest) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(endEntityRequest);
            return json;
        } catch (Exception e) {
            return null;
        }
    }

    public KeyStore loadKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream keystoreInputStream = new FileInputStream(prop.getProperty("keystore"))) {
            keyStore.load(keystoreInputStream, prop.getProperty("password_keystore").toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la création de l'usine de requêtes personnalisée : " + e.getMessage());
            return null;
        }
        return keyStore;
    }

    public KeyStore loadTrustStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream keystoreInputStream = new FileInputStream(prop.getProperty("trustore"))) {
            keyStore.load(keystoreInputStream, prop.getProperty("password_keystore").toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la création de l'usine de requêtes personnalisée : " + e.getMessage());
            return null;
        }
        return keyStore;
    }

    /////////////////////////////////////REVOKE Non Utilisé////////////////////////////////////////////////////////////
    @PutMapping("revoke/{endentity_name}")
    @Operation(hidden = true)
    public ResponseEntity<?> revokeSigner(@PathVariable String endentity_name) {
        GestSignataire gst = new GestSignataire();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RevokeRequest revokeRequest = new RevokeRequest();
        revokeRequest.setReason_code(Integer.parseInt(prop.getProperty("reason_code")));
        revokeRequest.setDelete(Boolean.parseBoolean(prop.getProperty("delete")));
        //String lien="https://10.10.1.7/ejbca/ejbca-rest-api/v1/endentity/"+endentity_name+"/revoke";
        String lien_revoke = prop.getProperty("lien_api_revoke");
        String lien_revoke_final = String.format(lien_revoke, endentity_name);
        HttpEntity<RevokeRequest> httpEntity = new HttpEntity<>(revokeRequest, headers);
        RestTemplate restTemplate = new RestTemplate(customFact.getClientHttpRequestFactory());
        ResponseEntity<String> response = restTemplate.exchange(lien_revoke_final, HttpMethod.PUT, httpEntity, String.class);
        HttpStatus statusCode = (HttpStatus) response.getStatusCode();
        int statusCodeValue = statusCode.value();
        if (statusCodeValue == 200) {
            return ResponseEntity.ok().body("Signataire supprimé avec succès");
        } else {
            return ResponseEntity.ok().body("Vérifier les informations du signataire que vous essayez de supprimer");
        }


    }
    @Operation(hidden = true)
    @PutMapping("revokeSigner2/{cle}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> revokeSignerSansRenew2(@PathVariable String cle) {
        int statusCodeValue = 0;
        Signataire signataire = new Signataire();
        GestSignataire gst = new GestSignataire();
        String cle_de_signature = "";
        if (!signataireRepository.findByCleDeSignature(cle).isEmpty()) {
            Signataire signer = signataireRepository.findByCleDeSignature(cle).get(0);
            String username = signer.getNomSignataire() + "_" + signer.getNomEntreprise().toUpperCase().replaceAll("\\s+", "_");
            cle_de_signature = "CLE_" + signer.getNomSignataire().toUpperCase().replaceAll("\\s+", "_") + "_" + signer.getNomEntreprise().toUpperCase().replaceAll("\\s+", "_");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RevokeRequest revokeRequest = new RevokeRequest();
            revokeRequest.setReason_code(Integer.parseInt(prop.getProperty("reason_code")));
            revokeRequest.setDelete(Boolean.parseBoolean(prop.getProperty("delete")));
            String lien_revoke = prop.getProperty("lien_api_revoke");
            String lien_revoke_final = String.format(lien_revoke, username);
            HttpEntity<RevokeRequest> httpEntity = new HttpEntity<>(revokeRequest, headers);
            RestTemplate restTemplate = new RestTemplate(customFact.getClientHttpRequestFactory());
            ResponseEntity<String> response = restTemplate.exchange(lien_revoke_final, HttpMethod.PUT, httpEntity, String.class);
            HttpStatus statusCode = (HttpStatus) response.getStatusCode();
            statusCodeValue = statusCode.value();

        }
        if (statusCodeValue == 200) {
            gst.deleteSigner(cle_de_signature);
            return ResponseEntity.ok().body("Signataire supprimé avec succès");

        } else {
            return ResponseEntity.badRequest().body("Vérifier les informations du signataire que vous essayez de supprimer");
        }
    }
    ///////////////////////REVOQUER SIGNATAIRE VERSION 2//////////////////////////////////
    @PutMapping("revokeSigner/{cle}")
    @Operation(hidden = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> revokeSignerSansRenew_V2(@PathVariable String cle,HttpServletRequest httpServletRequest) throws MalformedURLException {
        int statusCodeValue = 0;
        Signataire signataire = new Signataire();
        GestSignataire gst = new GestSignataire();
        String aliasCle = "";
        List<Worker> workerList = new ArrayList<>();
        String cle_de_signature = "";
        boolean resultat = false;
        String action = "Opération de suppression";

        try {
            if (!signataireRepository.findByCleDeSignature(cle).isEmpty()) {
                Signataire signer = signataireRepository.findByCleDeSignature(cle).get(0);
                cle_de_signature = signer.getSignerKey();
                aliasCle = cle_de_signature;
                workerList = workerRepository.findWorkersByNomWorker(signer.getNomApplication());
                Worker worker = workerList.get(0);
                resultat = deleteKeySigner(worker.getIdWorker(), aliasCle);
            }

            if (resultat) {
                gst.deleteSigner(cle_de_signature);
                String successMessage = "Signataire supprimé avec succès";
                logger.info(successMessage);
                gestLogs(httpServletRequest, action, successMessage);
                return ResponseEntity.ok().body(successMessage);
            } else {
                String badRequestMessage = "Vérifier les informations du signataire que vous essayez de supprimer";
                logger.warn(badRequestMessage);
                gestLogs(httpServletRequest, action, badRequestMessage);
                return ResponseEntity.badRequest().body(badRequestMessage);
            }
        } catch (HttpStatusCodeException e) {
            String errorMessage = "Une erreur HTTP est survenue: " + e.getResponseBodyAsString();
            logger.error(errorMessage, e);
            gestLogs(httpServletRequest, action, errorMessage);
            return ResponseEntity.status(e.getStatusCode()).body(errorMessage);
        } catch (Exception e) {
            String generalErrorMessage = "Une erreur inattendue est survenue: " + e.getMessage();
            logger.error(generalErrorMessage, e);
            gestLogs(httpServletRequest, action, generalErrorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(generalErrorMessage);
        }
    }
    public String[] separer_idapp_nomapp(String app_cible) {
        // Supprimer les crochets et diviser la chaîne par la virgule
        String[] parts = app_cible.substring(1, app_cible.length() - 1).split(",");

        // Retirer les espaces blancs autour des mots
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        return parts;
    }

    public String formaterDate() {
        // Obtenez la date actuelle
        Date currentDate = new Date();

        // Créez un objet SimpleDateFormat avec le modèle de format désiré
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy", new Locale("fr", "FR"));

        // Formatez la date en utilisant le modèle
        String formattedDate = dateFormat.format(currentDate);

        // Affichez le résultat
        return formattedDate;

    }

    public String calculerDateExpiration2(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime initialDate = LocalDateTime.parse(dateString, formatter);

        // Ajoutez Un ans à la date actuelle
        LocalDateTime dateInTwoYears = initialDate.plusYears(1);

        // Soustrayez un jour de la date dans deux ans
        LocalDateTime resultDate = dateInTwoYears.minusDays(1);

        // Formattez la date résultante (optionnel, pour l'affichage
        return resultDate.format(formatter);
    }

    public boolean validateEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public WebServiceTemplate createWebServiceTemplate() throws Exception {
        // Configurer le contexte SSL
        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(loadKeyStore(), prop.getProperty("password_keystore").toCharArray())
                .build();
        // Configurer le message sender avec SSL
        HttpComponentsMessageSender messageSender = new HttpComponentsMessageSender(HttpClientBuilder.create().setSSLContext(sslContext).addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor()).build());
        messageSender.setHttpClient(HttpClientBuilder.create().setSSLContext(sslContext).build());
        // Configurer WebServiceTemplate avec le message sender
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMessageSender(messageSender);
        return webServiceTemplate;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
    @GetMapping("token-expiration-time/{token}")
    @Operation(hidden = true)
    public long getTokenExpirationTime(@PathVariable String token) {
       try{
           // Extraire la date d'expiration du token JWT
           Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
           Date expirationDate = claims.getBody().getExpiration();

           // Calculer le temps restant avant l'expiration
           long currentTimeMillis = Instant.now().toEpochMilli();
           long expirationTimeMillis = expirationDate.getTime();
           long timeRemainingMillis = expirationTimeMillis - currentTimeMillis;

           // Convertir le temps restant en secondes et le retourner
           return timeRemainingMillis / 1000;
       } catch (Exception ex){
           return 0;
       }
    }
    @GetMapping("verif-token/{authToken}")
    @Operation(hidden = true)
    public boolean validateJwtToken(@PathVariable String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    private SecretKey getSecretKey(String keyString) {
        byte[] keyBytes = hexStringToByteArray(keyString);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    @Operation(hidden = true)
    @PostMapping("encrypt/{pin}")
    public String encrypterPin(@PathVariable String pin) {
        try {
            logger.info("########DEBUT PROCESSUS DU CHIFFREMENT DU CODE PIN#########");
            SecretKey secretKey = getSecretKey(prop.getProperty("cleDeSecret"));
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
            byte[] encryptedBytes = cipher.doFinal(pin.getBytes(StandardCharsets.UTF_8));
            String encodedPin = Base64.getEncoder().encodeToString(encryptedBytes);
            // Remplacer "/" par "A" et récupérer les indices
            StringBuilder replacedString = new StringBuilder(encodedPin);
            StringBuilder chaineIndex = new StringBuilder(",");
            for (int i = 0; i < replacedString.length(); i++) {
                if (replacedString.charAt(i) == '/') {
                    chaineIndex.append(i);
                    chaineIndex.append(",");
                    replacedString.setCharAt(i, 'A');
                }
            }
            replacedString.append(chaineIndex);
            // Afficher la chaîne après remplacement
            //System.out.println("Chaîne index : " + chaineIndex);
            //System.out.println("Chaîne avant remplacement : " + encodedPin);
            //System.out.println("Chaîne après remplacement : " + replacedString);
            // Afficher les indices des "/"
            logger.info("Code PIN chiffré avec succès !");
            logger.info("########PROCESSUS DU CHIFFREMENT DU CODE PIN TERMINE#########");
            return replacedString.toString();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            String msg = "Erreur lors du chiffrement.";
            logger.error(msg,e);
            logger.info("########PROCESSUS DU CHIFFREMENT DU CODE PIN TERMINE#########");
            return msg;
        }
    }

    /////////////////SELECTION DATE///////////////////////////////////
    @Operation(hidden = true)
    @GetMapping("listUserByDate/{dateCreation}")
    public List<Signataire> selectByDate(@PathVariable String dateCreation) {
        return signataireRepository.findByDateCreationContaining(dateCreation);
    }

    ////////////////////////////////////////////////////////////////
    @PostMapping("decrypt/{pinEncrypted}")
    @Operation(hidden = true)
    public String decryptPin(@PathVariable String pinEncrypted) {
        try {
            logger.info("########DEBUT PROCESSUS DU DECHIFFREMENT DU CODE PIN#########");
            SecretKey secretKey = getSecretKey(prop.getProperty("cleDeSecret"));
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));

            List<Integer> indices = new ArrayList<>();
            String[] parts = pinEncrypted.split(",");
            if (parts.length >= 2) {
                StringBuilder chaineRestauree = new StringBuilder(parts[0]);
                for (int i = 1; i < parts.length; i++) {
                    String index = parts[i];
                    indices.add(Integer.parseInt(index));
                }
                for (int index2 : indices) {
                    chaineRestauree.setCharAt(index2, '/');
                }
                //System.out.println(chaineRestauree);
                byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(chaineRestauree.toString()));
                return new String(decryptedBytes, StandardCharsets.UTF_8);
            } else {
                pinEncrypted = parts[0];
                byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(pinEncrypted));
                logger.info("Code PIN déchiffré avec succès !");
                logger.info("########PROCESSUS DU DECHIFFREMENT DU CODE PIN TERMINE#########");
                return new String(decryptedBytes, StandardCharsets.UTF_8);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            String msg= "Erreur lors du déchiffrement du code PIN";
            logger.error(msg,e);
            logger.info("########PROCESSUS DU DECHIFFREMENT DU CODE PIN TERMINE#########");
            return msg;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @GetMapping("generer_user/{nbre}")
    @Operation(hidden = true)
    public void genererEtEnregistrerDonnees(@PathVariable int nbre) {
        for (int i = 0; i < nbre; i++) {
            Signataire signataire = genererSignataire(i);
            signataireRepository.save(signataire);
        }
    }

    private Signataire genererSignataire(int index) {
        Random random = new Random();

        Signataire signataire = new Signataire();
        //signataire.setApplication_rattachee(String.valueOf(generateRandomInt(100000, 999999)));
        signataire.setCategorie(generateRandomCategory());
        signataire.setCleDeSignature("CLE_" + generateRandomString() + "_" + generateRandomWord() + "_" + generateRandomInt(1000, 9999));
        signataire.setCode_pin(generateRandomUUID());
        // Conversion de la String en LocalDateTime
        String dateCreationString = generateRandomDateAsString();
        LocalDateTime dateCreation = LocalDateTime.parse(dateCreationString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        signataire.setDateCreation(generateRandomDate().toString());
        signataire.setDate_expiration(LocalDateTime.parse(signataire.getDateCreation()).plusDays(generateRandomInt(365, 730)).toString());
        signataire.setEmail(generateRandomEmail(index));
        signataire.setNomSignataire(generateRandomName());
        signataire.setNomApplication(generateRandomWord());
        signataire.setNomEntreprise(generateRandomCompany());

        // Ajoutez ici toute logique d'ajustement des valeurs en fonction de l'index

        return signataire;
    }

    private String generateRandomDateAsString() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime randomDate = now.minusDays(generateRandomInt(1, 365 * 5));
        return randomDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private int generateRandomInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    private String generateRandomCategory() {
        String[] categories = {"Autre", "Catégorie1", "Catégorie2"};
        return categories[new Random().nextInt(categories.length)];
    }

    private String generateRandomString() {
        return generateRandomWord().toLowerCase();
    }

    private String generateRandomWord() {
        String[] words = {"Apple", "Banana", "Cherry", "Orange", "Grape", "Mango", "Strawberry"};
        return words[new Random().nextInt(words.length)];
    }

    private String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }

    private LocalDateTime generateRandomDate() {
        LocalDateTime now = LocalDateTime.now();
        return now.minusDays(generateRandomInt(1, 365 * 5)); // Date within the last 5 years
    }

    private String generateRandomEmail(int i) {
        return i + generateRandomWord().toLowerCase() + "@example.com";
    }

    private String generateRandomName() {
        String[] names = {"John Doe", "Jane Doe", "Alice Smith", "Bob Johnson", "Eva Brown"};
        return names[new Random().nextInt(names.length)];
    }

    private String generateRandomCompany() {
        String[] companies = {"ABC Corp", "XYZ Ltd", "123 Enterprises", "Tech Solutions", "Global Innovations"};
        return companies[new Random().nextInt(companies.length)];
    }

    @GetMapping("generatePdf")
    @Operation(hidden = true)
    public PDDocument generatePdf(@RequestBody Signataire signataire) {
        try (InputStream templateStream = getClass().getClassLoader().getResourceAsStream("template.pdf")) {

            // Load the template PDF
            document = PDDocument.load(templateStream);

            PDDocumentCatalog docCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();

            // Replace "date_impression" with the actual field name in your template
            PDField date_impr = acroForm.getField("date_impression");
            date_impr.setValue(formaterDate());

            PDField nom_signer = acroForm.getField("nomSignataire");
            nom_signer.setValue(signataire.getNomSignataire());

            PDField nom_entreprise = acroForm.getField("nom_entreprise");
            nom_entreprise.setValue(signataire.getNomEntreprise());

            PDField codePin = acroForm.getField("codePin");
            codePin.setValue(signataire.getCode_pin());


            // Save the document to the output stream
            return document;

            // Flush and close the output stream

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    ////////////////////////////CSRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR////////////////////////////////////////////////
 /*@PostMapping("obtenirCSR")

 public String generateCSR(String subjectDN ) throws Exception {

     KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
     keyPairGenerator.initialize(2048);
     KeyPair keyPair = keyPairGenerator.generateKeyPair();

     PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
             new X500Name(subjectDN),
             keyPair.getPublic()
     );

     ExtensionsGenerator extGen = new ExtensionsGenerator();
     extGen.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
     extGen.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyEncipherment | KeyUsage.digitalSignature | KeyUsage.nonRepudiation));
     p10Builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extGen.generate());

     ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());

     PKCS10CertificationRequest csr = p10Builder.build(signer);
     CustomPKCS10CertificationRequest my_csr= new CustomPKCS10CertificationRequest(csr);

     return convertToPEM(my_csr);
 }*/
    @PostMapping("obtenirCSR3/{subjectDN}")
    @Operation(hidden = true)
    public PKCS10CertificationRequest generateCSR2(@PathVariable String subjectDN) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // Générer une paire de clés (clé privée et clé publique)
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Créer une demande de certificat vide
        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
                new X500Name(subjectDN),
                keyPair.getPublic()
        );

        // Ajouter des attributs (facultatif)
        ASN1EncodableVector attributes = new ASN1EncodableVector();

        // Exemple d'ajout d'un attribut. Vous devez remplacer cela par vos propres attributs.
        ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier("2.5.4.3"); // OID pour CommonName
        attributes.add(new DERSequence(new ASN1Encodable[]{oid, new X500Name(subjectDN)}));

        ASN1Set asn1Attributes = new DERSet(attributes);
        p10Builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, asn1Attributes);

        // Créer un signataire de contenu
        PKCS10CertificationRequest csr = p10Builder.build(new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate()));

        return csr;
    }


    public String convertToPEM(String csr) {
        StringWriter stringWriter = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(stringWriter)) {
            pemWriter.writeObject(new PemObject("CERTIFICATE REQUEST", csr.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public String bytesToString(byte[] tab_bytes) {
        String text = new String((tab_bytes), StandardCharsets.UTF_8);
        return text;
    }

    ///////////////////////////////////////////////////////////////////////////
    //@PostMapping("getSignerKey")
    public String generateCryptoToken(String alias) throws MalformedURLException {
        System.setProperty("javax.net.ssl.keyStore", prop.getProperty("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("password_keystore"));
        System.setProperty("javax.net.ssl.trustStore", prop.getProperty("trustore1"));
        System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("password_keystore"));
        URL wsdlLocation = new URL(prop.getProperty("wsdlUrl"));
        AdminWSService service = new AdminWSService(wsdlLocation);
        port = service.getPort(AdminWS.class);
        try {
            connectionSetup(port);
        } catch (IOException | GeneralSecurityException e1) {
            e1.printStackTrace();
        }
        try {
            // Appeler l'opération
            int signerId = Integer.parseInt(prop.getProperty("idCryptoTokenWrap")) ;  // Remplacez par les valeurs appropriées
            String keyAlgorithm = "RSA";
            String keySpec = "2048";
            String authCode = prop.getProperty("authCodeSignKey");
            String result = port.generateSignerKey(signerId, keyAlgorithm, keySpec, alias, authCode);
            // Afficher le résultat
           // System.out.println("Résultat de l'opération : " + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("isExistSignerKey/{alias}")
    public boolean isExistSignerKey(@PathVariable String alias) throws MalformedURLException {
        System.setProperty("javax.net.ssl.keyStore", prop.getProperty("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("password_keystore"));
        System.setProperty("javax.net.ssl.trustStore", prop.getProperty("trustore1"));
        System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("password_keystore"));
        URL wsdlLocation = new URL(prop.getProperty("wsdlUrl"));
        AdminWSService service = new AdminWSService(wsdlLocation);
        port = service.getPort(AdminWS.class);
        boolean resultat = false;
        try {
            connectionSetup(port);
        } catch (IOException | GeneralSecurityException e1) {
            e1.printStackTrace();
        }
        List<KeyTestResult> result = null;
        try {
            // Appeler l'opération
            int signerId = Integer.parseInt(prop.getProperty("idCryptoTokenWrap")) ;  // Remplacez par les valeurs appropriées
            String keyAlgorithm = "RSA";
            String keySpec = "2048";
            String authCode = prop.getProperty("authCodeSignKey");
            result = port.testKey(signerId,alias,authCode);
            if(result.get(0).isSuccess()){
                resultat = true;
            }
            // Afficher le résultat
            // System.out.println("Résultat de l'opération : " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultat;
    }

    public boolean deleteSignerKey(String alias) throws MalformedURLException {
        System.setProperty("javax.net.ssl.keyStore", prop.getProperty("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("password_keystore"));
        System.setProperty("javax.net.ssl.trustStore", prop.getProperty("trustore1"));
        System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("password_keystore"));
        URL wsdlLocation = new URL(prop.getProperty("wsdlUrl"));
        AdminWSService service = new AdminWSService(wsdlLocation);
        port = service.getPort(AdminWS.class);
        try {
            connectionSetup(port);
        } catch (IOException | GeneralSecurityException e1) {
            e1.printStackTrace();
        }
        try {
            // Appeler l'opération
            int signerId = Integer.parseInt(prop.getProperty("idCryptoTokenWrap")) ;  // Remplacez par les valeurs appropriées
            String keyAlgorithm = "RSA";
            String keySpec = "2048";
            String authCode = prop.getProperty("authCodeSignKey");
            boolean result = port.removeKey(signerId,alias);
            // Afficher le résultat
            // System.out.println("Résultat de l'opération : " + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @GetMapping("getWorkerId/{nomWorker}")
    @Operation(hidden = true)
    public int getWorkerId(@PathVariable String nomWorker) throws MalformedURLException {
        System.setProperty("javax.net.ssl.keyStore", prop.getProperty("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("password_keystore"));
        System.setProperty("javax.net.ssl.trustStore", prop.getProperty("trustore1"));
        System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("password_keystore"));
        URL wsdlLocation = new URL(prop.getProperty("wsdlUrl"));
        AdminWSService service = new AdminWSService(wsdlLocation);
        port = service.getPort(AdminWS.class);
        try {
            connectionSetup(port);
        } catch (IOException | GeneralSecurityException e1) {
            e1.printStackTrace();
        }
        try {
            // Appeler l'opération

            int idWorker = port.getWorkerId(nomWorker);
            // Afficher le résultat
           // System.out.println("Résultat de l'opération : " + idWorker);
            return idWorker;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    @PostMapping("reload/{workerId}")
    @Operation(hidden = true)
    public void reload(@PathVariable int workerId) throws MalformedURLException {
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
            e1.printStackTrace();
        }
        try {
            // Appeler l'opération

            port.reloadConfiguration(workerId);
            // Afficher le résultat
            //System.out.println("Résultat de l'opération : " + idWorker);
            //return idWorker;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Récuperation de la liste de revocation
    @GetMapping("getAllLogs")
    public List<GestLogs> listAllLogs()  {
        return gestLogsRepository.findAll();
    }

    @PostMapping("getAllWorkers")
    @Operation(hidden = true)
    public List<Integer> getAllWorkers() throws MalformedURLException {
        // TODO Auto-generated method stub
        List<Integer> result = new ArrayList<>();
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
            List<String> nomWorker = new ArrayList<>();
            result=port.getWorkers(2);
            for(int idWorker: result){

            }
          return result;

        } catch (AdminNotAuthorizedException_Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }
    private List<byte[]> decodeBase64List(List<String> inputList) {
        List<byte[]> resultList = new ArrayList<>();
        for (String inputString : inputList) {
            byte[] certificateBytes = Base64.getDecoder().decode(inputString.getBytes(StandardCharsets.UTF_8));
            resultList.add(certificateBytes);
        }
        return resultList;
    }
    @PostMapping("importChaine/{chaine}/{alias}")
    @Operation(hidden = true)
    public void importChaine(@PathVariable List<String> chaine,@PathVariable String alias) throws MalformedURLException {
        // TODO Auto-generated method stub
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
            List<byte[]> certificateListBytes = decodeBase64List(chaine);
            port.importCertificateChain(8,certificateListBytes,alias,"foo123");

        } catch (AdminNotAuthorizedException_Exception |
                 CryptoTokenOfflineException_Exception | OperationUnsupportedException_Exception |
                 CertificateException_Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    @PostMapping("avoirCSR2/{subjectDN}/{keyAlias}")
    @Operation(hidden = true)
    public String webServiceConnect(@PathVariable String subjectDN, @PathVariable String keyAlias) throws MalformedURLException {
        // TODO Auto-generated method stub
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
        String user_num = "1";
        //List<Worker> workerList=workerRepository.findWorkersByNomWorker(nomWorker);
        //Worker mon_worker = workerList.get(0);
        //System.out.println("Liste worker: " + getWorkerId(nomWorker));
        Pkcs10CertReqInfo pkcinf = new Pkcs10CertReqInfo();
        pkcinf.setSignatureAlgorithm("SHA256withRSA"); //"SHA256withECDSA"
        pkcinf.setSubjectDN(subjectDN);
        //PKCS10CertReqInfo certReqInfo = new PKCS10CertReqInfo("SHA1WithRSA", "CN=Worker" + WORKER_PDF, null);
        try {
            // RestTemplate restTemplate = new RestTemplate(customFact.getClientHttpRequestFactory());
            // ResponseEntity<String> response = restTemplate.exchange("https://10.10.1.8/signserver/rest/v1/workers/14" , HttpMethod.PUT, httpEntity, String.class);
            int idWorker = Integer.parseInt(prop.getProperty("idWorker"));
            Base64SignerCertReqData resp = port.getPKCS10CertificateRequestForAlias(idWorker, pkcinf, false,keyAlias);
            String text = new String((resp.getBase64CertReq()));
            //System.out.println("cl� public: " + formatCSR(text));
            return formatCSR(text);
        } catch (InvalidWorkerIdException_Exception | AdminNotAuthorizedException_Exception
                 | CryptoTokenOfflineException_Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("avoirCSR/{subjectDN}")
    @Operation(hidden = true)
    public String Connect_WS(@PathVariable String subjectDN) throws MalformedURLException {
        // TODO Auto-generated method stub
        System.setProperty("javax.net.ssl.keyStore", prop.getProperty("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("password_keystore"));
        System.setProperty("javax.net.ssl.trustStore", prop.getProperty("trustore1"));
        System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("password_keystore"));
        URL wsdlLocation = new URL(prop.getProperty("wsdlUrl"));
        AdminWSService service = new AdminWSService(wsdlLocation);
        port = service.getPort(AdminWS.class);
        try {
            connectionSetup(port);
        } catch (IOException | GeneralSecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String user_num = "1";

        Pkcs10CertReqInfo pkcinf = new Pkcs10CertReqInfo();
        pkcinf.setSignatureAlgorithm("SHA256withRSA"); //"SHA256withECDSA"
        pkcinf.setSubjectDN(subjectDN);
        //PKCS10CertReqInfo certReqInfo = new PKCS10CertReqInfo("SHA1WithRSA", "CN=Worker" + WORKER_PDF, null);
        try {
            // RestTemplate restTemplate = new RestTemplate(customFact.getClientHttpRequestFactory());
            // ResponseEntity<String> response = restTemplate.exchange("https://10.10.1.8/signserver/rest/v1/workers/14" , HttpMethod.PUT, httpEntity, String.class);
            Base64SignerCertReqData resp = port.getPKCS10CertificateRequest(11, pkcinf, false);
            String text = new String((resp.getBase64CertReq()));
           // System.out.println("cl� public: " + formatCSR(text));
            return formatCSR(text);
        } catch (InvalidWorkerIdException_Exception | AdminNotAuthorizedException_Exception
                 | CryptoTokenOfflineException_Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
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

    private String insertNewLines(String input) {
        // Insérer des retours à la ligne tous les 64 caractères
        StringBuilder builder = new StringBuilder();
        int index = 0;
        while (index < input.length()) {
            builder.append(input, index, Math.min(index + 64, input.length()));
            if (index != input.length() - 1) {
                builder.append("\n");
            }
            index += 64;
        }
        return builder.toString();
    }

    public String formatCSR(String csr) {
        // Ajouter les délimiteurs
        String beginDelimiter = "-----BEGIN CERTIFICATE REQUEST-----";
        String endDelimiter = "-----END CERTIFICATE REQUEST-----";

        return beginDelimiter + "\n" + insertNewLines(csr) + "\n" + endDelimiter;
    }
    public String formatCert(String csr) {
        // Ajouter les délimiteurs
        String beginDelimiter = "-----BEGIN CERTIFICATE-----";
        String endDelimiter = "-----END CERTIFICATE-----";

        return beginDelimiter + "\n" + insertNewLines(csr) + "\n" + endDelimiter;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Methode pour la gestion des logs
     * **/
    public void gestLogs( HttpServletRequest httpServletRequest,String action, String message){
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
    @PostMapping("enroll_V2")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Enrolement Signataire ", description = "Enroler un signataire et lui fournir un certificat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrolement effectué avec succès - Renvoie le certificat."),
            @ApiResponse(responseCode = "400", description = "Vérifiez le format de l'email -Vous essayez d'enrôler une personne déja existante !"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "500", description = "Erreur de serveur")
    })


    public ResponseEntity<?> enrollSignataire_V2(@Valid @RequestBody SignataireRequest signataireRequest, HttpServletRequest httpServletRequest) throws Exception {
        RestTemplate restTemplate = new RestTemplate(customFact.getClientHttpRequestFactory());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String action = "Operation d'enrôlement";

       // System.out.println("FFFFFFF: "+user.getUsername());
        String cle_de_signature = "userkey_" + signataireRequest.getNomSignataire().trim().toUpperCase().replaceAll("\\s+", "_");
        String username = signataireRequest.getNomSignataire() + "_" + signataireRequest.getNom_entreprise().toUpperCase().replaceAll("\\s+", "_");
        String alias = prop.getProperty("aliasCle") + signataireRequest.getNomSignataire().trim().toUpperCase().replaceAll("\\s+", "_");

        long pin = pin_generation();
        String cle_de_signature2 = prop.getProperty("aliasCle") + decouper_nom(signataireRequest.getNomSignataire().trim().toUpperCase()) + "_" + decouper_nom(signataireRequest.getNom_entreprise().toUpperCase().trim());
        if (cle_de_signature2.length() > 30) {
            cle_de_signature2 = cle_de_signature2.substring(0, 30);
        }
        String alias2 = cle_de_signature2;

        if (isExistSignerKey(alias2)) {
            String conflictMessage = "La clé de signature " + alias2 + " existe déjà";
            logger.info(conflictMessage);
            gestLogs(httpServletRequest, action,"Echec: "+conflictMessage);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflictMessage);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObtenirCertRequest obtenirCertRequest = new ObtenirCertRequest();
            Signataire signataire = new Signataire();

            if (!validateEmail(signataireRequest.getEmail())) {
                String badRequestMessage = "Vérifiez le format de l'email";
                logger.warn(badRequestMessage);
                gestLogs(httpServletRequest, action, "Echec: "+badRequestMessage);
                return ResponseEntity.badRequest().body(badRequestMessage);
            }

            if (!signataireRepository.findByCleDeSignature(cle_de_signature2).isEmpty()) {
                String conflictMessage = "Vous essayez d'enrôler une personne déjà existante !";
                logger.info(conflictMessage);
                gestLogs(httpServletRequest, action, "Echec: "+conflictMessage);
                return ResponseEntity.badRequest().body(conflictMessage);
            }

            signataire.setNomSignataire(signataireRequest.getNomSignataire());
            signataire.setCategorie(signataireRequest.getCategorie());
            signataire.setNomApplication(signataireRequest.getTrustedApp());
            signataire.setCode_pin(encrypterPin(signataireRequest.getCode_pin()));
            signataire.setEmail(signataireRequest.getEmail());
            signataire.setNomEntreprise(signataireRequest.getNom_entreprise());
            signataire.setCleDeSignature(cle_de_signature2);
            signataire.setCniPassport(signataireRequest.getCniPassport());

            obtenirCertRequest.setCertificate_authority_name(prop.getProperty("certificate_authority_name"));
            obtenirCertRequest.setCertificate_profile_name(prop.getProperty("certificate_profile_name_CE"));
            obtenirCertRequest.setEnd_entity_profile_name(prop.getProperty("end_entity_profile_name_CE"));
            obtenirCertRequest.setInclude_chain(true);
            obtenirCertRequest.setUsername(username);
            obtenirCertRequest.setPassword(signataireRequest.getPassword());
            String subjectDN = "CN=" + signataireRequest.getNomSignataire() + ",O=" + signataireRequest.getNom_entreprise() + ",C=SN";

            String signKey = generateCryptoToken(alias2);
            signataire.setSignerKey(alias2);
            obtenirCertRequest.setCertificate_request(webServiceConnect(subjectDN, signKey));

            HttpEntity<ObtenirCertRequest> httpEntity = new HttpEntity<>(obtenirCertRequest, headers);
            ResponseEntity<String> response = null;
            try{
                response  = restTemplate.postForEntity(prop.getProperty("lien_api_ejbca_enroll"), httpEntity, String.class);
            }catch (Exception e){
                String errorMessage = "An error has occcured. Veuillez réessayer.";
                if(isExistSignerKey(alias)){
                    logger.info("Suppression de la clé existante pour l'alias : {}", alias);
                    deleteKeySigner(Integer.parseInt(prop.getProperty("idWorkerPourSupprimerSignerKey")),alias);
                    System.out.println("Suppression de la clé existante pour l'alias :" +alias);
                }
                System.out.println(errorMessage);
                logger.error(errorMessage);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorMessage);
            }


            EnrollResponse enrollResponse = objectMapper.readValue(response.getBody(), EnrollResponse.class);
            List<String> certificateChain = enrollResponse.getCertificate_chain();
            String chaineCertificat = formatCert(enrollResponse.getCertificate());

            List<String> certificateListPem = new ArrayList<>();
            certificateListPem.add(enrollResponse.getCertificate());
            if(certificateChain.size() == 2){
                // System.out.println("Import chaine");
                certificateListPem.add(certificateChain.get(0));
                certificateListPem.add(certificateChain.get(1));

            }
            importChaine(certificateListPem, alias2);

            HttpStatus statusCode = (HttpStatus) response.getStatusCode();
            int statusCodeValue = statusCode.value();

            if (statusCodeValue == 201) {
                Date date_creation = new Date();
                setDateSignataire(sdf, signataire, enrollResponse, date_creation);
                //signataire.setDate_expiration(calculerDateExpiration2(sdf.format(date_creation)));
                signataireRepository.save(signataire);
                logger.info("Enrollment avec succès: " + response.getBody());
                gestLogs(httpServletRequest, action, "Enrôlement réussi");
            } else {
                logger.error("Enrollment echoué: " + response.getBody());
                gestLogs(httpServletRequest, action, "Echec: Erreur Server");
            }

            return response;

        } catch (HttpServerErrorException e) {
            String errorMessage = "Opération échouée! Veuillez réessayer.";
            if(isExistSignerKey(alias)){
                deleteKeySigner(Integer.parseInt(prop.getProperty("idWorkerPourSupprimerSignerKey")),alias);
            }
            logger.error(errorMessage);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorMessage);
        }
        catch (HttpStatusCodeException e) {
            String errorMessage2 = "Erreur HTTP survenue: \nOpération échouée! Veuillez réessayer.";
            String errorMessage = "Erreur HTTP survenue: " + e.getResponseBodyAsString();
            if(isExistSignerKey(alias)){
                deleteKeySigner(Integer.parseInt(prop.getProperty("idWorkerPourSupprimerSignerKey")),alias);
            }
            logger.error(errorMessage, e);
            return ResponseEntity.status(e.getStatusCode()).body(errorMessage2);
        } catch (Exception e) {
            String errorMessage2 = "Erreur HTTP survenue: \nOpération échouée! Veuillez réessayer.";
            String generalErrorMessage = "Une erreur inattendue est apparue: " + e.getMessage();
            if(isExistSignerKey(alias)){
                deleteKeySigner(Integer.parseInt(prop.getProperty("idWorkerPourSupprimerSignerKey")),alias);
            }
            logger.error(generalErrorMessage, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage2);
        }
    }

    private void setDateSignataire(SimpleDateFormat sdf, Signataire signataire, EnrollResponse enrollResponse, Date date_creation) throws Exception {
        signataire.setDateCreation(sdf.format(date_creation));
        String siExpiration7Jours = prop.getProperty("expiration_certificat");
        if (siExpiration7Jours ==  "1"){
            signataire.setDate_expiration(calculerDateExpiration2(sdf.format(date_creation)));
        }
        else{
            X509Certificate certif = convertStringToX509(enrollResponse.getCertificate());
            //System.out.println("Total expiration :"+certif.getNotAfter());
            signataire.setDate_expiration(sdf.format(certif.getNotAfter()));
        }
    }

    ///////////////////AFFICHER L'ENSEMBLE DES NOMS DES WORKERS/////////////////////////////////////
    @GetMapping("getAllNomWorkers")
    @Operation(hidden = true)
    public List<String> findAllWorkers(){
        return workerRepository.findAllByNomWorker();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////REMOVE KEY////////////////////////////////////////////////////
    @PostMapping("deleteSigner/{idWorker}/{alias}")
    public boolean deleteKeySigner(@PathVariable int idWorker, @PathVariable String alias) throws MalformedURLException{
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
    ///////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////INSERTION ANCIENS SIGNATAIRES/////////////////////////////////////////////////////
    @PostMapping("enroll_V3")
   // @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public void enrollSignataire_V3(@Valid @RequestBody List<SignataireRequest> signataireRequest1) throws Exception {
        for(SignataireRequest signataireRequest : signataireRequest1){
            RestTemplate restTemplate = new RestTemplate(customFact.getClientHttpRequestFactory());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            //headers.add("X-Keyfactor-Requested-With","");
            long pin = pin_generation();
            String cle_de_signature = "userkey_" + signataireRequest.getNomSignataire().trim().toUpperCase().replaceAll("\\s+", "_")+"_"+pin ;
            String username = signataireRequest.getNomSignataire() + "_" + signataireRequest.getNom_entreprise().toUpperCase().replaceAll("\\s+", "_");
            //GENERER CLE DE SIGNATURE
            String alias = cle_de_signature;
            ObjectMapper objectMapper = new ObjectMapper();

            ObtenirCertRequest obtenirCertRequest = new ObtenirCertRequest();
            Signataire signataire = new Signataire();
            //////////////Infos pour le signataire/////////////////////////////////

            signataire.setNomSignataire(signataireRequest.getNomSignataire());
            signataire.setCategorie(signataireRequest.getCategorie());
            //signataire.setApplication_rattachee(separer_idapp_nomapp(signataireRequest.getTrustedApp())[0]);
            signataire.setNomApplication(signataireRequest.getTrustedApp());
            signataire.setCode_pin(encrypterPin(signataireRequest.getCode_pin()));
            signataire.setEmail(signataireRequest.getEmail());
            signataire.setNomEntreprise(signataireRequest.getNom_entreprise());
            signataire.setCleDeSignature(cle_de_signature);

            //////////////////////////////////////////////////////////////////////
            //////////////Infos pour obtenir certificat////////////////////////////
            obtenirCertRequest.setCertificate_authority_name(prop.getProperty("certificate_authority_name"));
            obtenirCertRequest.setCertificate_profile_name(prop.getProperty("certificate_profile_name"));
            obtenirCertRequest.setEnd_entity_profile_name(prop.getProperty("end_entity_profile_name"));
            obtenirCertRequest.setInclude_chain(true);
            obtenirCertRequest.setUsername(username);
            obtenirCertRequest.setPassword(signataireRequest.getPassword());
            //obtenirCertRequest.setCertificate_request(prop.getProperty("csr"));
            String subjectDN = "CN=" + signataireRequest.getNomSignataire() + ",O=" + signataireRequest.getNom_entreprise() + ",C=SN";
            //System.out.println(Connect_WS(subjectDN));

            String signKey = generateCryptoToken(alias);
            signataire.setSignerKey(alias);
            obtenirCertRequest.setCertificate_request(webServiceConnect(subjectDN,signKey));
            /////////////////////////////////////////////////////////////////////////
            /////////////////envoie req pour avoir certificat////////////////////////
            HttpEntity<ObtenirCertRequest> httpEntity = new HttpEntity<>(obtenirCertRequest, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(prop.getProperty("lien_api_ejbca_enroll"), httpEntity, String.class);
            //////////TRaiter la réponse de la requete
            EnrollResponse enrollResponse = objectMapper.readValue(response.getBody(),EnrollResponse.class);
            List<String> certificateChain = enrollResponse.getCertificate_chain();
            String chaineCertificat =formatCert(enrollResponse.getCertificate());

            List<String> certificateListPem = new ArrayList<>();
       /* String cleanedCertificatePem="";
        for(String certif : certificateChain){
            chaineCertificat+="\n"+certif;
            // Nettoyer la chaîne de certificat des sauts de ligne
            cleanedCertificatePem =cleanedCertificatePem+chaineCertificat.replaceAll("\\s", "");

        }*/
            certificateListPem.add(enrollResponse.getCertificate());
            importChaine(certificateListPem,alias);
            //System.out.println(chaineCertificat);
            HttpStatus statusCode = (HttpStatus) response.getStatusCode();
            int statusCodeValue = statusCode.value();
            //System.out.println("CODEEEEEE :" + statusCodeValue);
            if (statusCodeValue == 201) {
                Date date_creation = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                signataire.setDateCreation(sdf.format(date_creation));
                signataire.setDate_expiration(calculerDateExpiration2(sdf.format(date_creation)));
                signataireRepository.save(signataire);
            }

            /////////////////////////////////////////////////////////////////////////

        }

    }

    public String decouper_nom(String nomAChanger){
        if(nomAChanger.contains(" ")){
            String[] caract = nomAChanger.split(" ");
            nomAChanger = caract[0]+ "_";
            for(int i = 1; i < caract.length ; i++){
                nomAChanger += caract[i].charAt(0) ;
            }
        }
        if (nomAChanger.length() > 30){
            nomAChanger = nomAChanger.substring(0,30);
        }

        return nomAChanger;
    }
    public static X509Certificate convertStringToX509(String pemCert) throws Exception {

        // Décoder la chaîne Base64 en tableau de bytes
        byte[] certBytes = Base64.getDecoder().decode(pemCert);

        // Convertir en certificat X.509
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }


}
