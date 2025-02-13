package com.SunuBtrust360_Enrol.repository;

import com.SunuBtrust360_Enrol.models.QrCode;
import com.SunuBtrust360_Enrol.models.Signataire_V2;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {

    QrCode findQrCodeById(Long id);

    default QrCode findLast() {
        return findAll(Sort.by(Sort.Direction.DESC, "id")).stream().findFirst().orElse(null);
    }
}
