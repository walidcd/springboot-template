package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.mustangproject.validator.ZUGFeRDValidator;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import java.io.*;

@RestController
@RequestMapping("/facturx")
public class ValidationController {

    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> validateFacturX(@RequestParam("pdf") MultipartFile pdfFile) throws Exception {
        File tempPdf = File.createTempFile("uploaded", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(tempPdf)) {
            fos.write(pdfFile.getBytes());
        }

        ZUGFeRDValidator validator = new ZUGFeRDValidator();
        String xmlResult = validator.validate(tempPdf.getAbsolutePath());

        // Parse XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(xmlResult.getBytes());
        Document doc = builder.parse(is);

        // Extract summary status
        Node summaryNode = doc.getElementsByTagName("summary").item(0);
        String status = summaryNode.getAttributes().getNamedItem("status").getNodeValue();

        // Extract error messages
        NodeList messages = doc.getElementsByTagName("error");
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < messages.getLength(); i++) {
            msg.append("- ").append(messages.item(i).getTextContent()).append("\n");
        }

        String readable =
                "**Statut :** " + status.toUpperCase() + "\n" +
                        "**Erreurs :**\n" + (!msg.isEmpty() ? msg.toString() : "Aucune erreur") +
                        "\n";

        tempPdf.delete();
        return ResponseEntity.ok(readable);
    }
}
