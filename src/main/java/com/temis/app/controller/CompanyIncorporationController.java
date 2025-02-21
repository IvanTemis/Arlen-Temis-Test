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
                     
        String inputJson = "{"
        + "\"socios\": ["
        + "{"
        + "\"id\": \"SOC001\","
        + "\"nombre_completo\": \"Diego de la Vega Williamson\","
        + "\"nombre\": \"Diego\","
        + "\"primer_apellido\": \"de la Vega\","
        + "\"segundo_apellido\": \"Williamson\","
        + "\"nacionalidad\": \"mexicano\","
        + "\"correo\": \"diego@temislegal.ai\","
        + "\"telefono\": \"8122000899\","
        + "\"curp\": \"VEWD930120HNLGLG02\","
        + "\"ine_num\": \"1234567890\","
        + "\"direccion_completa\": \"C PEDRO VALDIVIA 1512, COL JARDINES DE MIRASIERRA 66236, SAN PEDRO GARZA GARCIA, N.L.\","
        + "\"ciudad_nacimiento\": \"San Pedro Garza García\","
        + "\"estado_nacimiento\": \"Nuevo León\","
        + "\"nacimiento_dia\": 20,"
        + "\"nacimiento_mes\": 1,"
        + "\"nacimiento_ano_num\": 1993,"
        + "\"nacimiento_ano_texto\": \"Mil novecientos noventa y tres\","
        + "\"estado_civil\": \"soltero\","
        + "\"rfc\": \"VEWD930120KJ2\","
        + "\"participacion\": {"
        + "\"porcentaje\": 50,"
        + "\"monto\": 10000"
        + "}"
        + "},"
        + "{"
        + "\"id\": \"SOC002\","
        + "\"nombre_completo\": \"Iván Alexis Cantú García\","
        + "\"nombre\": \"Iván Alexis\","
        + "\"primer_apellido\": \"Cantú\","
        + "\"segundo_apellido\": \"García\","
        + "\"nacionalidad\": \"mexicano\","
        + "\"correo\": \"ivan@temislegal.ai\","
        + "\"telefono\": \"8122000288\","
        + "\"curp\": \"CAG19041007HNILNRV09\","
        + "\"ine_num\": \"0987654321\","
        + "\"direccion_completa\": \"C WESLACO 325, COL PUERTA DEL NORTE 66054, GRAL. ESCOBEDO, N.L.\","
        + "\"ciudad_nacimiento\": \"Monterrey\","
        + "\"estado_nacimiento\": \"Nuevo León\","
        + "\"nacimiento_dia\": 10,"
        + "\"nacimiento_mes\": 4,"
        + "\"nacimiento_ano_num\": 1990,"
        + "\"nacimiento_ano_texto\": \"Mil novecientos noventa\","
        + "\"estado_civil\": \"casado\","
        + "\"rfc\": \"CAG190410KJ4\","
        + "\"participacion\": {"
        + "\"porcentaje\": 50,"
        + "\"monto\": 10000"
        + "},"
        + "\"conyuge\": {"
        + "\"id\": \"CON001\","
        + "\"nombre_completo\": \"Gloria Aguirre Gutiérrez\","
        + "\"nombre\": \"Gloria\","
        + "\"primer_apellido\": \"Aguirre\","
        + "\"segundo_apellido\": \"Gutiérrez\","
        + "\"curp\": \"AUGG990124HBSGNB05\","
        + "\"direccion\": \"C WESLACO 325, COL PUERTA DEL NORTE 66054, GRAL. ESCOBEDO, N.L.\""
        + "}"
        + "}"
        + "],"
        + "\"administracion\": {"
        + "\"id\": \"ADM001\","
        + "\"tipo\": \"Consejo de Administración\","
        + "\"forma_de_ejercicio\": \"Unanimidad\","
        + "\"presidente\": {"
        + "\"nombre_completo\": \"Diego de la Vega Williamson\","
        + "\"rfc\": \"VEWD930120KJ2\""
        + "},"
        + "\"secretario\": {"
        + "\"nombre_completo\": \"Iván Alexis Cantú García\","
        + "\"rfc\": \"CAG190410KJ4\""
        + "},"
        + "\"tesorero\": {"
        + "\"nombre_completo\": \"Marcelo Gómez Sada\","
        + "\"rfc\": \"GOSD880201HD1\""
        + "}"
        + "},"
        + "\"comisario\": {"
        + "\"id\": \"COM001\","
        + "\"nombre_completo\": \"Francisco Pérez Góngora\","
        + "\"rfc\": \"FOFJO345398UJ\""
        + "},"
        + "\"datos_generales\": {"
        + "\"id\": \"DG001\","
        + "\"transaccion\": {"
        + "\"id\": \"TXN001\""
        + "},"
        + "\"dia\": \"06\","
        + "\"mes\": \"Febrero\","
        + "\"ano\": \"2025\","

        + "\"tipo_sociedad\": \"Sociedad Anónima Promotora de Inversión (S.A.P.I.)\","
        + "\"codigo_sociedad\": \"SAPI\","

        /* + "\"tipo_sociedad\": \"Sociedad Anónima de Capital Variable (S.A. de C.V.)\","
        + "\"codigo_sociedad\": \"SA\"," */


        + "\"objeto_social\": \"Venta de equipo de cocina\","
        + "\"numero_socios\": 2,"
        + "\"capital_social\": {"
        + "\"numero\": 20000,"
        + "\"texto\": \"Veinte mil pesos\""
        + "},"
        + "\"ubicacion_completa\": \"Monterrey, Nuevo León, México\","
        + "\"ubicacion\": {"
        + "\"ciudad\": \"Monterrey\","
        + "\"estado\": \"Nuevo León\""
        + "}"
        + "},"
        + "\"denominaciones\":"
        + "{"
        + "\"id\": \"DEN001\","
        + "\"nombre_confirmado\": \"Cocimax\","
        + "\"denominaciones_tentativas\": [\"cocimax\", \"cocinator\", \"cocinas diego\"]"
        + "}"
        + "}";


                UserEntity user = new UserEntity();
                user.setId((long) 1);
                String draft = clientVirtualAssistantService.generateCompanyIncorporationDraft(inputJson, user);
                System.out.println("Borrador generado:\n" + draft);
                return draft;
    }
}
