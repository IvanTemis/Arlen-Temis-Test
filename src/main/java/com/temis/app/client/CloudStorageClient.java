package com.temis.app.client;

import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

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
}
