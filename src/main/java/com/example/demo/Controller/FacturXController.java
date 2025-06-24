package com.example.demo.Controller;

import org.apache.fop.apps.*;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

@RestController
@RequestMapping("/facturx")
public class FacturXController {

    // Generate Factur-X PDF from uploaded XML (using FOP/XSLT template)
    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> generateFromXml(@RequestParam("xml") MultipartFile xmlFile) throws Exception {
        byte[] pdfBytes = generatePdf(xmlFile);
        byte[] facturxPdf = embedXmlIntoPdf(pdfBytes, xmlFile.getBytes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_facturx.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(facturxPdf);
    }

    // Attach XML to an existing PDF (if you want to attach to a custom PDF)
    @PostMapping(value = "/attach", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> attachXmlToPdf(
            @RequestParam("pdf") MultipartFile pdfFile,
            @RequestParam("xml") MultipartFile xmlFile
    ) throws Exception {
        File tempPdf = File.createTempFile("input", ".pdf");
        pdfFile.transferTo(tempPdf);

        ZUGFeRDExporterFromA3 exporter = new ZUGFeRDExporterFromA3()
                .ignorePDFAErrors()
                .load(tempPdf.getAbsolutePath());
        exporter.setXML(xmlFile.getBytes());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.export(outputStream);

        tempPdf.delete();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=facturx.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(outputStream.toByteArray());
    }

    // --- PDF Generation using FOP + XSLT ---
    private byte[] generatePdf(MultipartFile xmlFile) throws Exception {
        // Load your XSLT template from resources (src/main/resources/invoice-facturx.xslt)
        Source xslt = new StreamSource(getClass().getResourceAsStream("/invoice-facturx.xslt"));
        Source xmlSource = new StreamSource(xmlFile.getInputStream());
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();

        // FOP setup
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfOut);

        // Transformer
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(xslt);

        // XML + XSLT => PDF
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(xmlSource, res);

        return pdfOut.toByteArray();
    }

    // --- Embed XML into PDF (Factur-X/ZUGFeRD Mustang) ---
    private byte[] embedXmlIntoPdf(byte[] pdfBytes, byte[] xmlBytes) throws Exception {
        File tempPdf = File.createTempFile("invoice", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(tempPdf)) {
            fos.write(pdfBytes);
        }

        ZUGFeRDExporterFromA3 exporter = new ZUGFeRDExporterFromA3()
                .ignorePDFAErrors()
                .load(tempPdf.getAbsolutePath())
                .setXML(xmlBytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.export(outputStream);
        tempPdf.delete();

        return outputStream.toByteArray();
    }
}
