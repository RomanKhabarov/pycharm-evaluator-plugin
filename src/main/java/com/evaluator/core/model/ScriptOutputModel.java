package com.evaluator.core.model;

import org.jetbrains.annotations.NotNull;

public record ScriptOutputModel(int id, @NotNull ExecutionStatus status, @NotNull EvaluationDataItem dataItem, @NotNull String scriptOutput) implements OutputModel {
    public ScriptOutputModel(int id, @NotNull EvaluationDataItem dataItem, @NotNull String scriptOutput) {
        this(id, ExecutionStatus.SUCCESS, dataItem, scriptOutput);
    }
}
