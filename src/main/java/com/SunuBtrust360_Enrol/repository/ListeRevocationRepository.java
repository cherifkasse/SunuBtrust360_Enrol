package com.SunuBtrust360_Enrol.repository;

import com.SunuBtrust360_Enrol.models.ListeRevocation;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListeRevocationRepository extends JpaRepository<ListeRevocation, Integer> {
    List<ListeRevocation> findBySignerKey(String signerKey);
}
