package com.temis.app.controller;

import com.temis.app.client.GoogleCalendarClient;
import com.google.api.services.calendar.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/calendar")
public class GoogleCalendarController {

    @Autowired
    private GoogleCalendarClient googleCalendarClient;

    @PostMapping("/createEvent")
    public String createEvent(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam String startDateTime,
            @RequestParam String endDateTime,
            @RequestParam(required = false) String[] attendeesEmails) {

        try {
            Event event = googleCalendarClient.createEvent(
                "ivan.cantu.garcia@gmail.com",//TO-DO CAMBIAR POR EL DE TEMIS
                    title,
                    description,
                    location,
                    startDateTime,
                    endDateTime,
                    attendeesEmails
            );
            return "Evento creado exitosamente con ID: " + event.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al crear el evento: " + e.getMessage();
        }
    }
}