package com.SunuBtrust360_Enrol.repository;

import com.SunuBtrust360_Enrol.models.IdLastSignataire;
import com.SunuBtrust360_Enrol.models.Signataire_V2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdLastSigantaireRepository extends JpaRepository <IdLastSignataire, Integer>{

    IdLastSignataire findById(int id);


}
