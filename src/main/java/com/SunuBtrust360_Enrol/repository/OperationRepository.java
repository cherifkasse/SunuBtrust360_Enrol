package com.SunuBtrust360_Enrol.repository;

import com.SunuBtrust360_Enrol.models.OperationSignature;
import com.SunuBtrust360_Enrol.models.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 05/03/2024/03/2024 - 11:58
 */
public interface OperationRepository extends JpaRepository<OperationSignature, Integer> {

    @Query("SELECT o.id, o.dateOperation, o.nomWorker FROM OperationSignature o WHERE o.dateOperation BETWEEN :date1 AND :date2 AND o.nomWorker = :workerName")
    List<Object[]> findOperationByDateRangeAndWorkerName(
            @Param("date1") String date1,
            @Param("date2") String date2,
            @Param("workerName") String workerName);
}
