package com.SunuBtrust360_Enrol.repository;


import com.SunuBtrust360_Enrol.models.SignDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignDocumentRepository extends JpaRepository<SignDocument, Integer> {
    SignDocument findByCodeASCIIString(String codeASCIIString);
}
