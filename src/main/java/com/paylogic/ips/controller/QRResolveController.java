package com.paylogic.ips.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gms.utils.exception.BusinessException;
import com.paylogic.ips.bo.QrResolveRequest;
import com.paylogic.ips.bo.QrResolveResponse;
import com.paylogic.ips.services.QrService;

@RestController
@RequestMapping("/api/qr")
public class QRResolveController {
/*
    @Autowired private QrService  qrService;

    @PostMapping("/resolve")
    public ResponseEntity<QrResolveResponse> resolveQr(@RequestBody QrResolveRequest request) throws BusinessException {

        // 1. Extraire l’UUID du QR scanné
        String uuid = qrService.extractUuid(request.getQrData());

        // 2. Appeler le système central QRR-M
        QrResolveResponse response = qrService.resolveQr(uuid);

        // 3. Retourner les infos au mobile
        return ResponseEntity.ok(response);
    }*/
}
