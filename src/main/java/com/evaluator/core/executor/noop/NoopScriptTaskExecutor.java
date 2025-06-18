package com.evaluator.core.executor.noop;

import com.evaluator.core.executor.TaskExecutor;
import com.evaluator.core.model.ExecutionStatus;
import com.evaluator.core.model.ScriptInputModel;
import com.evaluator.core.model.ScriptOutputModel;
import org.jetbrains.annotations.NotNull;

public class NoopScriptTaskExecutor implements TaskExecutor<ScriptInputModel, ScriptOutputModel> {
    @NotNull
    @Override
    public ScriptOutputModel performExecution(@NotNull ScriptInputModel inputModel) {
        return new ScriptOutputModel(inputModel.id(), inputModel.dataItem(), "Output: " + inputModel.id());
    }

    @NotNull
    @Override
    public ScriptOutputModel createFailureModel(@NotNull ScriptInputModel inputModel, @NotNull ExecutionStatus status) {
        return new ScriptOutputModel(inputModel.id(), status, inputModel.dataItem(), "");
    }
}
