package com.temis.app.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictRequest;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.protobuf.Value;
import com.temis.app.model.DocumentSummarizeDTO;
import com.temis.app.service.SummarizeService;

@Service
public class SummarizeServiceImpl implements SummarizeService{

    private static final String PROJECT_ID = "temis-430707";
    private static final String LOCATION = "us"; 
    private static final String ENDPOINT_ID = "5407432cc270c757";

    @Override
    public DocumentSummarizeDTO getSummarizeFromDocument() throws IOException {
        
        String documentText = "Summarizer: destaca lo esencial de los textos con inteligencia artificial. En general, Summarizer es otra IA que realiza resúmenes automáticos de textos largos. Permite definir la longitud para poder condensar la información clave de diferentes fuentes. Exhibe el resumen en formato texto y también en viñetas.";

        try (PredictionServiceClient predictionServiceClient = PredictionServiceClient.create()) {
            
            
            EndpointName endpointName = EndpointName.of(PROJECT_ID, LOCATION, ENDPOINT_ID);
            
            PredictRequest predictRequest = PredictRequest.newBuilder()
                .setEndpoint(endpointName.toString())
                .addAllInstances(getPredictInstances(documentText))
                .build();
            
            PredictResponse predictResponse = predictionServiceClient.predict(predictRequest);

            String summary = extractSummaryFromResponse(predictResponse);

            DocumentSummarizeDTO summarizeDTO = new DocumentSummarizeDTO();
            summarizeDTO.setId(Long.valueOf(1));
            summarizeDTO.setSummarize(summary);


            return summarizeDTO;
        }
    }

    private Iterable<Value> getPredictInstances(String documentText) {
        List<Value> instances = new ArrayList<>();
        
        // Dependiendo de cómo hayas configurado tu modelo, deberías ajustar esto. Aquí se está tratando como un String.
        instances.add(Value.newBuilder().setStringValue(documentText).build());
        
        return instances;
    }

    // Método para extraer el resumen de la respuesta de Vertex AI
    private String extractSummaryFromResponse(PredictResponse response) {
        // Aquí debes adaptar este código para procesar el resultado según la salida de tu modelo
        return response.getPredictions(0).getStructValue().getFieldsOrThrow("summary").getStringValue();
    }
    
}
