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
        "  \"datosGenerales\": {\n" +
        "    \"dia\": \"siete\",\n" +
        "    \"mes\": \"enero\",\n" +
        "    \"ano\": \"dos mil veinticinco\"\n" +
        "  },\n" +
        "  \"socios\": [\n" +
        "    {\n" +
        "      \"nombre\": \"DIEGO DE LA VEGA WILLIAMSON\",\n" +
        "      \"nacionalidad\": \"mexicano\",\n" +
        "      \"correo\": \"diego@temislegal.ai\",\n" +
        "      \"telefono\": \"8122000899\",\n" +
        "      \"curp\": \"VEWD930120HNLGLG02\",\n" +
        "      \"direccion\": \"C PEDRO VALDIVIA 1512, COL JARDINES DE MIRASIERRA 66236, SAN PEDRO GARZA GARCIA, N.L.\",\n" +
        "      \"estado_civil\": \"soltero\",\n" +
        "      \"rfc\": \"VEWD930120KJ2\",\n" +
        "      \"conyuge\": null,\n" +
        "      \"participacion\": 50\n" +
        "    },\n" +
        "    {\n" +
        "      \"nombre\": \"IVAN ALEXIS CANTU GARCIA\",\n" +
        "      \"nacionalidad\": \"mexicano\",\n" +
        "      \"correo\": \"ivan@temislegal.ai\",\n" +
        "      \"telefono\": \"8122000288\",\n" +
        "      \"curp\": \"CAG19041007HNILNRV09\",\n" +
        "      \"direccion\": \"C WESLACO 325, COL PUERTA DEL NORTE 66054, GRAL. ESCOBEDO, N.L.\",\n" +
        "      \"estado_civil\": \"casado\",\n" +
        "      \"rfc\": \"CAG190410KJ4\",\n" +
        "      \"conyuge\": {\n" +
        "        \"nombre\": \"Gloria Aguirre Gutierrez\",\n" +
        "        \"curp\": \"AUGG990124HBSGNB05\",\n" +
        "        \"direccion\": \"C WESLACO 325, COL PUERTA DEL NORTE 66054, GRAL. ESCOBEDO, N.L.\"\n" +
        "      },\n" +
        "      \"participacion\": 50\n" +
        "    }\n" +
        "  ],\n" +
        "  \"administracion\": {\n" +
        "    \"tipo\": \"Consejo de Administración\",\n" +
        "    \"presidente\": \"DIEGO DE LA VEGA WILLIAMSON\",\n" +
        "    \"secretario\": \"IVAN ALEXIS CANTU GARCIA\",\n" +
        "    \"tesorero\": \"Marcelo gomez sada\",\n" +
        "    \"forma_de_ejercicio\": \"Unanimidad\"\n" +
        "  },\n" +
        "  \"comisario\": {\n" +
        "    \"nombre\": \"Francisco perez gongora\",\n" +
        "    \"rfc\": \"FOFJO345398UJ\"\n" +
        "  },\n" +
        "  \"tipo_sociedad\": \"Sociedad de Responsabilidad Limitada (S. de R.L.)\",\n" +
        "  \"objeto_social\": \"Venta de equipo de cocina\",\n" +
        "  \"numero_socios\": 2,\n" +
        "  \"tamano_empresa\": \"Pequeña\",\n" +
        "  \"capital_social\": 20000,\n" +
        "  \"denominaciones\": [\"cocimax\", \"cocinator\", \"cocinas diego\"],\n" +
        "  \"ubicacion\": \"Monterrey, Nuevo León\"\n" +
        "}";


                UserEntity user = new UserEntity();
                user.setId((long) 1);
                String draft = clientVirtualAssistantService.generateCompanyIncorporationDraft(inputJson, user);
                System.out.println("Borrador generado:\n" + draft);
                return draft;
    }
}
