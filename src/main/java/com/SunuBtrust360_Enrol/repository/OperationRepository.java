package com.SunuBtrust360_Enrol.repository;

import com.SunuBtrust360_Enrol.models.OperationSignature;
import com.SunuBtrust360_Enrol.models.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 05/03/2024/03/2024 - 11:58
 */
public interface OperationRepository extends JpaRepository<OperationSignature, Integer> {
}
