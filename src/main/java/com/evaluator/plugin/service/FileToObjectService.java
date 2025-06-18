package com.evaluator.plugin.service;

import com.evaluator.core.model.EvaluationDataSet;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service(Service.Level.PROJECT)
public class FileToObjectService {
    @NotNull
    private final ObjectMapper objectMapper;

    public FileToObjectService() {
        this.objectMapper = createObjectMapper();
    }

    @NotNull
    public String loadPromptTemplateFromResource(@NotNull String path) {
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
    public EvaluationDataSet loadDatasetFromResource(@NotNull String path) {
        try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
            return objectMapper.readValue(stream, EvaluationDataSet.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Error while reading file", e);
        }
    }

    @Nullable
    public EvaluationDataSet loadDatasetFromProjectRoot(@NotNull Project project, @NotNull String relativePath) {
        if (project.getBasePath() == null) {
            throw new IllegalStateException("Project path is null");
        }
        Path path = Paths.get(project.getBasePath(), relativePath);
        try (var stream = Files.newInputStream(path)) {
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
