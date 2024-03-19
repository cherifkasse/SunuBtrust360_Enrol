package com.SunuBtrust360_Enrol.repository;


import com.SunuBtrust360_Enrol.models.Signataire_V2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 13/02/2024/02/2024 - 16:03
 */
public interface SignataireRepository_V2 extends JpaRepository<Signataire_V2, Integer> {

    List<Signataire_V2> findSignataireByCni(String cni);

    boolean existsById(int id);
    boolean findById(int id);

    @Override
    long count();
}
