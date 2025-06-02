package com.example.demo;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/facturx")
public class FacturXController {

    @PostMapping(value = "/attach", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> attachXmlToPdf(
            @RequestParam("pdf") MultipartFile pdfFile,
            @RequestParam("xml") MultipartFile xmlFile
    ) throws IOException {
        // Save the uploaded PDF to a temporary file
        File tempPdf = File.createTempFile("input", ".pdf");
        pdfFile.transferTo(tempPdf);

        // Initialize the Mustang exporter
        ZUGFeRDExporterFromA3 exporter = new ZUGFeRDExporterFromA3()
                .ignorePDFAErrors()
                .load(tempPdf.getAbsolutePath());
        exporter.setXML(xmlFile.getBytes());

        // Export the Factur-X PDF to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.export(outputStream);

        // Clean up the temporary file
        Files.deleteIfExists(tempPdf.toPath());

        // Return the resulting PDF
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=facturx.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(outputStream.toByteArray());
    }
}
