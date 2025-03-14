package com.SunuBtrust360_Enrol.security;

import com.SunuBtrust360_Enrol.security.jwt.AuthEntryPointJwt;
import com.SunuBtrust360_Enrol.security.jwt.AuthTokenFilter;
import com.SunuBtrust360_Enrol.security.services.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 22/08/2023 - 15:06
 */
@Configuration
@AllArgsConstructor
@EnableMethodSecurity
public class WebSecurityConfig {
    UserDetailsServiceImpl userDetailsService;

    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter(){
        return new AuthTokenFilter();
    }


    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((auth) ->
                        auth.requestMatchers(mvcMatcherBuilder.pattern("https://sunusign2ts2.btrust360.com/**")).permitAll()
                                .requestMatchers(mvcMatcherBuilder.pattern("/v0.0.2/sunubtrust360v0.0.2/signataire/**")).permitAll()
                                .requestMatchers(mvcMatcherBuilder.pattern("/v0.0.2/sunubtrust360v0.0.2/signataire/**")).permitAll()
                                .requestMatchers(mvcMatcherBuilder.pattern("/v0.0.2/signataire/enroll_V2")).permitAll()
                                .requestMatchers(mvcMatcherBuilder.pattern("/**")).permitAll()
                                .requestMatchers(mvcMatcherBuilder.pattern("/v0.0.2/**")).permitAll()
                                .requestMatchers(mvcMatcherBuilder.pattern("/v0.0.2/revoke/**")).permitAll()
                                .requestMatchers(mvcMatcherBuilder.pattern("/test/**")).permitAll()
                                .requestMatchers(mvcMatcherBuilder.pattern("/swagger-ui.html")).permitAll() // Autoriser l'accès à Swagger UI
                                .requestMatchers(mvcMatcherBuilder.pattern("/swagger-ui/**")).permitAll()
                                .requestMatchers(mvcMatcherBuilder.pattern("/v3/api-docs")).permitAll() // Autoriser l'accès à la documentation Swagger JSON
                                .requestMatchers(mvcMatcherBuilder.pattern("/webjars/**")).permitAll() // Autoriser l'accès aux ressources WebJars de Swagger
                                .anyRequest().authenticated()

                        );
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
