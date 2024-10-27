package com.temis.app.service;

import org.springframework.web.multipart.MultipartFile;

public interface OCRService {
    String realizarOCR(MultipartFile file);
}
