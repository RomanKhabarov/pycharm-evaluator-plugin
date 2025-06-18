package com.evaluator.plugin;

import com.evaluator.core.model.EvaluationOutputModel;
import com.evaluator.core.model.ExecutionStatus;
import com.evaluator.core.model.IdentifiableModel;
import com.evaluator.core.model.ScriptOutputModel;
import com.evaluator.core.listener.TaskExecutionListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.function.Consumer;

public abstract class ResultsListener<O> implements TaskExecutionListener<O> {
    @NotNull
    private final EvaluationsTableModel tableModel;

    protected ResultsListener(@NotNull EvaluationsTableModel tableModel) {
        this.tableModel = tableModel;
    }

    protected void updateTableModel(int rowIndex, int columnIndex, @NotNull Consumer<EvaluationViewItem> resultConsumer) {
        SwingUtilities.invokeLater(() -> {
            if (rowIndex < 0 || rowIndex >= tableModel.getRowCount()) {
                throw new IndexOutOfBoundsException("Data model row index out of range");
            }
            EvaluationViewItem item = tableModel.getItem(rowIndex);
            resultConsumer.accept(item);
            tableModel.fireTableCellUpdated(rowIndex, columnIndex);
        });
    }

    public static final class ScriptResultsListener extends ResultsListener<ScriptOutputModel> {
        public ScriptResultsListener(@NotNull EvaluationsTableModel tableModel) {
            super(tableModel);
        }

        @Override
        public void executionStarted(@NotNull IdentifiableModel taskInto) {
            updateTableModel(taskInto.id(), 2, item -> item.setModelOutput("Evaluating..."));
        }

        @Override
        public void executionFinished(@NotNull ScriptOutputModel taskResult) {
            String value = taskResult.status() == ExecutionStatus.SUCCESS
                    ? taskResult.scriptOutput()
                    : taskResult.status().name();
            updateTableModel(taskResult.id(), 2, item -> item.setModelOutput(value));
        }
    }

    public static final class EvaluationResultsListener extends ResultsListener<EvaluationOutputModel> {
        public EvaluationResultsListener(@NotNull EvaluationsTableModel tableModel) {
            super(tableModel);
        }

        @Override
        public void executionStarted(@NotNull IdentifiableModel taskInto) {
            updateTableModel(taskInto.id(), 3, item -> item.setScore("Evaluating..."));
        }

        @Override
        public void executionFinished(@NotNull EvaluationOutputModel taskResult) {
            String value = taskResult.status() == ExecutionStatus.SUCCESS
                    ? String.valueOf(taskResult.score())
                    : taskResult.status().name();
            updateTableModel(taskResult.id(), 3, item -> item.setScore(value));
        }
    }
}
