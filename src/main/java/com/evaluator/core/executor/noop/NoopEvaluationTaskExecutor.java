package com.evaluator.core.executor.noop;

import com.evaluator.core.executor.TaskExecutor;
import com.evaluator.core.model.EvaluationInputModel;
import com.evaluator.core.model.EvaluationOutputModel;
import com.evaluator.core.model.ExecutionStatus;
import org.jetbrains.annotations.NotNull;

public class NoopEvaluationTaskExecutor implements TaskExecutor<EvaluationInputModel, EvaluationOutputModel> {
    @NotNull
    @Override
    public EvaluationOutputModel performExecution(@NotNull EvaluationInputModel inputModel) {
        return new EvaluationOutputModel(inputModel.id(), inputModel.id() * 100);
    }

    @NotNull
    @Override
    public EvaluationOutputModel createFailureModel(
            @NotNull EvaluationInputModel inputModel, @NotNull ExecutionStatus status) {
        return new EvaluationOutputModel(inputModel.id(), status, 0);
    }
}
