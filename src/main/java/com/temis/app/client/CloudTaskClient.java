package com.temis.app.client;

import com.google.cloud.tasks.v2.*;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import java.time.Duration;

import javax.annotation.Nullable;
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

    public Task CreateTask(String queueId, @Nullable String taskName, String relativeEndpoint, HttpMethod httpMethod, Map<String, String> headers, ByteString body, Timestamp scheduleTime) throws IOException {
        log.info("Creating {} task in queue '{}' for endpoint '{}':\n{}\n{}\n{}", httpMethod, queueId, relativeEndpoint,headers,body,scheduleTime);

        var settings = CloudTasksSettings.newBuilder();

        if(taskName != null) {
            settings.createTaskSettings().retrySettings()
                    .setMaxAttempts(20)
                    .setInitialRetryDelayDuration(Duration.ofMillis(10))
                    .setMaxRetryDelayDuration(Duration.ofMillis(500))
                    .setRetryDelayMultiplier(1.3d)
                    .setMaxRpcTimeoutDuration(Duration.ofSeconds(2))
                    .setTotalTimeoutDuration(Duration.ofSeconds(3))
            ;
        }

        try (CloudTasksClient client = CloudTasksClient.create(settings.build())) {

            String queuePath = QueueName.of(projectId, locationId, queueId).toString();

            String targetUrl = serviceUrl + relativeEndpoint;

            // Add your service account email to construct the OIDC token.
            // in order to add an authentication header to the request.
            //Esto no se usa porque causa un error de "service_account_email must be set"
            //var oidcToken =  OidcToken.newBuilder().setAudience(targetUrl).setServiceAccountEmail(this.serviceAccountEmail).build();
            //var oauth = OAuthToken.newBuilder().setServiceAccountEmail(serviceAccountEmail).build();

            Task.Builder taskBuilder = Task.newBuilder()
                    .setScheduleTime(scheduleTime)
                    .setHttpRequest(
                            HttpRequest.newBuilder()
                                    .putAllHeaders(headers)
                                    .setBody(body)
                                    .setHttpMethod(httpMethod)
                                    .setUrl(targetUrl)
                                    //.setOidcToken(oidcToken)
                                    //.setOauthToken(oauth)
                                    .build());

            if(taskName != null && !taskName.isEmpty()){
                taskBuilder.setName("projects/" + projectId + "/locations/" + locationId + "/queues/" + queueId + "/tasks/" + taskName.replaceAll("[^A-Za-z0-9\\-_]","_"));
            }

            log.info("Creating task...");

            var task = client.createTask(queuePath, taskBuilder.build());

            log.info("Created task {}", task.getName());

            return task;
        }
    }

    public <T> Task CreateTask(String queueId, @Nullable String taskName, String relativeEndpoint, HttpMethod httpMethod, T body, Long delay) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(body);

        var headers = new HashMap<String, String>(){{
            put("Content-Type", "application/json");
        }};

        return this.CreateTask(queueId, taskName, relativeEndpoint, httpMethod, headers, ByteString.copyFrom(json, Charset.defaultCharset()), Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000L + delay).build());
    }
}
