package com.renanbilhan.certificate_generator.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.Rows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;

@Service
@Slf4j
public class CertificateService {

    public Flux<String> generateCertificate(FilePart file) throws IOException {
        System.out.println("abc");
        return file.content()
                .flatMap(dataBuffer -> {
                    try (Workbook workbook = new XSSFWorkbook(dataBuffer.asInputStream())) {
                        Sheet sheet = workbook.getSheetAt(0);
                        sheet.removeRow(sheet.getRow(0));

                        for (Row row : sheet) {
                            for (Cell cell : row) {
                                log.info(cell.toString() + "\t");
                            }
                        }
                        return Mono.just("Arquivo processado!");
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Erro ao processar o arquivo", e);
                    return Mono.just("Erro ao processar o arquivo: " + e.getMessage());
                })
                .doFinally(signalType -> {
                    log.info("Finalizando processamento do arquivo");
                });
    }
}
