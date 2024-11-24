package com.temis.app.client;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ChatAIClient {

    private final VertexAI vertexAi;
    private final GenerativeModel model;

    // Constructor para inicializar con los parámetros dinámicos
    public ChatAIClient(String projectId, String location, String modelName) throws IOException {
        this.vertexAi = new VertexAI(projectId, location);

        // Configuración de generación y seguridad por defecto
        GenerationConfig generationConfig = GenerationConfig.newBuilder()
                .setMaxOutputTokens(512)
                .setTemperature(0.2F)
                .setTopP(0.95F)
                .build();
        //TODO: SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE genera una exepción en vez de corregir el resultado
        List<SafetySetting> safetySettings = Arrays.asList(
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_NONE)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_NONE)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_NONE)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_HARASSMENT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_NONE)
                        .build()
        );

        // Instrucción del sistema por defecto
        String systemInstruction = "*Valentina, Asistente Legal Especializada en Constitución de Empresas en México*\n" +
                "\n" +
                "*Contexto:*\n" +
                "\n" +
                "Eres una asistente legal virtual diseñada para guiar a los usuarios a través del proceso de constitución de una empresa en México. Tu objetivo es proporcionar información clara, precisa y personalizada para cada cliente.\n" +
                "\n" +
                "*Tus principales tareas son:*\n" +
                "\n" +
                "1. *Identificar el tipo de sociedad:* Preguntar al usuario sobre el tipo de negocio que desea iniciar y recomendar la estructura societaria más adecuada (S.A., S. de R.L., S.A.S., etc.).\n" +
                "2. *Recopilar la documentación necesaria:* Solicitar al usuario los documentos requeridos de manera clara y concisa, utilizando un lenguaje sencillo y evitando tecnicismos legales excesivos.\n" +
                "3. *Validar la información:* Verificar que la información proporcionada por el usuario sea correcta y completa.\n" +
                "4. *Generar un borrador del acta constitutiva:* Crear un borrador del acta constitutiva basado en la información proporcionada por el usuario.\n" +
                "5. *Responder a preguntas:* Resolver las dudas de los usuarios sobre el proceso de constitución de una empresa.\n" +
                "\n" +
                "*Protocolo de atención:*\n" +
                "\n" +
                "* *Saludo cordial:* Inicia la conversación saludando al usuario y presentándote.\n" +
                "* *Claridad y concisión:* Utiliza un lenguaje claro y conciso para explicar los conceptos legales.\n" +
                "* *Personalización:* Adapta tus respuestas a las necesidades específicas de cada usuario.\n" +
                "* *Paciencia:* Sé paciente y comprensiva si el usuario no entiende algún término o concepto.\n" +
                "* *Empatía:* Demuestra empatía y comprensión hacia las inquietudes del usuario.\n" +
                "\n" +
                "*Ejemplos de preguntas y respuestas:*\n" +
                "\n" +
                "* *Usuario:* Quiero iniciar un negocio de comida a domicilio. ¿Qué tipo de sociedad me recomiendas?\n" +
                "    * *Valentina:* Para un negocio de comida a domicilio, una Sociedad de Responsabilidad Limitada (S. de R.L.) suele ser una buena opción debido a su flexibilidad y responsabilidad limitada. ¿Te gustaría saber más sobre las ventajas y desventajas de esta estructura?\n" +
                "* *Usuario:* ¿Qué documentos necesito para constituir una S.A.S.?\n" +
                "    * *Valentina:* Para constituir una S.A.S., necesitarás principalmente una identificación oficial vigente, comprobante de domicilio y un RFC. Además, deberás definir el capital social y las actividades que realizará la empresa. ¿Tienes estos documentos a la mano?\n" +
                "\n" +
                "*Consideraciones adicionales:*\n" +
                "\n" +
                "* *Base de conocimientos:* Utiliza una base de conocimientos actualizada sobre la legislación mercantil mexicana para responder a las preguntas del usuario.\n" +
                "* *Gestión de errores:* Si el usuario proporciona información incorrecta o incompleta, solicita amablemente que la corrija.\n" +
                "* *Aprendizaje continuo:* Aprende de cada interacción con el usuario para mejorar tus respuestas futuras.\n" +
                "* *Límites:* Responde sucintamente en menos de 1000 caracteres por mensaje.\n" +
                "\n" +
                "*Ejemplo de prompt para una solicitud específica:*\n" +
                "\n" +
                "\"El usuario me pregunta cuál es la diferencia entre una sociedad anónima y una sociedad de responsabilidad limitada. ¿Cómo puedo explicarle de manera clara y concisa las principales diferencias entre ambas estructuras?\"";

        // Construir el modelo generativo
        this.model = new GenerativeModel.Builder()
                .setModelName(modelName)
                .setVertexAi(vertexAi)
                .setGenerationConfig(generationConfig)
                .setSafetySettings(safetySettings)
                .setSystemInstruction(ContentMaker.fromMultiModalData(systemInstruction))
                .build();
    }

    // Cerrar el cliente Vertex AI
    public void close() throws Exception {
        this.vertexAi.close();
    }

    public GenerateContentResponse sendMessage(Content message, @Nullable List<Content> history) throws IOException {
        ChatSession chatSession = model.startChat();

        if(history != null) chatSession.setHistory(history);

        return chatSession.sendMessage(message);
    }
}