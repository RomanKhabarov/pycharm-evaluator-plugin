package com.evaluator.core.model;

import org.jetbrains.annotations.NotNull;

public record EvaluationOutputModel(int id, @NotNull ExecutionStatus status, int score) implements OutputModel {
    public EvaluationOutputModel(int id, int score) {
        this(id, ExecutionStatus.SUCCESS, score);
    }
}
