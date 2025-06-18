package com.evaluator.core.executor;

import com.evaluator.core.ExecutionController;
import com.evaluator.core.model.EvaluationInputModel;
import com.evaluator.core.model.EvaluationOutputModel;
import com.evaluator.core.model.ExecutionStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class EvaluationTaskExecutor implements TaskExecutor<EvaluationInputModel, EvaluationOutputModel> {
    private static final Logger LOG = Logger.getInstance(ExecutionController.class);
    private static final int SUCCESS_STATUS_CODE = 200;
    private static final int TOO_MANY_REQUESTS_STATUS_CODE = 429;
    private static final int TOO_MANY_REQUESTS_MAX_ATTEMPTS = 3;

    @Nullable
    private final String openApiKey;
    @NotNull
    private final ObjectMapper objectMapper;

    public EvaluationTaskExecutor(@Nullable String openApiKey) {
        this.openApiKey = openApiKey;
        this.objectMapper = createObjectMapper();
    }

    @NotNull
    private HttpResponse<InputStream> getResponse(@NotNull HttpClient client, @NotNull HttpRequest request) throws InterruptedException, IOException {
        HttpResponse<InputStream> response = null;
        for (int i = 0; i < TOO_MANY_REQUESTS_MAX_ATTEMPTS; i++) {
            if (response != null) {
                Optional<String> retryAfterHeader = response.headers().firstValue("Retry-After");
                int retrySeconds = retryAfterHeader.map(Integer::parseInt).orElse(3);
                LOG.warn(String.format("Response code 429: waiting for %d seconds, attempt %d", retrySeconds, i));
                Thread.sleep(Duration.ofSeconds(retrySeconds).toMillis());
            }
            response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != TOO_MANY_REQUESTS_STATUS_CODE) {
                break;
            }
        }
        return response;
    }

    @NotNull
    @Override
    public EvaluationOutputModel performExecution(@NotNull EvaluationInputModel inputModel) throws InterruptedException {
        ChatGptRequestModel requestModel = new ChatGptRequestModel("gpt-3.5-turbo", List.of(new ChatGptMessage("user", inputModel.prompt())), 0.3);
        String requestBody = serializeObject(requestModel);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<InputStream> response = getResponse(client, request);
            if (response.statusCode() == SUCCESS_STATUS_CODE) {
                ChatGptResponse responseModel = deserializeObject(response.body(), ChatGptResponse.class);
                if (responseModel != null && responseModel.choices() != null) {
                    int score = responseModel.choices().stream()
                            .findFirst()
                            .flatMap(c -> Optional.ofNullable(c.message()))
                            .flatMap(m -> Optional.ofNullable(m.content()))
                            .map(Integer::valueOf)
                            .orElseThrow();
                    return new EvaluationOutputModel(inputModel.id(), score);
                }
                throw new TaskExecutionException("Failed to parse ChatGPT response");
            }
            throw new TaskExecutionException("Request failed with status code: " + response.statusCode());

        } catch (InterruptedException e) {
            LOG.info(String.format("%d Evaluation request interrupted", inputModel.id()));
            throw e;

        } catch (IOException e) {
            LOG.info(String.format("%d Evaluation IOException", inputModel.id()));
            throw new UncheckedIOException("Exception during evaluation", e);
        }
    }

    @NotNull
    @Override
    public EvaluationOutputModel createFailureModel(@NotNull EvaluationInputModel inputModel, @NotNull ExecutionStatus status) {
        return new EvaluationOutputModel(inputModel.id(), status, 0);
    }

    @NotNull
    private String serializeObject(@NotNull Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Nullable
    private <T> T deserializeObject(@NotNull InputStream inputStream, @NotNull Class<T> tClass) {
        try {
            return objectMapper.readValue(inputStream, tClass);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @NotNull
    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    private record ChatGptMessage(@Nullable String role, @Nullable String content) {
        @JsonCreator
        public ChatGptMessage(@JsonProperty("role") String role, @JsonProperty("content") String content) {
            this.role = role;
            this.content = content;
        }
    }

    private record ChatGptRequestModel(@Nullable String model, @Nullable List<ChatGptMessage> messages, @Nullable Double temperature) {
        @JsonCreator
        public ChatGptRequestModel(@JsonProperty("model") String model, @JsonProperty("messages") List<ChatGptMessage> messages, @JsonProperty("temperature") Double temperature) {
            this.model = model;
            this.messages = messages;
            this.temperature = temperature;
        }
    }

    private record ChatGptResponse(@Nullable List<ChatGptChoice> choices) {
        @JsonCreator
        public ChatGptResponse(@JsonProperty("choices") List<ChatGptChoice> choices) {
            this.choices = choices;
        }
    }

    private record ChatGptChoice(@Nullable ChatGptMessage message) {
        @JsonCreator
        public ChatGptChoice(@JsonProperty("message") ChatGptMessage message) {
            this.message = message;
        }
    }
}
