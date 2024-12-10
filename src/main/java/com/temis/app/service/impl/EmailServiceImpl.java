package com.temis.app.service.impl;

import com.temis.app.client.VertexAIClient;
import com.temis.app.model.DocumentSummarizeDTO;
import com.temis.app.service.EmailService;
import com.temis.app.service.SummarizeService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.logging.Logger;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void SendHtmlEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        message.setSubject(subject);
        MimeMessageHelper helper;
        helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setText(body, true);
        mailSender.send(message);

        log.info("HTML email sent to {}", to);
    }

    @Override
    public void SendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        log.info("Simple email sent to {}", to);
    }
}