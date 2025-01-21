package com.temis.app.client;

import com.google.cloud.tasks.v2.*;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import java.io.IOException;
import java.nio.charset.Charset;

public class CloudTaskClient {

    private final String projectId;
    private final String locationId;
    private final String serviceUrl;
    private final String serviceAccountEmail;

    public CloudTaskClient(String projectId, String locationId, String serviceUrl, String serviceAccountEmail) {
        this.projectId = projectId;
        this.locationId = locationId;
        this.serviceUrl = serviceUrl;
        this.serviceAccountEmail = serviceAccountEmail;
    }

    public void DeleteTask(String taskName) throws IOException {
        try (CloudTasksClient client = CloudTasksClient.create()) {
            client.deleteTask(taskName);
        }
    }

    public Task CreateTask(String queueId, String relativeEndpoint, HttpMethod httpMethod, ByteString body, Timestamp scheduleTime) throws IOException {
        try (CloudTasksClient client = CloudTasksClient.create()) {

            String queuePath = QueueName.of(projectId, locationId, queueId).toString();

            // Add your service account email to construct the OIDC token.
            // in order to add an authentication header to the request.
            OidcToken.Builder oidcTokenBuilder =
                    OidcToken.newBuilder().setServiceAccountEmail(serviceAccountEmail);

            Task.Builder taskBuilder = Task.newBuilder()
                    .setScheduleTime(scheduleTime)
                    .setHttpRequest(
                            HttpRequest.newBuilder()
                                    .setBody(body)
                                    .setHttpMethod(httpMethod)
                                    .setOidcToken(oidcTokenBuilder)
                                    .setUrl(serviceUrl + relativeEndpoint)
                                    .build());

            return client.createTask(queuePath, taskBuilder.build());
        }
    }

    public <T> Task CreateTask(String queueId, String relativeEndpoint, HttpMethod httpMethod, T body, Long delay) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(body);

        return this.CreateTask(queueId, relativeEndpoint, httpMethod, ByteString.copyFrom(json, Charset.defaultCharset()), Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000L + delay).build());
    }
}
