package com.temis.app.client;

import com.google.cloud.tasks.v2.*;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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
        log.info("Deleting task '{}'", taskName);
        try (CloudTasksClient client = CloudTasksClient.create()) {
            client.deleteTask(taskName);
        }
    }

    public Task CreateTask(String queueId, String relativeEndpoint, HttpMethod httpMethod, Map<String, String> headers, ByteString body, Timestamp scheduleTime) throws IOException {
        log.info("Creating {} task in queue '{}' for endpoint '{}':\n{}\n{}\n{}", httpMethod, queueId, relativeEndpoint,headers,body,scheduleTime);
        try (CloudTasksClient client = CloudTasksClient.create()) {

            String queuePath = QueueName.of(projectId, locationId, queueId).toString();

            String targetUrl = serviceUrl + relativeEndpoint;

            // Add your service account email to construct the OIDC token.
            // in order to add an authentication header to the request.
            var oidcToken =  OidcToken.newBuilder().setAudience(targetUrl).setServiceAccountEmail(serviceAccountEmail);
            //var oauth = OAuthToken.newBuilder().setServiceAccountEmail(serviceAccountEmail);

            Task.Builder taskBuilder = Task.newBuilder()
                    .setScheduleTime(scheduleTime)
                    .setHttpRequest(
                            HttpRequest.newBuilder()
                                    .putAllHeaders(headers)
                                    .setBody(body)
                                    .setHttpMethod(httpMethod)
                                    .setUrl(targetUrl)
                                    .setOidcToken(oidcToken)
                                    //.setOauthToken(oauth)
                                    .build());

            log.info("Creating task...");

            var task = client.createTask(queuePath, taskBuilder.build());

            log.info("Created task {}", task.getName());

            return task;
        }
    }

    public <T> Task CreateTask(String queueId, String relativeEndpoint, HttpMethod httpMethod, T body, Long delay) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(body);

        var headers = new HashMap<String, String>(){{
            put("Content-Type", "application/json");
        }};

        return this.CreateTask(queueId, relativeEndpoint, httpMethod, headers, ByteString.copyFrom(json, Charset.defaultCharset()), Timestamp.newBuilder().setSeconds(/*System.currentTimeMillis() / 1000L +*/ delay).build());
    }
}
