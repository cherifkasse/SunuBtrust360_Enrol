package com.SunuBtrust360_Enrol.repository;

import com.SunuBtrust360_Enrol.models.Signataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 04/12/2023/12/2023 - 18:11
 */
public interface SignataireRepository extends JpaRepository<Signataire, Integer> {
    Optional<Signataire> findByEmail(String email);
    List<Signataire> findByNomSignataire(String nom);
    List<Signataire> findBySignerKey(String signerKey);
    @Query("SELECT s FROM Signataire s WHERE s.dateCreation LIKE %:dateCreation%")
    List<Signataire> findByDateCreationContaining(@Param("dateCreation") String dateCreation);

    List<Signataire> findByCleDeSignature(String cle);

    @Transactional
    void deleteBySignerKey(String signerKey);

    boolean existsByNomSignataire(String nom);
    boolean existsByEmail(String email);
    boolean existsByNomApplication(String app);
    boolean existsByNomEntreprise(String entr);

    // Méthode personnalisée pour la recherche


}
