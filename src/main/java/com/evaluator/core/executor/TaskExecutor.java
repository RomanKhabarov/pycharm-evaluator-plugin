package com.evaluator.core.executor;

import com.evaluator.core.model.ExecutionStatus;
import com.evaluator.core.model.IdentifiableModel;
import com.evaluator.core.model.OutputModel;
import org.jetbrains.annotations.NotNull;

public interface TaskExecutor<I extends IdentifiableModel, O extends OutputModel> {
    @NotNull
    O performExecution(@NotNull I inputModel) throws InterruptedException;

    @NotNull
    O createFailureModel(@NotNull I inputModel, @NotNull ExecutionStatus status);
}
