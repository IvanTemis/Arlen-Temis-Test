package com.temis.app.utils;

import org.springframework.beans.factory.annotation.Autowired;

import com.temis.app.config.properties.CloudConfigProperties;

public class TemplateSelector {

    /**
     * Retorna la URI del template según el código de sociedad.
     * Se esperan los valores "SAPI", "SA" o "SRL".  
     * Podemos modificar las rutas o incluso inyectarlas desde un archivo de configuración.
     */
    public static String getTemplatePath(String codigoSociedad, CloudConfigProperties cloudConfigProperties) {
        String templateUri = "gs://" + cloudConfigProperties.getStorage().getBucketName() + "/drafts/";
        if (codigoSociedad == null || codigoSociedad.isBlank()) {
            // Template por defecto si no se especifica el código.
            return "gs://tu-bucket/drafts/machote_default.docx";
        }
        
        // Convertir a mayúsculas para evitar problemas de mayúsculas/minúsculas.
        String codigo = codigoSociedad.trim().toUpperCase();
        
        switch (codigo) {
            case "SAPI":
                return templateUri + "machote_SAPI.docx";
            case "SA":
                return templateUri + "machote_SA_de_CV.docx";
            case "SRL":
                return templateUri + "machote_SRL.docx";
            default:
                return templateUri + "machote_default.docx";
        }
    }
    //TO-DO:Esto podríamos cambiarlo por una consulta a la base de datos desde un servicio.
}