package com.evaluator.plugin.listener;

import com.intellij.util.messages.Topic;

public interface ToolbarListener {
    Topic<ToolbarListener> TOPIC = Topic.create("Toolbar listener", ToolbarListener.class);

    void updateActions();
}