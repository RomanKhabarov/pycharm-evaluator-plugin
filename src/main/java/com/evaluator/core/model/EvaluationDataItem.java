package com.evaluator.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public record EvaluationDataItem(@NotNull String input, @NotNull String referenceOutput) {
    @JsonCreator
    public EvaluationDataItem(@JsonProperty("input") String input, @JsonProperty("reference_output") String referenceOutput) {
        this.input = input != null ? input : "";
        this.referenceOutput = referenceOutput != null ? referenceOutput : "";
    }
}
