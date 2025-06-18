package com.evaluator.plugin;

import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.actionSystem.UiDataProvider;
import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.NotNull;

public final class EvaluationsTableViewPanel extends TableViewPanel<EvaluationViewItem> implements UiDataProvider {

    public EvaluationsTableViewPanel(@NotNull TableView<EvaluationViewItem> table, @NotNull ActionToolbar actionToolbar) {
        super(table, actionToolbar);
    }

    @Override
    public void uiDataSnapshot(@NotNull DataSink dataSink) {
        dataSink.set(ProjectKeys.EVALUATIONS_TABLE_MODEL, (EvaluationsTableModel)getTable().getModel());
    }
}
