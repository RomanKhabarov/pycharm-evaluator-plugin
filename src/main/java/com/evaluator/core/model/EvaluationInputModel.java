package com.evaluator.core.model;

import org.jetbrains.annotations.NotNull;

public record EvaluationInputModel(int id, @NotNull String prompt) implements IdentifiableModel {
}
