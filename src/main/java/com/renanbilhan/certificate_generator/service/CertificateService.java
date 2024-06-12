package com.renanbilhan.certificate_generator.service;

import com.renanbilhan.certificate_generator.model.Certificate;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.apache.poi.ss.formula.functions.Rows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CertificateService {
    public static final String CERTIFICATES =  "classpath:template/certificate/";

    public static final String JRXMLFILE = "certificate.jrxml";

    public static final Logger LOGGER = LoggerFactory.getLogger(CertificateService.class);

    public static final String DOWNLOADDIRECTORY = "C:\\estudo\\jasper-report\\";


    public Flux<String> generateCertificate(FilePart file) throws IOException {

        System.out.println("abc");
        return file.content()
                .flatMap(dataBuffer -> {
                    try (Workbook workbook = new XSSFWorkbook(dataBuffer.asInputStream())) {
                        Sheet sheet = workbook.getSheetAt(0);
                        sheet.removeRow(sheet.getRow(0));

                        for (Row row : sheet) {
                            Map<String, Object> params = new HashMap<>();
                            params.put("name", row.getCell(0).toString());
                            params.put("workload", row.getCell(1).toString());
                            params.put("course", row.getCell(2).toString());

                            String pathAbsoluto = getAbsolutePath();
                            try{
                                String folderDirectory = createDirectory(row.getCell(0).toString());
                                JasperReport report = JasperCompileManager.compileReport(pathAbsoluto);
                                LOGGER.info("Report compilado");
                                JasperPrint print = JasperFillManager.fillReport(report, params, new JREmptyDataSource());
                                LOGGER.info("Jasper print");
                                JasperExportManager.exportReportToPdfFile(print, folderDirectory);
                            } catch (JRException e) {
                                throw new RuntimeException(e);
                            }
                            Certificate certificate = new Certificate();
                            certificate.setName(row.getCell(0).toString());
                            certificate.setWorkload(row.getCell(1).toString());
                            certificate.setCourse(row.getCell(2).toString());

                            System.out.println(certificate.getName());
                            System.out.println(certificate.getWorkload().toString());
                            System.out.println(certificate.getCourse());
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

    private String createDirectory(String fileName) {
        File dir = new File(DOWNLOADDIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath() + File.separator + fileName.concat(".pdf");
    }

    private String getAbsolutePath() throws FileNotFoundException {
        return ResourceUtils.getFile(CERTIFICATES + JRXMLFILE).getAbsolutePath();
    }
}
