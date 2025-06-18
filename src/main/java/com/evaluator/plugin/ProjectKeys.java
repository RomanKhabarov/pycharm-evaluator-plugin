package com.evaluator.plugin;

import com.intellij.openapi.actionSystem.DataKey;

public class ProjectKeys {
    public static final DataKey<EvaluationsTableModel> EVALUATIONS_TABLE_MODEL = DataKey.create("evaluations.table.model");

    private ProjectKeys() {
    }
}