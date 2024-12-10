package com.temis.app.service;

import com.temis.app.model.DocumentSummarizeDTO;
import jakarta.mail.MessagingException;

import java.io.IOException;

public interface EmailService {

    void SendHtmlEmail(String to, String subject, String body) throws MessagingException;

    void SendSimpleEmail(String to, String subject, String body);

}