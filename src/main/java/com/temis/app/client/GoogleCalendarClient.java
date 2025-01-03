package com.temis.app.client;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarClient {

    private final Calendar calendarService;

    public GoogleCalendarClient(String applicationName) throws IOException {
        NetHttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        this.calendarService = new Calendar.Builder(
                httpTransport,
                jsonFactory,
                new com.google.auth.http.HttpCredentialsAdapter(credentials))
                .setApplicationName(applicationName)
                .build();
    }

    public Calendar getCalendarService() {
        return calendarService;
    }

    public List<Event> getEventsInRange(String calendarId, String timeMin, String timeMax) throws IOException {
        DateTime minTime = new DateTime(timeMin);
        DateTime maxTime = new DateTime(timeMax);

        Events events = calendarService.events().list(calendarId)
                .setTimeMin(minTime)
                .setTimeMax(maxTime)
                .setSingleEvents(true)
                .execute();

        return events.getItems();
    }

    public Event createEvent(String calendarId, String title, String description, String location, String startDateTime, String endDateTime, String[] attendeesEmails) throws IOException {

        Event event = new Event()
                .setSummary(title)
                .setDescription(description)
                .setLocation(location);

        EventDateTime start = new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(startDateTime));
        EventDateTime end = new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(endDateTime));
        event.setStart(start).setEnd(end);

        if (attendeesEmails != null) {
            EventAttendee[] attendees = new EventAttendee[attendeesEmails.length];
            for (int i = 0; i < attendeesEmails.length; i++) {
                attendees[i] = new EventAttendee().setEmail(attendeesEmails[i]);
            }
            event.setAttendees(List.of(attendees));
        }

        return calendarService.events().insert(calendarId, event).execute();
    }
}