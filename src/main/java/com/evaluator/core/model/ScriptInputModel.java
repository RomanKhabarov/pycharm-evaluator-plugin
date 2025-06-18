package com.evaluator.core.model;

import org.jetbrains.annotations.NotNull;

public record ScriptInputModel(int id, @NotNull String scriptPath, @NotNull EvaluationDataItem dataItem) implements IdentifiableModel {
}
