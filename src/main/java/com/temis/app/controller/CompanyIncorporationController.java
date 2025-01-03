package com.temis.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.temis.app.dto.IncorporationRequest;
import com.temis.app.entity.UserEntity;
import com.temis.app.repository.UserRepository;
import com.temis.app.service.ClientVirtualAssistantService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/company-incorporation")
@Slf4j
public class CompanyIncorporationController {

    @Autowired
    private ClientVirtualAssistantService clientVirtualAssistantService;

    @PostMapping("/generate-draft")
    public String generateDraft(@RequestBody IncorporationRequest request) throws Exception {
                     
        
        String inputJson = "{\n" +
                    "  \"tipo_sociedad\": \"Sociedad por Acciones Simplificada (S.A.S.)\",\n" +
                    "  \"objeto_social\": \"Producción y venta de cosméticos y suplementos alimenticios\",\n" +
                    "  \"numero_socios\": 4,\n" +
                    "  \"tamano_empresa\": \"Startup\",\n" +
                    "  \"capital_social\": 20000,\n" +
                    "  \"denominaciones\": [\"Trigama\", \"Latridina\", \"Tirntrintontrontrueno\"],\n" +
                    "  \"ubicacion\": \"Escobedo, Nuevo León\",\n" +
                    "  \"socios\": [\n" +
                    "    {\n" +
                    "      \"nombre\": \"María Trinidad González Valdez\",\n" +
                    "      \"nacionalidad\": \"Mexicana\",\n" +
                    "      \"estado_civil\": \"Soltera\",\n" +
                    "      \"conyuge\": null\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"nombre\": \"Gabriel Aguirre González\",\n" +
                    "      \"nacionalidad\": \"Mexicana\",\n" +
                    "      \"estado_civil\": \"Soltero\",\n" +
                    "      \"conyuge\": null\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
                UserEntity user = new UserEntity();
                user.setId((long) 1);
                String draft = clientVirtualAssistantService.generateCompanyIncorporationDraft(inputJson, user);
                System.out.println("Borrador generado:\n" + draft);
                return draft;
    }
}
