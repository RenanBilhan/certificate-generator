package com.renanbilhan.certificate_generator.controller;

import com.renanbilhan.certificate_generator.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@RequestMapping("certificate")
@RequiredArgsConstructor
@Log4j2
public class CertificateController {

    private final CertificateService certificarteService;

    @PostMapping(value= "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity uploadExcel(@RequestPart("file") FilePart file) throws IOException {
        Object obj = certificarteService.generateCertificate(file).doOnError(throwable -> new Exception("deu erro"));
        return ResponseEntity.ok().body(obj);
    }

//    @PostMapping()
//    public void uploadExcel1(@RequestParam("file") FilePart file){
//        System.out.println("Arquivo recebido com sucesso");
//    }
}
