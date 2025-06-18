package com.evaluator.plugin.action;

import com.evaluator.plugin.Utils;
import com.evaluator.plugin.service.EvaluationService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StopAction extends DumbAwareAction {
    @NotNull
    private final EvaluationService evaluationService;
    private volatile boolean isEnabled = true;

    public StopAction(@NotNull Project project) {
        super("Stop", "Stops all evaluations", AllIcons.Actions.Cancel);
        this.evaluationService = Objects.requireNonNull(project.getService(EvaluationService.class));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(isEnabled);
        e.getPresentation().setVisible(evaluationService.isRunning());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            throw new IllegalStateException("Required data is not configured");
        }
        isEnabled = false;

        TaskRunnerKt.cancelExecutionWithBackgroundProgress(
            project,
            () -> {
                evaluationService.stopExecution();
                return null;
            },
            () -> {
                isEnabled = true;
                Utils.notifyActionsToolbarUpdate(project);
                return null;
            });
    }
}
