package com.temis.app.service.impl;

import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.temis.app.client.DocumentClassifierClient;
import com.temis.app.entity.DocumentEntity;
import com.temis.app.repository.DocumentRepository;
import com.temis.app.repository.DocumentTypeRepository;
import com.temis.app.service.DocumentClassificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class DocumentClassificationServiceImpl implements DocumentClassificationService {
    @Autowired
    private DocumentClassifierClient documentClassifierClient;
    @Autowired
    private DocumentTypeRepository documentTypeRepository;
    @Autowired
    private DocumentRepository documentRepository;

    @Override
    public DocumentEntity ClassifyDocument(DocumentEntity document) throws Exception {

        log.info("Classifying document '{}'", document.getPath());

        var types = documentTypeRepository.findAll();

        StringBuilder str = new StringBuilder("Categorías de documentos disponibles:\nID\tDESCRIPCIÓN");

        for (var type : types){
            str.append(type.getId());
            str.append('\t');
            str.append(type.getDescription());
            str.append('\n');
        }

        var response = documentClassifierClient.Classify(document.getPath(), document.getFileType(), str.toString());

        var result = ResponseHandler.getText(response).trim();

        long id = 0;

        try {
            id = Long.parseLong(result);
        } catch (NumberFormatException e) {
            log.error("Failed to parse result to DocumentTypeId: " + result, e);
        }

        var documentType = documentTypeRepository.findById(id);


        if(documentType.isPresent()){
            var dt = documentType.get();
            document.setDocumentType(dt);
            log.info("Classified document '{}' as '{}'", document.getPath(), dt.getName());
        }
        else {
            log.error("Warning failed to find DocumentType with ID: {}", id);
        }

        return documentRepository.save(document);
    }
}
