package com.SunuBtrust360_Enrol.repository;

import com.SunuBtrust360_Enrol.models.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 05/12/2023/12/2023 - 11:59
 */
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByNom(String nom);
    List<Application> findByIdApp(long idApp);

}
