package com.SunuBtrust360_Enrol.repository;



import com.SunuBtrust360_Enrol.models.Signataire;
import com.SunuBtrust360_Enrol.models.Signataire_V2;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 13/02/2024/02/2024 - 16:03
 */
public interface SignataireRepository_V2 extends JpaRepository<Signataire_V2, Integer> {
    @Query("SELECT s.id, s.dateCreation, s.nomWorker FROM Signataire_V2 s WHERE s.dateCreation BETWEEN :date1 AND :date2 AND s.nomWorker = :workerName")
    List<Object[]> findSignatairesByDateRangeAndWorkerName(
            @Param("date1") String date1,
            @Param("date2") String date2,
            @Param("workerName") String workerName);


    default Signataire_V2 findLast() {
        return findAll(Sort.by(Sort.Direction.DESC, "id")).stream().findFirst().orElse(null);
    }
    List<Signataire_V2> findSignataireByCni(String cni);
    List<Signataire_V2> findByNomSignataire(String nom);

    List<Signataire_V2> findSignataireByNomSignataire(String nom);

    List<Signataire_V2> findSignataire_V2ByIdApplication(Integer id);

    boolean existsById(int id);
    boolean existsByCni(String cni);
    boolean existsByNomSignataire(String nom);


    @Override
    long count();

    Optional<Signataire_V2> findByIdSigner(int idSigner);
}
