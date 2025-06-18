package com.evaluator.plugin;

import com.evaluator.plugin.listener.ToolbarListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class Utils {
    private Utils() {
    }

    public static void notifyActionsToolbarUpdate(@NotNull Project project) {
        project.getMessageBus()
                .syncPublisher(ToolbarListener.TOPIC)
                .updateActions();
    }
}
