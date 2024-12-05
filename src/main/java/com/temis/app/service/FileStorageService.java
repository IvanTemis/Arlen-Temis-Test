package com.temis.app.service;

import com.temis.app.entity.DocumentEntity;
import com.temis.app.entity.UserEntity;
import org.apache.tika.mime.MimeTypeException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

public interface FileStorageService {

    public DocumentEntity UploadDocumentStream(@Nullable UserEntity owner, String documentName, String fileType, InputStream inputStream) throws IOException, MimeTypeException;
}
