package com.evaluator.plugin;

import org.jetbrains.annotations.NotNull;

public class EvaluationViewItem {
    @NotNull
    private String input;
    @NotNull
    private String referenceOutput;
    @NotNull
    private String modelOutput;
    @NotNull
    private String score;

    public EvaluationViewItem(@NotNull String input, @NotNull String referenceOutput, @NotNull String modelOutput, @NotNull String score) {
        this.input = input;
        this.referenceOutput = referenceOutput;
        this.modelOutput = modelOutput;
        this.score = score;
    }

    @NotNull
    public String getInput() {
        return input;
    }

    public void setInput(@NotNull String input) {
        this.input = input;
    }

    @NotNull
    public String getReferenceOutput() {
        return referenceOutput;
    }

    public void setReferenceOutput(@NotNull String referenceOutput) {
        this.referenceOutput = referenceOutput;
    }

    @NotNull
    public String getModelOutput() {
        return modelOutput;
    }

    public void setModelOutput(@NotNull String modelOutput) {
        this.modelOutput = modelOutput;
    }

    @NotNull
    public String getScore() {
        return score;
    }

    public void setScore(@NotNull String score) {
        this.score = score;
    }
}
