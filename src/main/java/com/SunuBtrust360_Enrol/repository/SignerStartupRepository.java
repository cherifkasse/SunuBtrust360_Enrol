package com.SunuBtrust360_Enrol.repository;

import com.SunuBtrust360_Enrol.models.SignerStartup;
import com.SunuBtrust360_Enrol.models.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 10/09/2024/09/2024 - 18:39
 */
public interface SignerStartupRepository extends JpaRepository<SignerStartup, Long> {

    SignerStartup findSignerStartupByIdWorker(int idWorker);
}
