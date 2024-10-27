package com.temis.app.service.impl;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.temis.app.service.OCRService;

import java.io.File;
import java.io.IOException;

@Service
public class OCRServiceImpl implements OCRService {

    private final Tesseract tesseract;

    public OCRServiceImpl() {
        tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/tessdata");
        tesseract.setLanguage("spa");
    }

    @Override
    public String realizarOCR(MultipartFile file) {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        try {
            file.transferTo(convFile);
            return tesseract.doOCR(convFile);
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            return "Error en OCR";
        } finally {
            if (convFile.exists()) {
                convFile.delete();
            }
        }
    }
}