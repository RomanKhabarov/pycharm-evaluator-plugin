package com.evaluator.plugin;

import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.HorizontalLayout;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class TableViewPanel<T> extends JPanel {
    @NotNull
    private final TableView<T> table;

    public TableViewPanel(@NotNull TableView<T> table, @NotNull ActionToolbar actionToolbar) {
        super(new BorderLayout());
        this.table = table;

        JPanel toolbarPanel = new JPanel(new HorizontalLayout(10, HorizontalLayout.FILL));
        toolbarPanel.add(actionToolbar.getComponent(), HorizontalLayout.LEFT);

        JBScrollPane scrollPane = new JBScrollPane();
        scrollPane.setBorder(JBUI.Borders.empty(10));
        scrollPane.setViewportBorder(JBUI.Borders.empty());
        scrollPane.setViewportView(table);

        this.add(toolbarPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    protected @NotNull TableView<T> getTable() {
        return table;
    }
}
