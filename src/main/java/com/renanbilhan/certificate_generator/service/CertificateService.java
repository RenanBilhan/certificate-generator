package com.renanbilhan.certificate_generator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateService {

    private final ResourceLoader resourceLoader;
    public static final String CERTIFICATES = "classpath:templates/";
    public static final String JRXMLFILE = "certificate.jrxml";
    public static final String DOWNLOADDIRECTORY = "downloaded_certificates/";

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateService.class);

    public Flux<String> generateCertificate(FilePart file) {
        return file.content()
                .flatMap(dataBuffer -> {
                    try (Workbook workbook = new XSSFWorkbook(dataBuffer.asInputStream())) {
                        Sheet sheet = workbook.getSheetAt(0);
                        sheet.removeRow(sheet.getRow(0));
                        String pathAbsoluto = getAbsolutePath();

                        return Flux.fromIterable(sheet)
                                .flatMap(row -> {
                                    Map<String, Object> params = new HashMap<>();
                                    params.put("name", row.getCell(0).toString());
                                    params.put("workload", row.getCell(1).toString());
                                    params.put("course", row.getCell(2).toString());

                                    return Mono.fromCallable(() -> {
                                                String folderDirectory = createDirectory(row.getCell(0).toString());
                                                JasperReport report = JasperCompileManager.compileReport(getAbsolutePath());
                                                LOGGER.info("Report compilado para {}", row.getCell(0));
                                                JasperPrint print = JasperFillManager.fillReport(report, params, new JREmptyDataSource());
                                                LOGGER.info("Jasper print para {}", row.getCell(0));
                                                JasperExportManager.exportReportToPdfFile(print, folderDirectory);
                                                return "Arquivo exportado para " + row.getCell(0);
                                            })
                                            .onErrorResume(e -> {
                                                log.error("Erro ao gerar certificado para {}", row.getCell(0), e);
                                                return Mono.just("Erro ao gerar certificado para " + row.getCell(0));
                                            })
                                            .subscribeOn(Schedulers.parallel());
                                });
                    } catch (IOException e) {
                        return Flux.error(e);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Erro ao processar o arquivo", e);
                    return Flux.error(new RuntimeException("Erro ao processar o arquivo: " + e.getMessage()));
                })
                .doFinally(signalType -> {
                    log.info("Finalizando processamento do arquivo");
                });
    }

    private String createDirectory(String fileName) {
        File dir = new File(DOWNLOADDIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getPath() + File.separator + fileName.concat(".pdf");
    }

    private String getAbsolutePath() throws IOException {
        Resource resource = resourceLoader.getResource(CERTIFICATES + JRXMLFILE);
        InputStream inputStream = resource.getInputStream();

        File tempFile = File.createTempFile("certificate", ".jrxml");
        tempFile.deleteOnExit();

        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            IOUtils.copy(inputStream, outputStream);
        }

        return tempFile.getAbsolutePath();
    }
}