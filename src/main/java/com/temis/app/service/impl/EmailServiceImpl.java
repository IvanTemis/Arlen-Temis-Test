package com.temis.app.service.impl;

import com.temis.app.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.util.Pair;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private MimeMessageHelper HTMLEmailTemplate(MimeMessage message, String to, String body) throws MessagingException {
        var helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setText(body, true);
        return helper;
    }

    @Override
    public void SendHtmlEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        message.setSubject(subject);

        MimeMessageHelper helper = HTMLEmailTemplate(message, to, body);

        mailSender.send(message);

        log.info("HTML email sent to {}", to);
    }

    @SafeVarargs
    @Override
    public final void SendHtmlEmailWithAttachments(String to, String subject, String body, Pair<String, ByteArrayResource>... attachments) throws MessagingException {
        SendHtmlEmailWithAttachments(to, subject, body, null, attachments);
    }

    @Override
    public final void SendHtmlEmailWithAttachments(String to, String subject, String body, String[] bcc, Pair<String, ByteArrayResource>... attachments) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        message.setSubject(subject);
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        if (bcc != null && bcc.length > 0) {
            helper.setBcc(bcc);
        }
        helper.setText(body, true);


        //TO-DO Regresar codigo para que envie documento.
        /*for (Pair<String, ByteArrayResource> attachment : attachments) {
            helper.addAttachment(attachment.getFirst(), attachment.getSecond());
        }*/

        mailSender.send(message);

        log.info("HTML email with attachments sent to {} with BCC: {}", to, bcc != null ? String.join(", ", bcc) : "None");
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