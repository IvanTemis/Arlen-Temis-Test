package com.temis.app.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.temis.app.client.VertexAIClient;
import com.temis.app.model.DocumentSummarizeDTO;
import com.temis.app.service.SummarizeService;

@Service
public class SummarizeServiceImpl implements SummarizeService{

    private static final Logger logger = Logger.getLogger(SummarizeServiceImpl.class.getName());

    private final VertexAIClient vertexAIClient;

    @Autowired
        public SummarizeServiceImpl(VertexAIClient vertexAIClient) {
            this.vertexAIClient = vertexAIClient;
        }

    @Override
    public DocumentSummarizeDTO getSummarizeFromDocument() throws IOException {

        String summary = vertexAIClient.summarize("Summarizer: destaca lo esencial de los textos con inteligencia artificial. En general, Summarizer es otra IA que realiza resúmenes automáticos de textos largos. Permite definir la longitud para poder condensar la información clave de diferentes fuentes. Exhibe el resumen en formato texto y también en viñetas.");

        // Crear el DTO con el resumen generado
        DocumentSummarizeDTO summarizeDTO = new DocumentSummarizeDTO();
        summarizeDTO.setSummarize(summary);

        return summarizeDTO;
    }

}