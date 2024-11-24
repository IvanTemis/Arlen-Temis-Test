package com.temis.app.service;

import java.io.IOException;

import com.temis.app.model.DocumentSummarizeDTO;

public interface SummarizeService {
    
    DocumentSummarizeDTO getSummarizeFromDocument () throws IOException;

}