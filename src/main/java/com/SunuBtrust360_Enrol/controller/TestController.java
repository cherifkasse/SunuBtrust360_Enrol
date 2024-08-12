package com.SunuBtrust360_Enrol.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 21/08/2023 - 16:28
 */
//@CrossOrigin(origins = {"http://localhost:8080","http://localhost:4200"})
@RestController
@RequestMapping("test/")
@Hidden
public class TestController {
    @RequestMapping("/")
    public String hello(){
        return "Goooooooooood";
    }
    @GetMapping
    public String AccessPourTous(){
        return "Acces pour tous";
    }

    @GetMapping("user")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER')")
    public String opAccess(){
        return "Acces operateur";
    }

    @GetMapping("admin")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER')")
    public String adminAccess(){
        return "Acces admin";
    }


}
