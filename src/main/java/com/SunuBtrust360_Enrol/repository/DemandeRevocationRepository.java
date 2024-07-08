package com.SunuBtrust360_Enrol.repository;

import com.SunuBtrust360_Enrol.models.DemandeRevocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface DemandeRevocationRepository extends JpaRepository<DemandeRevocation, Integer> {
    @Transactional
    void deleteBySignerKey(String signerKey);
}
