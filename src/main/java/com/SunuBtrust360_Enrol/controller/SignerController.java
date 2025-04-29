package com.SunuBtrust360_Enrol.controller;


import com.SunuBtrust360_Enrol.config.CustomHttpRequestFactory;
import com.SunuBtrust360_Enrol.models.*;
import com.SunuBtrust360_Enrol.repository.*;
import com.SunuBtrust360_Enrol.payload.request.ObtenirCertRequest_V2;
import com.SunuBtrust360_Enrol.payload.request.SignataireRequest_V2;
import com.SunuBtrust360_Enrol.services.CertService;
import com.SunuBtrust360_Enrol.utils.GestSignataire;
import com.SunuBtrust360_Enrol.wsdl.*;
import com.SunuBtrust360_Enrol.wsdl_client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.auth.HttpAuthHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.validation.Valid;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.KeyStoreException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 13/02/2024/02/2024 - 16:06
 */
@RestController
@RequestMapping("/v0.0.2/signer/")
@CrossOrigin(origins = "http://localhost:8080")
@Api("API de signature")
@Hidden
public class SignerController {
    @RequestMapping("/")
    public String hello(){
        return "Goooooooooood";
    }
    private static final String ALGORITHM = "AES";
    private static final String MODE = "CBC";
    private static final String PADDING = "PKCS5Padding";
    private static final String CIPHER_TRANSFORMATION = ALGORITHM + "/" + MODE + "/" + PADDING;
    private static final byte[] IV = new byte[16];
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(SignerController.class);
    Properties prop = null;
    Logger log = null;
    AdminWS port = null;

    CustomHttpRequestFactory customFact = new CustomHttpRequestFactory();
    @Autowired
    private final SignataireRepository_V2 signataireRepository_V2;
    @Autowired
    private final SignataireRepository signataireRepository;
    @Autowired
    private PieceIdentiteRepository pieceIdentiteRepository;

    @Autowired
    private InfosCertificatRepository infosCertificatRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private IdLastSigantaireRepository idLastSigantaireRepository;

    @Autowired
    private SignerStartupRepository signerStartupRepository;

    @Autowired
    private SignDocumentRepository signDocumentRepository;

    @Autowired
    private CertService certService;

    public SignerController(SignataireRepository_V2 signataireRepository, SignataireRepository signataireRepository1, WorkerRepository workerRepository, OperationRepository operationRepository, IdLastSigantaireRepository idLastSigantaireRepository,InfosCertificatRepository infosCertificatRepository) {
        this.signataireRepository_V2 = signataireRepository;
        this.signataireRepository = signataireRepository1;
        this.workerRepository = workerRepository;
        this.operationRepository = operationRepository;
        this.idLastSigantaireRepository = idLastSigantaireRepository;
        this.infosCertificatRepository = infosCertificatRepository;
        log = LogManager.getLogger(SignerController.class);
        log.debug("Registration class constructor");
        try (InputStream input = SignerController.class.getClassLoader().getResourceAsStream("configWin.properties")) {

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

    //Endpoint enrollement signataire
    ///////////////////////////////////////////////////////////////////////////////////
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
    }
    @PostMapping("enroll")
    @ApiOperation(value="Cette section permet à l’application métier qui a effectué un enrôlement de téléverser une copie d'un document d'identité pour un signataire existant dans le système. En téléversant une pièce d'identité, les opérateurs du centre d’enregistrement peuvent vérifier et authentifier l'identité du signataire. Cette fonctionnalité améliore les mesures de sécurité et garantit le respect des protocoles de vérification d'identité.")
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Le fichier a été téléchargé avec succès"),
            @ApiResponse(responseCode = "400", description = "L’ID du signataire n’existe pas ou aucun fichier n’a été fourni"),
            @ApiResponse(responseCode = "500", description = "Une erreur interne du serveur s’est produite")
    })
    public ResponseEntity<?> enrollSignataire_V2(@Valid @RequestBody SignataireRequest_V2 signataireRequest, BindingResult bindingResult) throws Exception {
        RestTemplate restTemplate = new RestTemplate(customFact.getClientHttpRequestFactory());
        HttpHeaders headers = new HttpHeaders();
        String nomSignataire = signataireRequest.getNomSignataire();
        String idAppAajouter ="";
        headers.setContentType(MediaType.APPLICATION_JSON);
        if(signataireRequest.getIdApplication() != null){
            nomSignataire = nomSignataire + signataireRequest.getIdApplication();
            idAppAajouter = signataireRequest.getIdApplication().toString();
        }
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
        String messageRetourDecouper = decouper_nom(signataireRequest.getNomSignataire().trim().toUpperCase()) + idAppAajouter+ "_" + signataireRequest.getCni();
        if (messageRetourDecouper.equals("Tableau vide!")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur: Informations absentes.");
        }
        String cle_de_signature2 = prop.getProperty("aliasCle") + messageRetourDecouper;

        if (cle_de_signature2.length() > 50){
            cle_de_signature2 = cle_de_signature2.substring(0, 50);
        }
        if (signataireRequest.getCni().length() > 15){
            String cniMessage = "Le numéro de cni ne doit pas dépasser 15 caractères";
            logger.info(cniMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cniMessage);
        }
        String alias = cle_de_signature2;
        ObjectMapper objectMapper = new ObjectMapper();

        if (isExistSignerKey(alias)){
            String conflictMessage = "La clé de signature " + alias + " existe déjà";
            logger.info(conflictMessage);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflictMessage);
        }

        try {
            ObtenirCertRequest_V2 obtenirCertRequest = new ObtenirCertRequest_V2();
            Signataire_V2 signataire = new Signataire_V2();

            Set<String> champsAttendus = new HashSet<>(Arrays.asList("nomSignataire", "cni", "telephone","idApplication"));
            for (Field field : SignataireRequest_V2.class.getDeclaredFields()) {
                if (!champsAttendus.contains(field.getName())) {
                    String badRequestMessage = "Champ supplementaire '" + field.getName() + "' non autorise.";
                    logger.warn(badRequestMessage);
                    return ResponseEntity.badRequest().body(badRequestMessage);
                }
            }
            if (signataireRequest.getNomSignataire() == null || signataireRequest.getNomSignataire().isEmpty() ||
                    signataireRequest.getCni() == null || signataireRequest.getCni().isEmpty()) {
                String badRequestMessage = "Verifiez si vous avez rempli toutes les informations";
                logger.warn(badRequestMessage);
                return ResponseEntity.badRequest().body(badRequestMessage);
            }

            if (!signataireRepository_V2.findSignataireByCni(signataireRequest.getCni()).isEmpty()
            && !trouverSignerParNomSigner(signataireRequest.getNomSignataire()+signataireRequest.getCni()).isEmpty()) {
                String conflictMessage = "Person already exists2!";
                logger.info(conflictMessage);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(conflictMessage);
            }

            String username = signataireRequest.getNomSignataire() + "_" + signataireRequest.getCni().toUpperCase().replaceAll("\\s+", "_");
            signataire.setNomSignataire(nomSignataire);
            Long pin = pin_generation();
            signataire.setCodePin(encrypterPin(pin.toString()));
            String signKey = generateCryptoToken(alias);
            System.out.println("SIGNNNNN: "+signKey);
            if (signKey.length() > 70){
                signKey = signKey.substring(0, 70);
            }
            signataire.setSignerKey(signKey);
            signataire.setCni(signataireRequest.getCni());
            signataire.setTelephone(signataireRequest.getTelephone());
            signataire.setIdApplication(Integer.valueOf(idAppAajouter));
            Worker worker = findNomWorkerById(signataireRequest.getIdApplication());
            signataire.setNomWorker(worker.getNomWorker());
            obtenirCertRequest.setCertificate_authority_name(prop.getProperty("certificate_authority_name"));
            obtenirCertRequest.setCertificate_profile_name(prop.getProperty("certificate_profile_name"));
            obtenirCertRequest.setEnd_entity_profile_name(prop.getProperty("end_entity_profile_name"));
            obtenirCertRequest.setInclude_chain(true);
            obtenirCertRequest.setUsername(username);
            obtenirCertRequest.setPassword(prop.getProperty("defaultPassword"));
            String subjectDN = "CN=" + signataireRequest.getNomSignataire() + ",O=" + signataireRequest.getCni() + ",C=SN";
            obtenirCertRequest.setCertificate_request(Connect_WS2(subjectDN, signKey));

            HttpEntity<ObtenirCertRequest_V2> httpEntity = new HttpEntity<>(obtenirCertRequest, headers);
            ResponseEntity<String> response = null;
            try{
                response  = restTemplate.postForEntity(prop.getProperty("lien_api_ejbca_enroll"), httpEntity, String.class);
            }catch (Exception e){
                String errorMessage = "An error has occcured. Veuillez réessayer.";
                if(isExistSignerKey(alias)){
                    logger.info("Suppression de la clé existante pour l'alias : {}", alias);
                    deleteKeySigner(Integer.parseInt(prop.getProperty("idWorkerPourSupprimerSignerKey")),alias);
                    //System.out.println("Suppression de la clé existante pour l'alias :" +alias);
                }
                System.out.println(errorMessage);
                logger.error(errorMessage);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorMessage);
            }




