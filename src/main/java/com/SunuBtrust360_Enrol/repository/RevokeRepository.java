package com.SunuBtrust360_Enrol.repository;

import com.SunuBtrust360_Enrol.models.Revoke;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RevokeRepository extends JpaRepository<Revoke, Integer> {


    List<Revoke> findByReasonTextContaining(String texte);
}
