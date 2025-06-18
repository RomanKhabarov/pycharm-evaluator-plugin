package com.evaluator.plugin;

import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class EvaluationsTableModel extends ListTableModel<EvaluationViewItem> {
    public EvaluationsTableModel() {
        super(createTableModel());
    }

    @NotNull
    private static ColumnInfo[] createTableModel() {
        ColumnInfo<EvaluationViewItem, String> inputColumn = createStringColumn("Input", EvaluationViewItem::getInput);
        ColumnInfo<EvaluationViewItem, String> referenceColumn = createStringColumn("Reference Output", EvaluationViewItem::getReferenceOutput);
        ColumnInfo<EvaluationViewItem, String> modelColumn = createStringColumn("Model Output", EvaluationViewItem::getModelOutput);
        ColumnInfo<EvaluationViewItem, String> scoreColumn = createStringColumn("Score", EvaluationViewItem::getScore);
        return new ColumnInfo[] { inputColumn, referenceColumn, modelColumn, scoreColumn };
    }

    @NotNull
    private static <T> ColumnInfo<T, String> createStringColumn(
            @NotNull String columnName, @NotNull Function<T, String> valueConverter) {
        return new ColumnInfo<>(columnName) {
            @Override
            public String valueOf(T item) {
                return valueConverter.apply(item);
            }
        };
    }
}
