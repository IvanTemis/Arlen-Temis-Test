package com.temis.app.service;

import com.temis.app.entity.DocumentEntity;

import java.io.IOException;

public interface DocumentClassificationService {

    DocumentEntity ClassifyDocument(DocumentEntity document) throws Exception;

}