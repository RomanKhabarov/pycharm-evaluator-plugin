package com.evaluator.core.listener;

import com.evaluator.core.model.IdentifiableModel;
import org.jetbrains.annotations.NotNull;

public interface TaskExecutionListener<O> {
    default void executionStarted(@NotNull IdentifiableModel taskInto) { }
    void executionFinished(@NotNull O taskResult);
}
