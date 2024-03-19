package com.SunuBtrust360_Enrol.controller;


import com.SunuBtrust360_Enrol.config.CustomHttpRequestFactory;
import com.SunuBtrust360_Enrol.models.OperationSignature;
import com.SunuBtrust360_Enrol.models.PieceIdentite;
import com.SunuBtrust360_Enrol.models.Signataire_V2;
import com.SunuBtrust360_Enrol.repository.OperationRepository;
import com.SunuBtrust360_Enrol.repository.PieceIdentiteRepository;
import com.SunuBtrust360_Enrol.repository.SignataireRepository_V2;
import com.SunuBtrust360_Enrol.payload.request.ObtenirCertRequest_V2;
import com.SunuBtrust360_Enrol.payload.request.SignataireRequest_V2;
import com.SunuBtrust360_Enrol.repository.WorkerRepository;
import com.SunuBtrust360_Enrol.wsdl.*;
import com.SunuBtrust360_Enrol.wsdl_client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.client.RestTemplate;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.validation.Valid;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.KeyStoreException;
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
@RequestMapping("/signer/")
@CrossOrigin(origins = "http://localhost:8080")
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
    private PieceIdentiteRepository pieceIdentiteRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private OperationRepository operationRepository;

    public SignerController(SignataireRepository_V2 signataireRepository,WorkerRepository workerRepository,OperationRepository operationRepository) {
        this.signataireRepository_V2 = signataireRepository;
        this.workerRepository = workerRepository;
        this.operationRepository = operationRepository;
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
    public ResponseEntity<?> enrollSignataire_V2(@Valid @RequestBody SignataireRequest_V2 signataireRequest, BindingResult bindingResult) throws Exception {
        RestTemplate restTemplate = new RestTemplate(customFact.getClientHttpRequestFactory());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.add("X-Keyfactor-Requested-With","");
        if (bindingResult.hasErrors()) {

            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
        //GENERER CLE DE SIGNATURE SUR SIGNSERVER
        String alias = prop.getProperty("aliasCle") + signataireRequest.getNomSignataire().trim().toUpperCase().replaceAll("\\s+", "_");
        ObjectMapper objectMapper = new ObjectMapper();

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
    }
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
            return replacedString.toString();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                 | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return "Error during encryption.";
        }
    }
    @PostMapping("decrypt/{pinEncrypted}")
    public String decryptPin(@PathVariable String pinEncrypted) {
        try {
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
                return new String(decryptedBytes, StandardCharsets.UTF_8);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                 | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return "Error during decryption.";
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////
    ///Générer code pin
    //////////////////////////////////////////////////////////////////////////////////////
    @PostMapping("generer_pin")
    public long pin_generation() throws NoSuchAlgorithmException {
        final int taille_pin = 6;
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        final StringBuilder pin = new StringBuilder(taille_pin);
        for (int i = 0; i < taille_pin; i++) {
            pin.append(secureRandom.nextInt(9));
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
        if (!signataireRepository_V2.existsById(idSignataire)) {
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

        Optional<Signataire_V2> signataireV2Optional = signataireRepository_V2.findById(id_signer);
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
        Optional<Signataire_V2> signataireV2Optional = signataireRepository_V2.findById(id_signer);
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
}
