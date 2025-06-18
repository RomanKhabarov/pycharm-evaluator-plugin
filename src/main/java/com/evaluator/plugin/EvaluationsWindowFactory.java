package com.evaluator.plugin;

import com.evaluator.plugin.action.RunAction;
import com.evaluator.plugin.action.StopAction;
import com.evaluator.plugin.listener.ToolbarListener;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.NotNull;

public class EvaluationsWindowFactory implements ToolWindowFactory, DumbAware {

    @NotNull
    private ActionToolbar createActionToolbar(@NotNull Project project) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new RunAction(project));
        actionGroup.add(new StopAction(project));
        return ActionManager.getInstance().createActionToolbar(getClass().getName(), actionGroup, true);
    }

    @NotNull
    TableView<EvaluationViewItem> createTableView() {
        TableView<EvaluationViewItem> table = new TableView<>();
        table.setModel(new EvaluationsTableModel());
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        return table;
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ActionToolbar actionToolbar = createActionToolbar(project);
        TableView<EvaluationViewItem> table = createTableView();
        EvaluationsTableViewPanel tableViewPanel = new EvaluationsTableViewPanel(table, actionToolbar);

        actionToolbar.setTargetComponent(tableViewPanel);
        project.getMessageBus()
                .connect(toolWindow.getContentManager())
                .subscribe(ToolbarListener.TOPIC, (ToolbarListener)
                        () -> ApplicationManager.getApplication().invokeLater(actionToolbar::updateActionsAsync));

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(tableViewPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        ToolWindowFactory.super.init(toolWindow);
        toolWindow.setIcon(AllIcons.Ide.LikeDimmed);
    }
}
