package com.temis.app.state.with_user;

import com.temis.app.config.properties.TwilioConfigProperties;
import com.temis.app.config.properties.WhatsappApiConfigProperties;
import com.temis.app.entity.DocumentEntity;
import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.UserEntity;
import com.temis.app.service.FileStorageService;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ProcessFileIntransitableState extends IntransitableWithUserStateTemplate {

    @Autowired
    private TwilioConfigProperties twilioConfigProperties;
    @Autowired
    private WhatsappApiConfigProperties whatsappApiConfigProperties;
    @Autowired
    private FileStorageService fileStorageService;

    @Override
    protected void Intransitable(MessageContextEntity message, UserEntity user) throws IOException {
        if(message.getMediaUrl() != null){
            var mediaUrl = message.getMediaUrl();
            var mediaContentType = message.getMediaContentType();

            HttpHeaders headers = new HttpHeaders();

            switch (message.getMessageSource()){
                case TWILIO -> {
                    String authStr = twilioConfigProperties.accountSid() + ":" + twilioConfigProperties.authToken();

                    ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(authStr);

                    var authEncoded =  Base64.getEncoder().encodeToString(byteBuffer.array());

                    headers.add("Authorization", "Basic " + authEncoded);

                }
                case META -> {
                    headers.add("Authorization", "Bearer " + whatsappApiConfigProperties.token());
                }
                default -> throw new UnsupportedOperationException();
            }

            headers.add("Accept", mediaContentType);

            var stream = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(
                            HttpClient.create().followRedirect(true)
                    )).build().get()
                    .uri(mediaUrl)
                    .headers(httpHeaders -> {
                        httpHeaders.addAll(headers);
                    })
                    .exchangeToFlux(clientResponse -> clientResponse.body(BodyExtractors.toDataBuffers()))
                    .map(DataBuffer::asInputStream)
                    .reduce(SequenceInputStream::new);

            AtomicReference<DocumentEntity> document = new AtomicReference<>();
            stream.doOnNext(inputStream -> {
                try {
                    document.set(fileStorageService.UploadDocumentStream(user, UUID.randomUUID().toString(), mediaContentType, inputStream));

                    inputStream.close();
                } catch (IOException | MimeTypeException e) {
                    throw new RuntimeException(e);
                }
            }).block().close();
            //Es intencional que brinque el error aquí
            assert document.get() != null;
            message.setMediaUrl(document.get().getPath());
        }
    }
}
