package com.example.demo;

import org.mustangproject.validator.ZUGFeRDValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Controller
@RequestMapping("/facturx")
public class ValidationController {

    @GetMapping("/validate")
    public String showValidationPage() {
        return "validate";
    }

    @PostMapping("/validate")
    public String validateFacturX(@RequestParam("pdf") MultipartFile pdfFile, Model model) {
        File tempPdf = null;
        try {
            // Save the uploaded PDF to a temporary file
            tempPdf = File.createTempFile("uploaded", ".pdf");
            try (FileOutputStream fos = new FileOutputStream(tempPdf)) {
                fos.write(pdfFile.getBytes());
            }

            // Initialize the Mustangproject ZUGFeRDValidator
            ZUGFeRDValidator validator = new ZUGFeRDValidator();
            String validationResult = validator.validate(tempPdf.getAbsolutePath());

            model.addAttribute("validationResult", validationResult);
        } catch (Exception e) {
            model.addAttribute("error", "Validation failed: " + e.getMessage());
        } finally {
            if (tempPdf != null && tempPdf.exists()) {
                tempPdf.delete();
            }
        }
        return "validate";
    }
}
