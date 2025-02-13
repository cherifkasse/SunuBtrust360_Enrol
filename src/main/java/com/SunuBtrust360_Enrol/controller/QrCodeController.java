package com.SunuBtrust360_Enrol.controller;

import com.SunuBtrust360_Enrol.models.QrCode;
import com.SunuBtrust360_Enrol.payload.request.QrCodeRequest;
import com.SunuBtrust360_Enrol.repository.QrCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v0.0.2/qrcode/")
public class QrCodeController {
    @Autowired
    private QrCodeRepository qrCodeRepository;

    @PostMapping("enregistrerQrCode")
    public ResponseEntity<?> enregistrerQrCode(@RequestBody QrCode qrCode) {
        QrCode qrCodeRequest1 = qrCodeRepository.save(qrCode);
        return ResponseEntity.ok(qrCodeRequest1);
    }

    @GetMapping("getQrCode/{idQrCode}")
    public ResponseEntity<?> enregistrerQrCode(@PathVariable Long idQrCode) {
        try {
            // Recherche du QR code dans la base de données
            QrCode qrCodeRequest1 = qrCodeRepository.findQrCodeById(idQrCode);

            // Si le QR code n'existe pas, retourner une réponse 404 avec un message
            if (qrCodeRequest1 == null) {
                return ResponseEntity.status(404).body("Erreur : ID de QR code introuvable.");
            }

            // Si le QR code existe, retourner une réponse 200 avec le QR code
            return ResponseEntity.ok(qrCodeRequest1);

        } catch (Exception e) {
            // En cas d'erreur interne (ex: problème de base de données), retourner une réponse 500
            return ResponseEntity.status(500).body("Erreur interne du serveur. Veuillez réessayer plus tard.");
        }
    }
    @GetMapping("getLastQrCode")
    public QrCode getLastQrCode() {
        return qrCodeRepository.findLast();
    }
}
