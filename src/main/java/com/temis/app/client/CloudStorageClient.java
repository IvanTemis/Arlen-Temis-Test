package com.temis.app.client;

import com.google.cloud.storage.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;

@Slf4j
public class CloudStorageClient {
    // The ID of your GCP project
    String projectId;

    // The ID of your GCS bucket
    String bucketName;

    Storage storage;

    public CloudStorageClient(String projectId, String bucketName) {
        this.projectId = projectId;
        this.bucketName = bucketName;
        this.storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    }

    public Blob UploadFileStream(String objectName, String contentType, InputStream inputStream) throws IOException {

        log.info("Starting upload of Streamed file into bucket {} as {}", bucketName, objectName);

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

        Storage.BlobWriteOption precondition;
        if (storage.get(bucketName, objectName) == null) {
            // For a target object that does not yet exist, set the DoesNotExist precondition.
            // This will cause the request to fail if the object is created before the request runs.
            precondition = Storage.BlobWriteOption.doesNotExist();
        } else {
            // If the destination already exists in your bucket, instead set a generation-match
            // precondition. This will cause the request to fail if the existing object's generation
            // changes before the request runs.
            precondition =
                    Storage.BlobWriteOption.generationMatch(
                            storage.get(bucketName, objectName).getGeneration());
        }

        var blob = storage.createFrom(blobInfo, inputStream, precondition);

        log.info("Uploaded Streamed file into bucket {} as {}", bucketName, objectName);

        return blob;
    }


    public String ReadFile(String objectUri) throws IOException {
        assert objectUri.startsWith("gs://");

        var uri = objectUri.replace("gs://", "");

        var slash = uri.indexOf('/');

        return ReadFile(uri.substring(0, slash), uri.substring(slash + 1));
    }

    public String ReadFile(String bucketName, String objectName) throws IOException {
        log.info("Begining read of file from bucket {} as {}", bucketName, objectName);

        BlobId blobId = BlobId.of(bucketName, objectName);

        Blob blob = storage.get(blobId);

        if(blob == null){
            throw new InvalidPathException("gs://" + bucketName + "/" + objectName, "Returned null in project " + projectId);
        }

        blob.getContentType();

        try (var reader = blob.reader(); InputStream stream = Channels.newInputStream(reader)) {

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = stream.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }

            return result.toString(StandardCharsets.UTF_8);
        }
    }
}
