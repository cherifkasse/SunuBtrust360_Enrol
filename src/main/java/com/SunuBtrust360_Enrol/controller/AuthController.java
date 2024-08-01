package com.SunuBtrust360_Enrol.controller;

import com.SunuBtrust360_Enrol.models.ERole;
import com.SunuBtrust360_Enrol.models.GestLogs;
import com.SunuBtrust360_Enrol.models.Role;
import com.SunuBtrust360_Enrol.models.User;
import com.SunuBtrust360_Enrol.payload.request.LoginRequest;
import com.SunuBtrust360_Enrol.payload.request.SignupRequest;
import com.SunuBtrust360_Enrol.payload.response.JwtResponse;
import com.SunuBtrust360_Enrol.payload.response.MessageResponse;
import com.SunuBtrust360_Enrol.repository.GestLogsRepository;
import com.SunuBtrust360_Enrol.repository.RoleRepository;
import com.SunuBtrust360_Enrol.repository.UserRepository;
import com.SunuBtrust360_Enrol.security.jwt.JwtUtils;
import com.SunuBtrust360_Enrol.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 21/08/2023 - 13:08
 */
@RestController

//@CrossOrigin(origins = {"http://localhost:8080","http://localhost:4200"})
@RequestMapping("/v0.0.2/")
@Hidden
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;
    @Autowired
    public JdbcTemplate jdbcTemplate;

    @Autowired
    private final GestLogsRepository gestLogsRepository;


    @Autowired
    JwtUtils jwtUtils;
    @RequestMapping("/")
    public String hello(){
        return "Goooooooooooddd";
    }

    public AuthController(GestLogsRepository gestLogsRepository,UserRepository userRepository) {
        this.gestLogsRepository = gestLogsRepository;
        this.userRepository = userRepository;
    }

    /**
     * Methode pour la gestion des logs
     * **/
    public void gestLogs(HttpServletRequest httpServletRequest, String action, String message){
        GestLogs gestLogs = new GestLogs();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date_logs = new Date();
        String role = "-";
        if(httpServletRequest.getAttribute("role").toString().contains("ADMIN")){
            role = "ADMINISTRATEUR";
        }
        if(httpServletRequest.getAttribute("role").toString().toUpperCase().contains("USER")){
            role = "OPERATEUR(TRICE)";
        }

        gestLogs.setAuteur(httpServletRequest.getAttribute("email").toString());
        gestLogs.setDate(sdf.format(date_logs));
        gestLogs.setEmail(httpServletRequest.getAttribute("username").toString());
        gestLogs.setRole(role);
        gestLogs.setAction(action);
        gestLogs.setMessage(message);
        gestLogsRepository.save(gestLogs);
    }
    @PostMapping("signing")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest) {
        User user = userRepository.findUserByEmail(loginRequest.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());


            // Ajouter les informations de l'utilisateur dans la requête pour les logs
            httpServletRequest.setAttribute("username", loginRequest.getEmail());
            httpServletRequest.setAttribute("email", user.getUsername());
            httpServletRequest.setAttribute("role", roles.stream().findFirst().orElse("USER"));

            // Enregistrez l'action de l'utilisateur
            String action = "Action Connexion";
            String message = "Connexion réussie";
            gestLogs(httpServletRequest, action, message);

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    getNomByEmail(loginRequest.getEmail()),
                    loginRequest.getEmail(),
                    roles
            ));
        } catch (Exception e) {
            // Enregistrez l'échec de l'authentification
            httpServletRequest.setAttribute("username", loginRequest.getEmail());
            httpServletRequest.setAttribute("email", user.getUsername());  // Utiliser l'email comme username par défaut
            httpServletRequest.setAttribute("role", "-");

            String action = "Action Connexion";
            String message = "Echec: Connexion échouée " + e.getMessage();
            gestLogs(httpServletRequest, action, message);

            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("signup")
   //S @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest){
        if(userRepository.existsByEmail(signupRequest.getEmail())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Erreur : Un utilisateur avec cet email existe dejà !"));
        }
        if(!isValidPassword(signupRequest.getPassword())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("""
                            Le mot de passe ne respecte pas les critères :
                            Au moins 8 caractèresDoit contenir au moins une lettre majuscule
                            Doit contenir au moins une lettre minuscule
                            Doit contenir au moins un chiffre
                            Peut contenir des caractères spéciaux
                            Ne doit pas contenir d'espaces"""));
        }
        if(!validateEmail(signupRequest.getEmail())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Veuillez saisir un email valide!"));
        }
        User user = new User(signupRequest.getUsername(),signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword())
        );

        Set<String> strRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();
        if (strRoles == null){
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Erreur : Role non trouvé !"));
            roles.add(userRole);
        }else{
            strRoles.forEach(role -> {
                switch(role){
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Erreur : Role non trouvé !"));
                        roles.add(adminRole);
                        break;
                    case "super":
                        Role superRole = roleRepository.findByName(ERole.ROLE_SUPER)
                                .orElseThrow(() -> new RuntimeException("Erreur : Role non trouvé !"));
                        roles.add(superRole);
                        break;

                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Erreur : Role non trouvé !"));
                        roles.add(userRole);

                }

            });
        }
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Utilisateur enrégistré avec succès ! "));
    }

    public static boolean isValidPassword(String password) {
        // Au moins 8 caractères
        // Doit contenir au moins une lettre majuscule
        // Doit contenir au moins une lettre minuscule
        // Doit contenir au moins un chiffre
        // Peut contenir des caractères spéciaux
        // Ne doit pas contenir d'espaces
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();
    }
    public String getNomByEmail(String email) {
        String sql = "SELECT username FROM users WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{email}, String.class);
    }

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public boolean validateEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}



