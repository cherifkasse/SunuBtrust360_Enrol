package com.SunuBtrust360_Enrol.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CertService {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public boolean existsByUid(String tableName, String uid) {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE uniq_id_certificate = :uid";
        Number count = (Number) entityManager.createNativeQuery(query)
                .setParameter("uid", uid)
                .getSingleResult();
        return count.intValue() > 0;
    }
}
