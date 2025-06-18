package com.evaluator.plugin.action;

import com.evaluator.plugin.EvaluationViewItem;
import com.evaluator.plugin.EvaluationsTableModel;
import com.evaluator.plugin.ProjectKeys;
import com.evaluator.plugin.Utils;
import com.evaluator.core.model.EvaluationDataSet;
import com.evaluator.plugin.service.EvaluationService;
import com.evaluator.plugin.service.FileToObjectService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class RunAction extends DumbAwareAction {
    @NotNull
    private final EvaluationService evaluationService;
    private volatile boolean isEnabled = true;

    public RunAction(@NotNull Project project) {
        super("Run", "Runs parallel evaluation", AllIcons.Actions.Execute);
        this.evaluationService = Objects.requireNonNull(project.getService(EvaluationService.class));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(isEnabled);
        e.getPresentation().setVisible(!evaluationService.isRunning());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        EvaluationsTableModel tableModel = e.getData(ProjectKeys.EVALUATIONS_TABLE_MODEL);
        if (tableModel == null || project == null) {
            throw new IllegalStateException("Required data is not configured");
        }
        isEnabled = false;

        TaskRunnerKt.initiateExecutionWithBackgroundProgress(
            project,
            () -> {
                FileToObjectService loadDataService = project.getService(FileToObjectService.class);
                EvaluationDataSet dataset = loadDataService.loadDatasetFromProjectRoot(project, "dataset.json");
                String promptTemplate = loadDataService.loadPromptTemplateFromResource("evaluation_prompt.txt");
                List<EvaluationViewItem> items = generateViewItems(dataset);

                return new DataContext(dataset, promptTemplate, items);
            },
            (DataContext data) -> {
                tableModel.setItems(data.items());
                tableModel.fireTableDataChanged();
                return null;
            },
            (DataContext data) -> {
                if (data.dataset() != null) {
                    evaluationService.startExecution(data.dataset(), data.promptTemplate(), tableModel);
                }
                return null;
            },
            () -> {
                isEnabled = true;
                Utils.notifyActionsToolbarUpdate(project);
                return null;
            });
    }

    @NotNull
    private List<EvaluationViewItem> generateViewItems(@Nullable EvaluationDataSet dataset) {
        if (dataset == null) {
            return List.of();
        }
        return dataset.data().stream().map(dataItem -> new EvaluationViewItem(dataItem.input(), dataItem.referenceOutput(), "", "")).toList();
    }

    private record DataContext(@Nullable EvaluationDataSet dataset, @NotNull String promptTemplate, @NotNull List<EvaluationViewItem> items) {
    }
}
