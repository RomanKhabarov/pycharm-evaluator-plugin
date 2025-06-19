package com.evaluator.plugin.service;

import com.evaluator.plugin.EvaluationsTableModel;
import com.evaluator.plugin.ResultsListener;
import com.evaluator.plugin.Utils;
import com.evaluator.core.ExecutionController;
import com.evaluator.core.listener.TaskExecutionListener;
import com.evaluator.core.model.EvaluationDataSet;
import com.evaluator.core.model.EvaluationOutputModel;
import com.evaluator.core.model.ExecutionStatus;
import com.evaluator.core.model.ScriptOutputModel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service(Service.Level.PROJECT)
public class EvaluationService implements Disposable {
    private static final Logger LOG = Logger.getInstance(EvaluationService.class);

    @NotNull
    private final Project project;
    @NotNull
    private final AtomicReference<ExecutionController> executionControllerAtomicReference = new AtomicReference<>(null);

    public EvaluationService(@NotNull Project project) {
        this.project = project;
    }

    public boolean isRunning() {
        return executionControllerAtomicReference.get() != null;
    }

    public void startExecution(
            @NotNull EvaluationDataSet dataset,
            @NotNull String promptTemplate,
            @NotNull EvaluationsTableModel tableModel) {
        ExecutionController controller = new ExecutionController(dataset, promptTemplate);
        if (!executionControllerAtomicReference.compareAndSet(null, controller)) {
            LOG.warn("Evaluation already in progress");
            return;
        }
        controller.addScriptResultsConsumer(new ResultsListener.ScriptResultsListener(tableModel));
        controller.addEvaluationResultsConsumer(new ResultsListener.EvaluationResultsListener(tableModel));
        controller.addScriptResultsConsumer(new ScriptsFailedCompletionListener(dataset.data().size(), this::onEvaluationExecutionsCompleted));
        controller.addEvaluationResultsConsumer(new EvaluationsCompletionListener(dataset.data().size(), this::onEvaluationExecutionsCompleted));
        controller.runEvaluations();
    }

    public void stopExecution() {
        ExecutionController controller = executionControllerAtomicReference.getAndSet(null);
        if (controller == null) {
            LOG.warn("Evaluation already stopped");
            return;
        }
        controller.close();
    }

    private void onEvaluationExecutionsCompleted() {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            stopExecution();
            Utils.notifyActionsToolbarUpdate(project);
        });
    }

    @Override
    public void dispose() {
        stopExecution();
    }

    @FunctionalInterface
    public interface AllTasksCompleteListener {
        void onAllComplete();
    }

    private static class EvaluationsCompletionListener implements TaskExecutionListener<EvaluationOutputModel> {
        private final int completionThreshold;

        @NotNull
        private final AllTasksCompleteListener allTasksCompletionListener;

        @NotNull
        private final AtomicInteger completionCounter;

        public EvaluationsCompletionListener(
                int completionThreshold, @NotNull AllTasksCompleteListener allTasksCompletionListener) {
            this.completionThreshold = completionThreshold;
            this.allTasksCompletionListener = allTasksCompletionListener;
            this.completionCounter = new AtomicInteger(0);
        }

        @Override
        public void executionFinished(@NotNull EvaluationOutputModel taskResult) {
            if (completionCounter.incrementAndGet() == completionThreshold) {
                allTasksCompletionListener.onAllComplete();
            }
        }
    }

    private static class ScriptsFailedCompletionListener implements TaskExecutionListener<ScriptOutputModel> {
        private final int completionThreshold;

        @NotNull
        private final AllTasksCompleteListener allTasksCompletionListener;

        @NotNull
        private final AtomicInteger completionCounter;

        public ScriptsFailedCompletionListener(
                int completionThreshold, @NotNull AllTasksCompleteListener allTasksCompletionListener) {
            this.completionThreshold = completionThreshold;
            this.allTasksCompletionListener = allTasksCompletionListener;
            this.completionCounter = new AtomicInteger(0);
        }

        @Override
        public void executionFinished(@NotNull ScriptOutputModel taskResult) {
            if (taskResult.status() == ExecutionStatus.FAILED
                    && completionCounter.incrementAndGet() == completionThreshold) {
                allTasksCompletionListener.onAllComplete();
            }
        }
    }
}
