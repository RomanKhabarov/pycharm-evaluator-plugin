package com.evaluator.plugin.service;

import com.evaluator.core.model.EvaluationDataSet;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.components.Service;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service(Service.Level.PROJECT)
public class FileToObjectService {
    @NotNull
    private final ObjectMapper objectMapper;

    public FileToObjectService() {
        this.objectMapper = createObjectMapper();
    }

    @NotNull
    public String loadPromptTemplate(@NotNull String path) {
        Objects.requireNonNull(path, "Path can't be null");
        try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Error while reading file", e);
        }
    }

    @Nullable
    public EvaluationDataSet loadDataset(@NotNull String path) {
        Objects.requireNonNull(path, "Path can't be null");
        try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
            return objectMapper.readValue(stream, EvaluationDataSet.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Error while reading file", e);
        }
    }

    @NotNull
    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
