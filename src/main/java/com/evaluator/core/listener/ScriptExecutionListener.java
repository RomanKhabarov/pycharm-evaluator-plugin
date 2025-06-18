package com.evaluator.core.listener;

import com.evaluator.core.TaskExecutionService;
import com.evaluator.core.model.EvaluationInputModel;
import com.evaluator.core.model.EvaluationOutputModel;
import com.evaluator.core.model.ExecutionStatus;
import com.evaluator.core.model.ScriptOutputModel;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScriptExecutionListener implements TaskExecutionListener<ScriptOutputModel> {
    @NotNull
    private final TaskExecutionService<EvaluationInputModel, EvaluationOutputModel> evaluationExecutionService;
    @NotNull
    private final String promptTemplate;
    private final int numberOfEvaluations;

    public ScriptExecutionListener(
            @NotNull TaskExecutionService<EvaluationInputModel, EvaluationOutputModel> evaluationExecutionService,
            @NotNull String promptTemplate,
            int numberOfEvaluations) {
        this.evaluationExecutionService = evaluationExecutionService;
        this.promptTemplate = promptTemplate;
        this.numberOfEvaluations = numberOfEvaluations;
    }

    @NotNull
    private String buildPromptTemplate(@NotNull String promptTemplate, @NotNull ScriptOutputModel scriptOutputModel) {
        return promptTemplate
                .replace("{{input}}", scriptOutputModel.dataItem().input())
                .replace("{{reference_output}}", scriptOutputModel.dataItem().referenceOutput())
                .replace("{{model_output}}", scriptOutputModel.scriptOutput());
    }

    @Override
    public void executionFinished(@NotNull ScriptOutputModel scriptOutputModel) {
        if (scriptOutputModel.status() != ExecutionStatus.SUCCESS) {
            return;
        }
        String prompt = buildPromptTemplate(promptTemplate, scriptOutputModel);

        EvaluationInputModel evaluationInputModel = new EvaluationInputModel(scriptOutputModel.id(), prompt);

        evaluationExecutionService.executeAndReduce(evaluationInputModel, numberOfEvaluations, evaluationOutputs -> {
            int majorityScore = evaluationOutputs.stream()
                    .collect(Collectors.groupingBy(EvaluationOutputModel::score, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(0);

            Optional<EvaluationOutputModel> firstOutput =
                    evaluationOutputs.stream().findFirst();
            return firstOutput
                    .map(item -> new EvaluationOutputModel(item.id(), majorityScore))
                    .orElse(new EvaluationOutputModel(-1, ExecutionStatus.FAILED, 0));
        });
    }
}