//            if(response.getBody() == null || response.getBody().isBlank()){
//                String errorMessage = "Opération échouée! Veuillez réessayer.";
//                if(isExistSignerKey(alias)){
//                    logger.info("Suppression de la clé existante pour l'alias : {}", alias);
//                    deleteKeySigner(Integer.parseInt(prop.getProperty("idWorkerPourSupprimerSignerKey")),alias);
//                }
//                logger.error(errorMessage);
//                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorMessage);
//            }
            EnrollResponse_V2 enrollResponse = objectMapper.readValue(response.getBody(), EnrollResponse_V2.class);
            enrollResponse.setCodePin(pin.toString());
            List<String> certificateChain = enrollResponse.getCertificate_chain();
            String chaineCertificat = formatCert(enrollResponse.getCertificate());
            List<String> certificateListPem = new ArrayList<>();
            certificateListPem.add(enrollResponse.getCertificate());
            if(certificateChain.size() == 2){
               // System.out.println("Import chaine");
                certificateListPem.add(certificateChain.get(0));
                certificateListPem.add(certificateChain.get(1));

            }


            importChaine(certificateListPem, alias);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                Date date_creation = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                signataire.setDateCreation(sdf.format(date_creation));
                String siExpiration7Jours = prop.getProperty("expiration_certificat");
                if (siExpiration7Jours ==  "1"){
                    signataire.setDateExpiration(calculerDateExpirationJours(sdf.format(date_creation)));
                }
                else{
                    X509Certificate certif = convertStringToX509(enrollResponse.getCertificate());
                    //System.out.println("Total expiration :"+certif.getNotAfter());
                    signataire.setDateExpiration(sdf.format(certif.getNotAfter()));
                }
                Signataire_V2 lastSigner = signataireRepository_V2.findLast();
                List<IdLastSignataire> listeIdLastSignataire = idLastSigantaireRepository.findAll();
                IdLastSignataire idLastSignataire = new IdLastSignataire();
                if (listeIdLastSignataire.isEmpty()) {
                    idLastSignataire.setLastIdSignataire(lastSigner.getIdSigner()+1);
                    idLastSigantaireRepository.save(idLastSignataire);
                }
                else{
                    idLastSignataire = idLastSigantaireRepository.findById(1);
                    idLastSignataire.setLastIdSignataire(idLastSignataire.getLastIdSignataire()+1);
                    idLastSigantaireRepository.save(idLastSignataire);
                }
                signataire.setIdSigner(idLastSignataire.getLastIdSignataire());
                signataire = signataireRepository_V2.save(signataire);


                enrollResponse.setId_signer(idLastSignataire.getLastIdSignataire());
                String responseBodyWithCodePin = objectMapper.writeValueAsString(enrollResponse);
                logger.info("Enrollment avec succès: " + responseBodyWithCodePin);
                return new ResponseEntity<>(responseBodyWithCodePin, HttpStatus.OK);
            } else {
                logger.error("Enrollment echoué: " + response.getBody());
                return new ResponseEntity<>(response.getBody(), response.getStatusCode());
            }

        }catch (HttpServerErrorException e) {
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
    @PostMapping("renew")
    public ResponseEntity<?> renewSignataire_V2(@Valid @RequestBody SignataireRequest_V2 signataireRequest, BindingResult bindingResult) throws Exception {
        RestTemplate restTemplate = new RestTemplate(customFact.getClientHttpRequestFactory());
        HttpHeaders headers = new HttpHeaders();
        String nomSignataire = signataireRequest.getNomSignataire();
        String idAppAajouter ="";
        GestSignataire gst = new GestSignataire();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
        String cle_de_signature2 = "";
        int id_app = 0;
        if (signataireRepository_V2.existsByCni(signataireRequest.getCni())) {

            int idApp = signataireRequest.getIdApplication();
            List<Signataire_V2> signataireList = signataireRepository_V2.findByNomSignataire(signataireRequest.getNomSignataire());
            List<Signataire_V2> signataireListCNI = signataireRepository_V2.findSignataireByCni(signataireRequest.getCni());
            //boolean a = signataireListCNI.get(0).getNomSignataire().equals(signataireRequest.getNomSignataire()+signataireRequest.getIdApplication());
            System.out.println("C BONNN"+ signataireListCNI.get(0).getNomSignataire());
            System.out.println("C BONNN"+ signataireRequest.getNomSignataire());
            Signataire_V2 signer = null;
            if(signataireListCNI.get(0).getNomSignataire().equals(signataireRequest.getNomSignataire())){
                signer = signataireListCNI.get(0);
            }
            System.out.println("Cccccc"+ signer.getNomSignataire());
            cle_de_signature2 = signer.getSignerKey();
            nomSignataire = signer.getNomSignataire();
            id_app = signer.getIdApplication();
        }
        String alias = cle_de_signature2;
        ObjectMapper objectMapper = new ObjectMapper();


        try {
            ObtenirCertRequest_V2 obtenirCertRequest = new ObtenirCertRequest_V2();
            Signataire_V2 signataire = new Signataire_V2();

            Set<String> champsAttendus = new HashSet<>(Arrays.asList("nomSignataire", "cni", "telephone","idApplication"));
            for (Field field : SignataireRequest_V2.class.getDeclaredFields()) {
                if (!champsAttendus.contains(field.getName())) {
                    String badRequestMessage = "Champ supplementaire '" + field.getName() + "' non autorise.";
                    logger.warn(badRequestMessage);
                    return ResponseEntity.badRequest().body(badRequestMessage);
                }
            }
            if (signataireRequest.getNomSignataire() == null || signataireRequest.getNomSignataire().isEmpty() ||
                    signataireRequest.getCni() == null || signataireRequest.getCni().isEmpty()) {
                String badRequestMessage = "Verifiez si vous avez rempli toutes les informations";
                logger.warn(badRequestMessage);
                return ResponseEntity.badRequest().body(badRequestMessage);
            }



            String username = signataireRequest.getNomSignataire() + "_" + signataireRequest.getCni().toUpperCase().replaceAll("\\s+", "_");
            signataire.setNomSignataire(nomSignataire);
            Long pin = pin_generation();
            signataire.setCodePin(encrypterPin(pin.toString()));

            deleteKeySigner(id_app, alias);
            String signKey = generateCryptoToken(alias);
            System.out.println("SIGNNNNN: "+signKey);
            if (signKey.length() > 70){
                signKey = signKey.substring(0, 70);
            }
            signataire.setSignerKey(signKey);
            signataire.setCni(signataireRequest.getCni());
            signataire.setTelephone(signataireRequest.getTelephone());
            signataire.setIdApplication(id_app);

            obtenirCertRequest.setCertificate_authority_name(prop.getProperty("certificate_authority_name"));
            obtenirCertRequest.setCertificate_profile_name(prop.getProperty("certificate_profile_name"));
            obtenirCertRequest.setEnd_entity_profile_name(prop.getProperty("end_entity_profile_name"));
            obtenirCertRequest.setInclude_chain(true);
            obtenirCertRequest.setUsername(username);
            obtenirCertRequest.setPassword(prop.getProperty("defaultPassword"));
            String subjectDN = "CN=" + signataireRequest.getNomSignataire() + ",O=" + signataireRequest.getCni() + ",C=SN";
            obtenirCertRequest.setCertificate_request(Connect_WS2(subjectDN, signKey));

            HttpEntity<ObtenirCertRequest_V2> httpEntity = new HttpEntity<>(obtenirCertRequest, headers);
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


            EnrollResponse_V2 enrollResponse = objectMapper.readValue(response.getBody(), EnrollResponse_V2.class);
            enrollResponse.setCodePin(pin.toString());
            List<String> certificateChain = enrollResponse.getCertificate_chain();
            String chaineCertificat = formatCert(enrollResponse.getCertificate());

            List<String> certificateListPem = new ArrayList<>();
            certificateListPem.add(enrollResponse.getCertificate());
            if(certificateChain.size() == 2){
                // System.out.println("Import chaine");
                certificateListPem.add(certificateChain.get(0));
                certificateListPem.add(certificateChain.get(1));

            }

            importChaine(certificateListPem, alias);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                Date date_creation = new Date();
                String dateExp = null;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                signataire.setDateCreation(sdf.format(date_creation));
                String siExpiration7Jours = prop.getProperty("expiration_certificat");
                if (siExpiration7Jours ==  "1"){
                    signataire.setDateExpiration(calculerDateExpirationJours(sdf.format(date_creation)));
                }
                else{
                    X509Certificate certif = convertStringToX509(enrollResponse.getCertificate());
                    //System.out.println("Total expiration :"+certif.getNotAfter());
                    signataire.setDateExpiration(sdf.format(certif.getNotAfter()));
                    dateExp = sdf.format(certif.getNotAfter());
                }
                gst.updateRenouveler2(signataire.getSignerKey(), signataire.getCodePin(), sdf.format(date_creation), dateExp);

                // signataire = signataireRepository_V2.save(signataire);
                //enrollResponse.setId_signer((int) signataireRepository_V2.count());
                String responseBodyWithCodePin = objectMapper.writeValueAsString(enrollResponse);
                logger.info("Renouvellement avec succès: " + responseBodyWithCodePin);
                return new ResponseEntity<>(responseBodyWithCodePin, HttpStatus.OK);
            } else {
                logger.error("Renouvellemen echoué: " + response.getBody());
                return new ResponseEntity<>(response.getBody(), response.getStatusCode());
            }

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
    /*public ResponseEntity<?> enrollSignataire_V2(@Valid @RequestBody SignataireRequest_V2 signataireRequest, BindingResult bindingResult) throws Exception {
        RestTemplate restTemplate = new RestTemplate(customFact.getClientHttpRequestFactory());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.add("X-Keyfactor-Requested-With","");
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
        //GENERER CLE DE SIGNATURE SUR SIGNSERVER
        //String alias = prop.getProperty("aliasCle") + signataireRequest.getNomSignataire().trim().toUpperCase().replaceAll("\\s+", "_");
        String cle_de_signature2 = prop.getProperty("aliasCle") + decouper_nom(signataireRequest.getNomSignataire().trim().toUpperCase());
        //GENERER CLE DE SIGNATURE
        if (cle_de_signature2.length() > 30){
            cle_de_signature2 = cle_de_signature2.substring(0,30);
        }
        String alias = cle_de_signature2;
        ObjectMapper objectMapper = new ObjectMapper();

        if (isExistSignerKey(alias)){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("La clé de signature "+alias+" existe déja");
        }

        ObtenirCertRequest_V2 obtenirCertRequest = new ObtenirCertRequest_V2();
        Signataire_V2 signataire = new Signataire_V2();
        //////////////Infos pour le signataire/////////////////////////////////

        // Vérifier les champs supplémentaires
        Set<String> champsAttendus = new HashSet<>(Arrays.asList("nomSignataire", "cni", "telephone"));

        for (Field field : SignataireRequest_V2.class.getDeclaredFields()) {
            if (!champsAttendus.contains(field.getName())) {
                return ResponseEntity.badRequest().body("Champ supplementaire '" + field.getName() + "' non autorise.");
            }
        }
        if (signataireRequest.getNomSignataire()==null || signataireRequest.getNomSignataire()=="" || (signataireRequest.getCni()==null) || (signataireRequest.getCni()=="")) {
            return ResponseEntity.badRequest().body("Verifiez si vous avez rempli toutes les informations");
        }
        if (!signataireRepository_V2.findSignataireByCni(signataireRequest.getCni()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Person already exists!");
        }
        String username = signataireRequest.getNomSignataire() + "_" + signataireRequest.getCni().toUpperCase().replaceAll("\\s+", "_");
        signataire.setNomSignataire(signataireRequest.getNomSignataire());
        Long pin = pin_generation();
        signataire.setCodePin(encrypterPin(pin.toString()));
        String signKey = generateCryptoToken(alias);
        signataire.setSignerKey(signKey);
        signataire.setCni(signataireRequest.getCni());
        signataire.setTelephone(signataireRequest.getTelephone());

        //////////////////////////////////////////////////////////////////////
        //////////////Infos pour obtenir certificat////////////////////////////
        obtenirCertRequest.setCertificate_authority_name(prop.getProperty("certificate_authority_name"));
        obtenirCertRequest.setCertificate_profile_name(prop.getProperty("certificate_profile_name"));
        obtenirCertRequest.setEnd_entity_profile_name(prop.getProperty("end_entity_profile_name"));
        obtenirCertRequest.setInclude_chain(true);
        obtenirCertRequest.setUsername(username);
        obtenirCertRequest.setPassword(prop.getProperty("defaultPassword"));
        //obtenirCertRequest.setCertificate_request(prop.getProperty("csr"));
        String subjectDN = "CN=" + signataireRequest.getNomSignataire() + ",O=" + signataireRequest.getCni() + ",C=SN";
        //System.out.println(Connect_WS(subjectDN));

        obtenirCertRequest.setCertificate_request(Connect_WS2(subjectDN,signKey));
        /////////////////////////////////////////////////////////////////////////
        /////////////////envoie req pour avoir certificat////////////////////////
        HttpEntity<ObtenirCertRequest_V2> httpEntity = new HttpEntity<>(obtenirCertRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(prop.getProperty("lien_api_ejbca_enroll"), httpEntity, String.class);
        //////////TRaiter la réponse de la requete
        EnrollResponse_V2 enrollResponse = objectMapper.readValue(response.getBody(), EnrollResponse_V2.class);
        enrollResponse.setCodePin(pin.toString());

        String responseBodyWithCodePin = "";
        List<String> certificateChain = enrollResponse.getCertificate_chain();
        String chaineCertificat =formatCert(enrollResponse.getCertificate());
        List<String> certificateListPem = new ArrayList<>();
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
            signataire.setDateExpiration(calculerDateExpiration2(sdf.format(date_creation)));
            signataire = signataireRepository_V2.save(signataire);
           // System.out.println("ID SIGNERRR: "+(int) signataireRepository_V2.count());
            enrollResponse.setId_signer((int) signataireRepository_V2.count());

            // Convertissez l'objet EnrollResponse mis à jour en une chaîne JSON
            responseBodyWithCodePin = objectMapper.writeValueAsString(enrollResponse);
        }

        /////////////////////////////////////////////////////////////////////////
        return new ResponseEntity<>(responseBodyWithCodePin, HttpStatus.OK);
    }*/
    //Calculer la duree du certficat
    /////////////////////////////////////////////////////////////////////////////////////////
    private List<byte[]> decodeBase64List(List<String> inputList) {
        List<byte[]> resultList = new ArrayList<>();
        for (String inputString : inputList) {
            byte[] certificateBytes = Base64.getDecoder().decode(inputString.getBytes(StandardCharsets.UTF_8));
            resultList.add(certificateBytes);
        }
        return resultList;
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
    public String calculerDateExpirationJours(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime initialDate = LocalDateTime.parse(dateString, formatter);

        // Ajoutez une semaine à la date initiale
        LocalDateTime resultDate = initialDate.plusWeeks(Long.parseLong(prop.getProperty("expiration_certificat")));

        // Formatez la date résultante pour l'affichage
        return resultDate.format(formatter);
    }
    /////////////////////////////////////////////////////////////////////////////////////////
    //Importer le certficat
    ///////////////////////////////////////////////////////////////////////////////////
    @PostMapping("importChaine/{chaine}/{alias}")
    public void importChaine(@PathVariable List<String> chaine,@PathVariable String alias) throws MalformedURLException {
        // TODO Auto-generated method stub
        System.setProperty("javax.net.ssl.keyStore", prop.getProperty("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("password_keystore"));
        System.setProperty("javax.net.ssl.trustStore", prop.getProperty("trustore1"));
        System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("password_keystore"));
        URL wsdlLocation = new URL(prop.getProperty("wsdlUrl"));
        AdminWSService service = new AdminWSService(wsdlLocation);
        port = service.getPort(AdminWS.class);
        try {
            setupTLS(port);
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
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///Formatter Certificat
    /////////////////////////////////////////////////////////////////////////////////////
    public String formatCert(String csr) {
        // Ajouter les délimiteurs
        String beginDelimiter = "-----BEGIN CERTIFICATE-----";
        String endDelimiter = "-----END CERTIFICATE-----";

        return beginDelimiter + "\n" + insertNewLines(csr) + "\n" + endDelimiter;
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
    /////////////////////////////////////////////////////////////////////////////////////
    ///Methode pour chiffrer le pin de l'utilisateur
    /////////////////////////////////////////////////////////////////////////////////////
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    private SecretKey getSecretKey(String keyString) {
        byte[] keyBytes = hexStringToByteArray(keyString);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
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
    @PostMapping("decrypt/{pinEncrypted}")
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
    ////////////////////////////////////////////////////////////////////////////////////
    ///Générer code pin
    //////////////////////////////////////////////////////////////////////////////////////
    @PostMapping("generer_pin")
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
    ///////////////////////////////////////////////////////////////////////////////////////

    //Generer une clé de signature en basant sur le nom du signataire
    @PostMapping("getSignerKey/{alias}")
    public String generateCryptoToken(@PathVariable String alias) throws MalformedURLException {
        System.setProperty("javax.net.ssl.keyStore", prop.getProperty("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("password_keystore"));
        System.setProperty("javax.net.ssl.trustStore", prop.getProperty("trustore1"));
        System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("password_keystore"));
        URL wsdlLocation = new URL(prop.getProperty("wsdlUrl") );
        AdminWSService service = new AdminWSService(wsdlLocation);
        port = service.getPort(AdminWS.class);
        try {
            setupTLS(port);
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
        fac.init(keyStore, keyPass);
        return fac.getKeyManagers();
    }
    private void setupTLS(AdminWS port)
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
    private void setupTLS_sign(AdminWS port,String username)
            throws IOException, GeneralSecurityException {

        HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();
        // Set client certificate information for authentication (if required)
        AuthorizationPolicy authorizationPolicy = httpConduit.getAuthorization();
        authorizationPolicy.setAuthorizationType(HttpAuthHeader.AUTH_TYPE_BASIC); // Set the appropriate authorization type
        authorizationPolicy.setUserName(username);
        authorizationPolicy.setPassword("passe");
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
    private void setupTLS_sign_client(ClientWS port,String username)
            throws IOException, GeneralSecurityException {

        HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();
        TLSClientParameters tlsCP = new TLSClientParameters();
        //System.out.println(keyPassword);
        KeyStore keyStore = KeyStore.getInstance("JKS");
        String keyStoreLoc = prop.getProperty("keystore");
        String keyPassword = prop.getProperty("password_keystore");
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
        // Set client certificate information for authentication (if required)
        AuthorizationPolicy authorizationPolicy = httpConduit.getAuthorization();
        authorizationPolicy.setAuthorizationType(HttpAuthHeader.AUTH_TYPE_BASIC); // Set the appropriate authorization type
        authorizationPolicy.setUserName(username);
        authorizationPolicy.setPassword("passe");








    }
    public String formatCSR(String csr) {
        // Ajouter les délimiteurs
        String beginDelimiter = "-----BEGIN CERTIFICATE REQUEST-----";
        String endDelimiter = "-----END CERTIFICATE REQUEST-----";

        return beginDelimiter + "\n" + insertNewLines(csr) + "\n" + endDelimiter;
    }

    @PostMapping("avoirCSR2/{subjectDN}/{keyAlias}")
    public String Connect_WS2(@PathVariable String subjectDN, @PathVariable String keyAlias) throws MalformedURLException {
        // TODO Auto-generated method stub
        System.setProperty("javax.net.ssl.keyStore", prop.getProperty("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("password_keystore"));
        System.setProperty("javax.net.ssl.trustStore", prop.getProperty("trustore1"));
        System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("password_keystore"));
        URL wsdlLocation = new URL(prop.getProperty("wsdlUrl"));
        AdminWSService service = new AdminWSService(wsdlLocation);
        port = service.getPort(AdminWS.class);
        try {
            setupTLS(port);
        } catch (IOException | GeneralSecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String user_num = "1";
        Pkcs10CertReqInfo pkcinf = new com.SunuBtrust360_Enrol.wsdl.Pkcs10CertReqInfo();
        pkcinf.setSignatureAlgorithm("SHA256withRSA"); //"SHA256withECDSA"
        pkcinf.setSubjectDN(subjectDN);
        //PKCS10CertReqInfo certReqInfo = new PKCS10CertReqInfo("SHA1WithRSA", "CN=Worker" + WORKER_PDF, null);
        try {
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
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //Depot justificatifs
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    @PostMapping("depot/{idSignataire}")
    public ResponseEntity<String> uploadPieceIdentite(@PathVariable Integer idSignataire,@RequestParam("piece_cni") MultipartFile file){
        // Vérifier si l'ID du signataire est présent dans la table signataire
        if (!signataireRepository_V2.existsByIdSigner(idSignataire)) {
            return ResponseEntity.badRequest().body("Ce signataire n'existe pas dans nos bases de donnees!");
        }
        if (file.isEmpty()) {
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Fichier non existant !");
        }
        try {
            PieceIdentite pieceIdentite = new PieceIdentite();
            pieceIdentite.setScan(file.getBytes());
            pieceIdentite.setIdSignataire(idSignataire);
            pieceIdentiteRepository.save(pieceIdentite);
            return ResponseEntity.ok("Scan de la piece d'identite enregistre avec succès ");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'enregistrement du scan de la pièce d'identite : " + e.getMessage());
        }

    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////SIGNATURE DOCS///////////////////////////////////////////////////
    @PostMapping("signature/{id_signer}")
    public ResponseEntity<?> signDocument(@RequestParam(value="workerId",required = false) Integer idWorker,@RequestParam("filereceivefile") MultipartFile file,@RequestParam("codePin") String codePin,@PathVariable Integer id_signer) throws IOException {

        Optional<Signataire_V2> signataireV2Optional = signataireRepository_V2.findByIdSigner(id_signer);
        if(idWorker == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID Application introuvable !");
        }
        int workerId = idWorker != null ? idWorker.intValue() : 0;
        if(!signataireV2Optional.isPresent()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Utilisateur inconnu !");
        }
        Signataire_V2 signer = signataireV2Optional.get();
        if (!encrypterPin(codePin).equals(signer.getCodePin()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Veuillez fournir un bon code PIN !");
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Veuillez selectionner un fichier !");
        }
        if(!workerRepository.existsByIdWorker(idWorker)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID Application introuvable !");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        //headers.add("X-Keyfactor-Requested-With","");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("filereceivefile", new ByteArrayResource(file.getBytes()){
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("workerId", workerId);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate(customFact.getClientHttpRequestFactory(signer.getNomSignataire(),"passer"));
        String signatureUrl = prop.getProperty("signUrl");
        ResponseEntity<byte[]> response = restTemplate.postForEntity(signatureUrl, requestEntity, byte[].class);
        /////Remplissage table operation
        OperationSignature operationSignature = new OperationSignature();
        operationSignature.setIdSigner(id_signer);
        operationSignature.setCodePin(signer.getCodePin());
        operationSignature.setSignerKey(signer.getSignerKey());
        Date dateOp = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        operationSignature.setDateOperation(sdf.format(dateOp));

        operationRepository.save(operationSignature);

        return response;
    }


    @PostMapping("signature2/{id_signer}")
    public ResponseEntity<?> signature_V2(@RequestParam(value="workerId",required = false) Integer idWorker,@RequestParam("filereceivefile") MultipartFile file,@RequestParam("codePin") String codePin,@PathVariable Integer id_signer) throws Exception {
        Optional<Signataire_V2> signataireV2Optional = signataireRepository_V2.findByIdSigner(id_signer);
        if(idWorker == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID Application introuvable !");
        }
        int workerId = idWorker != null ? idWorker.intValue() : 0;
        if(!signataireV2Optional.isPresent()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Utilisateur inconnu !");
        }
        Signataire_V2 signer = signataireV2Optional.get();
        if (!encrypterPin(codePin).equals(signer.getCodePin()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Veuillez fournir un bon code PIN !");
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Veuillez selectionner un fichier !");
        }
        if(!workerRepository.existsByIdWorker(idWorker)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID Application introuvable !");
        }
       // HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // TODO Auto-generated method stub
        System.setProperty("javax.net.ssl.keyStore", prop.getProperty("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("password_keystore"));
        System.setProperty("javax.net.ssl.trustStore", prop.getProperty("trustore1"));
        System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("password_keystore"));
        URL wsdlLocation = new URL("http://10.10.0.16/signserver/ClientWSService/ClientWS?wsdl");
        System.out.println(wsdlLocation);
        ClientWSService service = new ClientWSService(wsdlLocation);
        ClientWS  port_client = service.getClientWSPort();
        try {
            setupTLS_sign_client(port_client,signer.getNomSignataire());
        } catch (IOException | GeneralSecurityException e1) {
            e1.printStackTrace();
        }
        byte[] fileBytes = file.getBytes();
        List<byte[]> bytesFile = new ArrayList<>();
        bytesFile.add(fileBytes);
        //System.out.println("FILEEEEEEE: "+idWorker.toString());
        String workerName = "PDFSigner_Wrap";
        DataResponse response =  port_client.processData("PDFSigner_Wrap",null,fileBytes);
        /////Remplissage table operation
        OperationSignature operationSignature = new OperationSignature();
        operationSignature.setIdSigner(id_signer);
        operationSignature.setCodePin(signer.getCodePin());
        operationSignature.setSignerKey(signer.getSignerKey());
        Date dateOp = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        operationSignature.setDateOperation(sdf.format(dateOp));

        operationRepository.save(operationSignature);

        return ResponseEntity.ok(response) ;

    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping("signature3/{commonNamePrenom}")
    public void Signature_base(@PathVariable String commonNamePrenom) {
        final String serviceURL = "https://10.10.0.16:443/signserver/ClientWSService/ClientWS";
        String keyStoreLocation = prop.getProperty("keystore");
        String trustStoreLocation = prop.getProperty("trustore1");

        String password = prop.getProperty("password_keystore");


            String filename = keyStoreLocation + "ejbca_auth_jks.jks";
            String filenameTrust = keyStoreLocation + "ejbca_truststore.jks";
            System.setProperty("javax.net.ssl.keyStore", keyStoreLocation);
            System.setProperty("javax.net.ssl.keyStorepassword", password);
            System.setProperty("javax.net.ssl.trustStore", trustStoreLocation);
            System.setProperty("javax.net.ssl.trustStorePassword", password);


        //QName serviceName = new QName("http://www.confiancefactory.com/", "SignServerUser_Cert");
        URL wsdlURL=null;
        try {
            wsdlURL = new URL(serviceURL + "?wsdl");
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ClientWSService service = new ClientWSService(wsdlURL);
        ClientWS port = service.getClientWSPort();
        try {
            setupTLS(port, keyStoreLocation, password, commonNamePrenom);
        } catch (IOException | GeneralSecurityException e1) {
            // TODO Auto-generated catch block
            //log.error(e1.getMessage());
            e1.printStackTrace();
        }


        try {
            byte[] byteArrayDocument=null;
            try {
                //byteArrayDocument = Files.readAllBytes(Paths.get("D://ProJs//SigningServer//LS33600.pdf"));
                byteArrayDocument = Files.readAllBytes(Paths.get("D:\\DOC009.pdf"));

                System.out.println("byteArrayDocument.length: "+byteArrayDocument.length);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            DataResponse rsp= port.processData("PDFSigner_Wrap", null, byteArrayDocument);

            long begin = System.currentTimeMillis();

            byte[] doc_signe= rsp.getData();


            try {
                Files.write(Paths.get("D:\\DOC009_sign.pdf"), doc_signe);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();

            System.out.println("Signed in " + (end - begin) + " ms");

        } catch (
                InternalServerException_Exception | RequestFailedException_Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void setupTLS(Object port, String keyStore_TrustStoreFolder, String keyPassword, String username)
            throws FileNotFoundException, IOException, GeneralSecurityException {

        // String filename = chemin_keystore.trim()+"ejbca_auth_jks.jks";
        // String filenameTrust = chemin_keystore.trim()+"ejbca_truststore.jks";

        if (port == null) {
            throw new IllegalArgumentException("The port object cannot be null.");
        }

        if (!Proxy.class.isAssignableFrom(port.getClass())) {
            throw new IllegalArgumentException("The given port object is not a proxy instance.");
        }


        HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();

        TLSClientParameters tlsCP = new TLSClientParameters();

        KeyStore keyStore = KeyStore.getInstance("JKS");
        String keyStoreLoc = keyStore_TrustStoreFolder.trim() + "ejbca_auth_jks.jks";

        keyStore.load(new FileInputStream(keyStoreLoc), keyPassword.toCharArray());
        KeyManager[] myKeyManagers = getKeyManagers(keyStore, keyPassword);
        if (myKeyManagers == null) {
            throw new IllegalArgumentException("The key store cannot be null.");
        }
        tlsCP.setKeyManagers(myKeyManagers);

        KeyStore trustStore = KeyStore.getInstance("JKS");
        String trustStoreLoc = keyStore_TrustStoreFolder.trim() + "ejbca_truststore.jks";
        trustStore.load(new FileInputStream(trustStoreLoc), keyPassword.toCharArray());
        TrustManager[] myTrustStoreKeyManagers = getTrustManagers(trustStore);
        if (myTrustStoreKeyManagers == null) {
            throw new IllegalArgumentException("The trusted store cannot be null.");
        }

        tlsCP.setTrustManagers(myTrustStoreKeyManagers);

        // The following is not recommended and would not be done in a prodcution
        // environment,
        // this is just for illustrative purpose
        tlsCP.setDisableCNCheck(true);

        httpConduit.setTlsClientParameters(tlsCP);

        // Set client certificate information for authentication (if required)
        AuthorizationPolicy authorizationPolicy = httpConduit.getAuthorization();
        authorizationPolicy.setAuthorizationType(HttpAuthHeader.AUTH_TYPE_BASIC); // Set the appropriate authorization type
        authorizationPolicy.setUserName(username);
        authorizationPolicy.setPassword("passe");

    }
    DataResponse rsp=null;
    @PostMapping("signature4/{id_signer}")
    public ResponseEntity<?> Signature_base22(@RequestParam(value="workerId",required = false) Integer idWorker, @RequestParam("filereceivefile") MultipartFile file, @RequestParam("codePin") String codePin, @PathVariable Integer id_signer) throws IOException {

        Optional<Signataire_V2> signataireV2Optional = signataireRepository_V2.findByIdSigner(id_signer);
        if(idWorker == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID Application introuvable !");
        }
        int workerId = idWorker != null ? idWorker.intValue() : 0;
        if(!signataireV2Optional.isPresent()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Utilisateur inconnu !");
        }
        Signataire_V2 signer = signataireV2Optional.get();
        if (!encrypterPin(codePin).equals(signer.getCodePin()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Veuillez fournir un bon code PIN !");
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Veuillez selectionner un fichier !");
        }
        if(!workerRepository.existsByIdWorker(idWorker)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID Application introuvable !");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        //headers.add("X-Keyfactor-Requested-With","");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("filereceivefile", new ByteArrayResource(file.getBytes()){
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("workerId", workerId);
        body.add("codePin", codePin);
        //////////////////////////////////////////////////////
        final String serviceURL = prop.getProperty("wsdlUrl_client");
        String keyStoreLocation = prop.getProperty("keystore");
        String trustStoreLocation = prop.getProperty("trustore1");

        String password = prop.getProperty("password_keystore");

        System.setProperty("javax.net.ssl.keyStore", keyStoreLocation);
        System.setProperty("javax.net.ssl.password", password);
        System.setProperty("javax.net.ssl.trustStore", trustStoreLocation);
        System.setProperty("javax.net.ssl.trustStorePassword", password);


        //QName serviceName = new QName("http://www.confiancefactory.com/", "SignServerUser_Cert");
        URL wsdlURL=null;
        try {
            wsdlURL = new URL(serviceURL );
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ClientWSService service = new ClientWSService(wsdlURL);
        ClientWS port = service.getClientWSPort();
        System.out.println("#####PORT "+port);
        try {
            setupTLS3(port, password, signer.getNomSignataire());
        } catch (IOException | GeneralSecurityException e1) {
            // TODO Auto-generated catch block
            //log.error(e1.getMessage());
            e1.printStackTrace();
        }


        try {
            byte[] byteArrayDocument=null;
            try {
                //byteArrayDocument = Files.readAllBytes(Paths.get("D://ProJs//SigningServer//LS33600.pdf"));
                byteArrayDocument = Files.readAllBytes(Paths.get(file.getOriginalFilename()));

                System.out.println("byteArrayDocument.length: "+byteArrayDocument.length);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            byte[] fileBytes = file.getBytes();
            List<byte[]> bytesFile = new ArrayList<>();
            bytesFile.add(fileBytes);
            rsp= port.processData("PDFSigner_Wrap", null, fileBytes);
            OperationSignature operationSignature = new OperationSignature();
            operationSignature.setIdSigner(id_signer);
            operationSignature.setCodePin(signer.getCodePin());
            operationSignature.setSignerKey(signer.getSignerKey());
            Date dateOp = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            operationSignature.setDateOperation(sdf.format(dateOp));

            operationRepository.save(operationSignature);

        } catch (
                InternalServerException_Exception | RequestFailedException_Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ResponseEntity.ok(rsp) ;

    }
    private void setupTLS3(ClientWS port, String keyPassword, String username)
            throws FileNotFoundException, IOException, GeneralSecurityException {

        // String filename = chemin_keystore.trim()+"ejbca_auth_jks.jks";
        // String filenameTrust = chemin_keystore.trim()+"ejbca_truststore.jks";

        if (port == null) {
            throw new IllegalArgumentException("The port object cannot be null.");
        }

        if (!Proxy.class.isAssignableFrom(port.getClass())) {
            throw new IllegalArgumentException("The given port object is not a proxy instance.");
        }
        // Configuration du conduit HTTP pour utiliser TLS
        HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();

        //HTTPConduit httpConduit = (HTTPConduit) ((BindingProvider) port).getRequestContext().get(HTTPConduit.class);
        System.out.println("#####PORT "+httpConduit);
        TLSClientParameters tlsCP = new TLSClientParameters();

        KeyStore keyStore = KeyStore.getInstance("JKS");
        String keyStoreLoc = prop.getProperty("keystore");

        keyStore.load(Files.newInputStream(Paths.get(keyStoreLoc)), keyPassword.toCharArray());
        KeyManager[] myKeyManagers = getKeyManagers(keyStore, keyPassword);
        if (myKeyManagers == null) {
            throw new IllegalArgumentException("The key store cannot be null.");
        }
        tlsCP.setKeyManagers(myKeyManagers);

        KeyStore trustStore = KeyStore.getInstance("JKS");
        String trustStoreLoc = prop.getProperty("trustore1");
        trustStore.load(Files.newInputStream(Paths.get(trustStoreLoc)), keyPassword.toCharArray());
        TrustManager[] myTrustStoreKeyManagers = getTrustManagers(trustStore);
        if (myTrustStoreKeyManagers == null) {
            throw new IllegalArgumentException("The trusted store cannot be null.");
        }

        tlsCP.setTrustManagers(myTrustStoreKeyManagers);

        // The following is not recommended and would not be done in a prodcution
        // environment,
        // this is just for illustrative purpose
        tlsCP.setDisableCNCheck(true);

        httpConduit.setTlsClientParameters(tlsCP);

        // Set client certificate information for authentication (if required)
        AuthorizationPolicy authorizationPolicy = httpConduit.getAuthorization();
        authorizationPolicy.setAuthorizationType(HttpAuthHeader.AUTH_TYPE_BASIC); // Set the appropriate authorization type
        authorizationPolicy.setUserName(username);
        authorizationPolicy.setPassword("passe");

    }

    ////////////////////////////////////////////////
    /////////////////API DE RECHERHCE DE SIGNER PAR ID///////////////////////////////////////
    @GetMapping("findSignerById/{idSigner}")
    public Signataire_V2 trouverSignerParId(@PathVariable int idSigner){
        Optional<Signataire_V2> signer = signataireRepository_V2.findByIdSigner(idSigner);
        if (signer.isPresent()){
           return  signer.get();
        }
        return null;
    }

    @GetMapping("findSignataireById/{idSigner}")
    public Signataire trouverSignataireParId(@PathVariable int idSigner){
        Optional<Signataire> signer = signataireRepository.findById(idSigner);
        if (signer.isPresent()){
            return  signer.get();
        }
        return null;
    }

    @PostMapping("enregistrerInfosCertif")
    public ResponseEntity<String> enregistrerInfosCertif(@RequestBody  InfosCertificat infosCertificat){
        infosCertificatRepository.save(infosCertificat);
        return ResponseEntity.ok().build();
    }

    @GetMapping("getInfosEnrolement")
    public List<InfosCertificat> getInfosEnrolement(){
        return infosCertificatRepository.findAll();
    }


    @GetMapping("findSignerByCni/{cniSigner}")
    public List<Signataire_V2> trouverSignerParCNI(@PathVariable String cniSigner){
        System.out.println("CNI reçu : " + cniSigner);
        return signataireRepository_V2.findSignataireByCni(cniSigner);
    }

    @GetMapping("findNomWorkerById/{id_worker}")
    public Worker findNomWorkerById(@PathVariable int id_worker){
        Worker worker = workerRepository.findWorkersByIdWorker(id_worker);
        return worker;
    }


    @GetMapping("findSignerBynomSigner/{nomSigner}")
    public List<Signataire_V2> trouverSignerParNomSigner(@PathVariable String nomSigner){
        List<Signataire_V2> signerList = signataireRepository_V2.findSignataireByNomSignataire(nomSigner);
        return signerList;
    }

    @GetMapping("findSignerBynomWorkerBetweenDate/{date1}/{date2}/{workerName}")
    public List<Object[] > trouverSignerParNomWorkerBetweenDate(@PathVariable String date1,@PathVariable String date2,@PathVariable String workerName){
        date1 = date1+" 00:00:00";
        date2 = date2+" 00:00:00";
        List<Object[]> signerList = signataireRepository_V2.findSignatairesByDateRangeAndWorkerName(date1,date2,workerName);
        return signerList;
    }
    @GetMapping("findOperationBynomWorkerBetweenDate/{date1}/{date2}/{workerName}")
    public List<Object[] > trouverOperationParNomWorkerBetweenDate(@PathVariable String date1,@PathVariable String date2,@PathVariable String workerName){
        date1 = date1+" 00:00:00";
        date2 = date2+" 00:00:00";
        List<Object[]> signerList = operationRepository.findOperationByDateRangeAndWorkerName(date1,date2,workerName);
        return signerList;
    }

    @GetMapping("getAllOperations")
    public List<OperationSignature> getAllOperations(){
        List<OperationSignature> signerList = operationRepository.findAll();
        return signerList;
    }



    @GetMapping("findSignerByIdApp/{idApp}")
    public boolean trouverSignerParIdApp(@PathVariable Integer idApp){
        boolean isWorker = workerRepository.existsByIdWorker(idApp);
        return isWorker;
    }

    @PostMapping("ajoutOperation")
    public OperationSignature trouverSignerParId(@RequestBody OperationSignature operationSignature){
        return operationRepository.save(operationSignature);
    }

    @GetMapping("isExistedWorker/{idWorker}")
    public boolean verifieWorkerExist(@PathVariable int idWorker){
        return workerRepository.existsByIdWorker(idWorker);
    }

    public String decouper_nom(String nomAChanger) {
        //System.out.println("1er caractere : "+nomAChanger.charAt(0));
        if (nomAChanger.contains(" ")) {
            String[] caract = nomAChanger.split(" ");
            logger.info("Caracteres du tableau :"+caract.toString());
            if (caract.length < 1) {
                return "Tableau vide!";
            }
            if (caract[0].trim().isEmpty()) {
                return "Tableau vide!";
            }
            nomAChanger = caract[0] + "_";
            logger.info("caract de 0 :"+caract[0]);
            logger.info("Taille du tableau :"+caract.length);
            if (caract.length > 1) {
                for (int i = 1; i < caract.length; i++) {
                    logger.info("Dans la boucle FOR");
                    if (!caract[i].trim().isEmpty()) {
                        logger.info("caract de "+i+" :"+caract[i]);
                        nomAChanger += caract[i].charAt(0);
                    }
                }
            }

        }
        if (nomAChanger.length() > 70) {
            nomAChanger = nomAChanger.substring(0, 70);
        }
        logger.info("Nom caractere :"+nomAChanger);
        return nomAChanger;
    }

    public boolean isExistSignerKey(String alias) throws MalformedURLException {
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
            //e.printStackTrace();
            logger.error(e.getMessage());
        }
        return resultat;
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

    //Changer les attributs d'un Worker
    @PostMapping("setWorkerAttributes/{workerId}")
    public void setWorkerAttributes(@PathVariable int workerId, @RequestParam("image") String base64Image) throws MalformedURLException {
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
            List<String> nomWorker = new ArrayList<>();
            //port.setWorkerProperty(workerId,"ADD_VISIBLE_SIGNATURE","True");
            port.setWorkerProperty(workerId,"VISIBLE_SIGNATURE_CUSTOM_IMAGE_BASE64",base64Image);
           // port.setWorkerProperty(workerId,"VISIBLE_SIGNATURE_RECTANGLE","10,12,17,18");
           // port.setWorkerProperty(workerId,"VISIBLE_SIGNATURE_PAGE",numeroPage+"");

        } catch (AdminNotAuthorizedException_Exception e) {
            e.printStackTrace();
        }

    }

    //Changer les attributs d'un Worker
    @PostMapping("setWorkerAttributesCoord/{workerId}/{x1}/{y1}/{x2}/{y2}")
    public void setWorkerAttributesCoord(@PathVariable int workerId,@PathVariable String x1, @PathVariable String y1, @PathVariable String x2, @PathVariable String y2) throws MalformedURLException {
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
            List<String> nomWorker = new ArrayList<>();
            //port.setWorkerProperty(workerId,"ADD_VISIBLE_SIGNATURE","True");
            //port.setWorkerProperty(workerId,"VISIBLE_SIGNATURE_CUSTOM_IMAGE_BASE64",base64Image);
            port.setWorkerProperty(workerId,"VISIBLE_SIGNATURE_RECTANGLE",x1+","+y1+","+x2+","+y2);
            // port.setWorkerProperty(workerId,"VISIBLE_SIGNATURE_PAGE",numeroPage+"");

        } catch (AdminNotAuthorizedException_Exception e) {
            e.printStackTrace();
        }

    }

    @PostMapping("setWorkerAttributesPage/{workerId}/{numeroPage}")
    public void setWorkerAttributesPage(@PathVariable Integer workerId,@PathVariable String numeroPage) throws MalformedURLException {
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
            List<String> nomWorker = new ArrayList<>();
            //port.setWorkerProperty(workerId,"ADD_VISIBLE_SIGNATURE","True");
            // port.setWorkerProperty(workerId,"VISIBLE_SIGNATURE_RECTANGLE","10,12,17,18");
            port.setWorkerProperty(workerId,"VISIBLE_SIGNATURE_PAGE",numeroPage);

        } catch (AdminNotAuthorizedException_Exception e) {
            e.printStackTrace();
        }


    }

    @PostMapping("reloadWorker/{workerId}")
    public void reloadWorker(@PathVariable Integer workerId) throws MalformedURLException {
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

            port.reloadConfiguration(workerId);

        } catch (AdminNotAuthorizedException_Exception e) {
            e.printStackTrace();
        }


    }

    @GetMapping("findSignerStartup/{idWorker}")
    public ResponseEntity<SignerStartup> findSignerStartup(@PathVariable Integer idWorker){
        SignerStartup signerStartup = signerStartupRepository.findSignerStartupByIdWorker(idWorker);
        return ResponseEntity.ok(signerStartup);
    }

    @GetMapping("findSignDocument/{codeASCIIString}")
    public ResponseEntity<SignDocument> findSignerStartup(@PathVariable String codeASCIIString){
        SignDocument signDocument = signDocumentRepository.findByCodeASCIIString(codeASCIIString);
        return ResponseEntity.ok(signDocument);
    }

    @PostMapping("creerSignerStartup")
    public ResponseEntity<String> creerSignerStartup(@Valid @RequestBody SignerStartup signerStartup) {
        try {
            logger.info("Création de signerStartup avec ID Worker: {}", signerStartup.getIdWorker());

            Worker worker = workerRepository.findWorkersByIdWorker(signerStartup.getIdWorker());
            if(worker == null){
                logger.warn("ID worker non trouvé: {}", signerStartup.getIdWorker());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID worker non trouvé !");
            }

            SignerStartup signerStartupR = signerStartupRepository.findSignerStartupByIdWorker(signerStartup.getIdWorker());
            if (signerStartupR != null){
                logger.warn("Worker déjà enregistré avec ID: {}", signerStartup.getIdWorker());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Worker déjà enregistré !");
            }

            long codePin = pin_generation();
            SignerStartup signerStartup1 = new SignerStartup();
            signerStartup1.setIdWorker(signerStartup.getIdWorker());
            signerStartup1.setCodePin(encrypterPin(String.valueOf(codePin)));
            signerStartupRepository.save(signerStartup1);

            logger.info("Code Pin généré et enregistré pour ID Worker: {}", signerStartup.getIdWorker());
            return ResponseEntity.ok("Code Pin :" + codePin);

        } catch (NoSuchAlgorithmException e) {
            logger.error("Erreur de génération de Code Pin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la génération du Code Pin: "+e);
        } catch (Exception e) {
            logger.error("Erreur lors de la création de signerStartup", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la création: "+e);
        }
    }

    @GetMapping("checkUid")
    public boolean checkUidExists(@RequestParam String tableName, @RequestParam String uid) {
        return certService.existsByUid(tableName, uid);
    }

    public static X509Certificate convertStringToX509(String pemCert) throws Exception {

        // Décoder la chaîne Base64 en tableau de bytes
        byte[] certBytes = Base64.getDecoder().decode(pemCert);

        // Convertir en certificat X.509
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

}
