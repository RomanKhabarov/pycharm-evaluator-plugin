package com.evaluator.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public record EvaluationDataSet(@Nullable String modelPath, @NotNull List<EvaluationDataItem> data) {
    @JsonCreator
    public EvaluationDataSet(@JsonProperty("model_path") String modelPath, @JsonProperty("data") List<EvaluationDataItem> data) {
        this.modelPath = modelPath;
        this.data = data != null ? Collections.unmodifiableList(data) : List.of();
    }
}
