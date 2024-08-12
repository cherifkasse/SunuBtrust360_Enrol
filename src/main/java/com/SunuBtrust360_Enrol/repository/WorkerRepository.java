package com.SunuBtrust360_Enrol.repository;

import com.SunuBtrust360_Enrol.models.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 08/01/2024/01/2024 - 10:52
 */
public interface WorkerRepository extends JpaRepository<Worker, Long> {

    List<Worker> findWorkersByNomWorker(String nomWorker);
    Worker findWorkersByIdWorker(int id_worker);

    boolean existsByIdWorker(int idWorker);
    @Query("SELECT w.nomWorker FROM Worker w")
    List<String> findAllByNomWorker();

}
