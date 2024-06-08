package com.renanbilhan.certificate_generator.controller;

import com.renanbilhan.certificate_generator.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("certificate")
@RequiredArgsConstructor
@Log4j2
public class CertificateController {

    private final CertificateService certificarteService;

    @PostMapping("/upload")
    public ResponseEntity uploadExcel(@RequestParam("file") MultipartFile file) throws IOException {
        certificarteService.generateCertificate(file);
        return ResponseEntity.ok().body("Processando arquivio");
    }

    @PostMapping()
    public void uploadExcel1(@RequestParam("file") MultipartFile file){
        System.out.println("Arquivo recebido com sucesso");
    }
}
