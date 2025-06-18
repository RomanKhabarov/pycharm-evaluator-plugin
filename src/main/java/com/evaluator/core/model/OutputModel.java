package com.evaluator.core.model;

import org.jetbrains.annotations.NotNull;

public interface OutputModel extends IdentifiableModel {
    @NotNull
    ExecutionStatus status();
}
