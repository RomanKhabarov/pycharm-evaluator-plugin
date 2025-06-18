package com.evaluator.core.executor;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TaskExecutionException extends RuntimeException {
    public TaskExecutionException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public TaskExecutionException(@NotNull String message) {
        super(message);
    }
}
