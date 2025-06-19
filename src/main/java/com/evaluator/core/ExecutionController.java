package com.evaluator.core;

import com.evaluator.core.executor.EvaluationTaskExecutor;
import com.evaluator.core.executor.ScriptTaskExecutor;
import com.evaluator.core.listener.ScriptExecutionListener;
import com.evaluator.core.listener.TaskExecutionListener;
import com.evaluator.core.model.*;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class ExecutionController implements AutoCloseable {
    private static final Logger LOG = Logger.getInstance(ExecutionController.class);

    @NotNull
    private final EvaluationDataSet dataset;
    @NotNull
    private final TaskExecutionService<ScriptInputModel, ScriptOutputModel> scriptExecutionService;
    @NotNull
    private final TaskExecutionService<EvaluationInputModel, EvaluationOutputModel> evaluationExecutionService;

    public ExecutionController(@NotNull EvaluationDataSet dataset, @NotNull String promptTemplate) {
        this.dataset = dataset;
        this.scriptExecutionService = new TaskExecutionService<>(4, new ScriptTaskExecutor(), Duration.ofSeconds(2));
        this.evaluationExecutionService = new TaskExecutionService<>(6, new EvaluationTaskExecutor(System.getenv("OPENAI_API_KEY")), Duration.ofSeconds(2));
        addScriptExecutionListener(new ScriptExecutionListener(evaluationExecutionService, promptTemplate, 3));
    }

    public void addScriptExecutionListener(@NotNull TaskExecutionListener<ScriptOutputModel> listener) {
        this.scriptExecutionService.addExecutionListener(listener);
    }

    public void addEvaluationExecutionListener(@NotNull TaskExecutionListener<EvaluationOutputModel> listener) {
        this.evaluationExecutionService.addExecutionListener(listener);
    }

    public void runEvaluations() {
        LOG.info("Starting evaluation with dataset: " + dataset.modelPath());
        if (dataset.modelPath() != null) {
            for (int i = 0; i < dataset.data().size(); i++) {
                EvaluationDataItem dataItem = dataset.data().get(i);
                scriptExecutionService.execute(new ScriptInputModel(i, dataset.modelPath(), dataItem));
            }
        } else {
            throw new IllegalStateException("Dataset is incorrect");
        }
        LOG.info("Jobs submitted.");
    }

    @Override
    public void close() {
        LOG.info("Controller shutting down...");
        scriptExecutionService.close();
        evaluationExecutionService.close();
        LOG.info("Controller shutdown finished.");
    }
}
