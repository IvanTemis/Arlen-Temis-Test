package com.temis.app.service.impl;

import com.temis.app.client.CloudStorageClient;
import com.temis.app.entity.DocumentEntity;
import com.temis.app.entity.DocumentTypeEntity;
import com.temis.app.entity.UserEntity;
import com.temis.app.repository.DocumentRepository;
import com.temis.app.repository.DocumentTypeRepository;
import com.temis.app.service.FileStorageService;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private DocumentTypeRepository documentTypeRepository;
    @Autowired
    private CloudStorageClient cloudStorageClient;


    @Override
    public DocumentEntity UploadDocumentStream(@Nullable UserEntity owner, String documentName, String fileType, InputStream inputStream) throws IOException, MimeTypeException {

        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        MimeType type = allTypes.forName(fileType);
        String extension = type.getExtension();

        String path;
        if(owner == null){
            path = "general/" + documentName + extension;
        }
        else {
            path = owner.getId() + "/" + documentName + extension;
        }

        var blob = cloudStorageClient.UploadFileStream(path, fileType, inputStream);

        var document = new DocumentEntity();

        document.setDocumentType(null);
        document.setUploadedBy(owner);
        document.setFileType(fileType);
        document.setName(documentName + extension);
        document.setPath("gs://" + blob.getBucket() + "/" +  blob.getName());
        document.setIsActive(true);
        document.setLastModifiedDate(new Date());
        document.setReceptionDate(new Date());

        return documentRepository.save(document);
    }
}
