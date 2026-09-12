package com.example.TaskAPI.task.api;

import com.example.TaskAPI.security.JwtVerifier;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.enums.TaskStatus;
import com.example.TaskAPI.task.service.TaskService;
import com.example.TaskAPI.user.domain.entity.User;
import com.example.TaskAPI.web.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class TaskStreamControllerIntegrationTest extends BaseIntegrationTest {
    private final List<HttpClient> openClients = new ArrayList<>();
    @LocalServerPort
    private int port;
    @Autowired
    private TaskService taskService;

    @AfterEach
    void closeStreams() {
        openClients.forEach(HttpClient::shutdownNow);
        openClients.clear();
    }

    @Test
    void stream_unauthenticated_returns401() throws Exception {
        HttpResponse<Void> response = newClient()
                .send(requestBuilder(null).build(), HttpResponse.BodyHandlers.discarding());

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void stream_authenticated_receivesConnectedEvent() throws Exception {
        List<ReceivedEvent> receivedEvents = connectAndCollect(testUserToken);

        awaitConnected(receivedEvents);
    }

    @Test
    void stream_taskCreatedForConnectedUser_receivesTaskChangedEvent() throws Exception {
        List<ReceivedEvent> receivedEvents = connectAndCollect(testUserToken);

        awaitConnected(receivedEvents);

        Task task = taskService.createTask(
                Task.builder()
                        .title("Stream test task")
                        .status(TaskStatus.TODO)
                        .build(),
                null,
                Set.of(loginUser.getUuid()));

        Thread.sleep(2000);

        ReceivedEvent taskChanged = receivedEvents.stream()
                .filter(e -> e.name().equals("task-changed"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no task-changed event received; events=" + receivedEvents));

        String actualUuid = objectMapper.readTree(taskChanged.data()).get("taskUuid").asString();
        assertThat(actualUuid).isEqualTo(task.getUuid().toString());
    }

    @Test
    void stream_taskCreatedForDifferentUser_isNotReceived() throws Exception {
        User otherUser = createUser("stream-other-user");
        String otherUserToken = jwtIssuer.issue(
                otherUser.getId(), otherUser.getUsername(), List.of(JwtVerifier.DEFAULT_ROLE));
        List<ReceivedEvent> ownerEvents = connectAndCollect(testUserToken);
        List<ReceivedEvent> otherEvents = connectAndCollect(otherUserToken);

        awaitConnected(ownerEvents);
        awaitConnected(otherEvents);

        Task task = taskService.createTask(
                Task.builder()
                        .title("Owner-only task")
                        .status(TaskStatus.TODO)
                        .build(),
                null,
                Set.of(loginUser.getUuid()));

        Thread.sleep(2000);

        ReceivedEvent ownerTaskChanged = ownerEvents.stream()
                .filter(e -> e.name().equals("task-changed"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("owner stream never received task-changed; events=" + ownerEvents));
        String ownerActualUuid = objectMapper.readTree(ownerTaskChanged.data()).get("taskUuid").asString();
        assertThat(ownerActualUuid).isEqualTo(task.getUuid().toString());

        boolean otherReceivedIt = otherEvents.stream()
                .filter(e -> e.name().equals("task-changed"))
                .map(e -> {
                    try {
                        return objectMapper.readTree(e.data()).get("taskUuid").asString();
                    } catch (Exception ex) {
                        throw new RuntimeException("failed to parse json=[" + e.data() + "]", ex);
                    }
                })
                .anyMatch(uuid -> uuid.equals(task.getUuid().toString()));
        assertThat(otherReceivedIt).isFalse();
    }

    private HttpClient newClient() {
        HttpClient client = HttpClient.newHttpClient();

        openClients.add(client);

        return client;
    }

    private void awaitConnected(List<ReceivedEvent> receivedEvents) {
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(receivedEvents).anyMatch(e -> e.name().equals("connected")));
    }

    private HttpRequest.Builder requestBuilder(String token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/tasks/stream"))
                .GET();

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        return builder;
    }

    private List<ReceivedEvent> connectAndCollect(String token) {
        List<ReceivedEvent> receivedEvents = Collections.synchronizedList(new ArrayList<>());
        HttpClient client = newClient();

        Thread consumer = new Thread(() -> {
            try {
                HttpResponse<Stream<String>> response =
                        client.send(requestBuilder(token).build(), HttpResponse.BodyHandlers.ofLines());
                String[] pendingEventName = new String[1];

                try (Stream<String> lines = response.body()) {
                    lines.forEach(line -> {
                        if (line.startsWith("event:")) {
                            pendingEventName[0] = line.substring("event:".length()).trim();
                        } else if (line.startsWith("data:") && pendingEventName[0] != null) {
                            receivedEvents.add(new ReceivedEvent(
                                    pendingEventName[0], line.substring("data:".length()).trim()));
                            pendingEventName[0] = null;
                        }
                    });
                }
            } catch (IOException | InterruptedException ex) {
            }
        });

        consumer.setDaemon(true);
        consumer.start();

        return receivedEvents;
    }

    private record ReceivedEvent(String name, String data) {
    }
}
