package com.evaluator.core.executor;

import com.evaluator.core.ExecutionController;
import com.evaluator.core.model.ExecutionStatus;
import com.evaluator.core.model.ScriptInputModel;
import com.evaluator.core.model.ScriptOutputModel;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ScriptTaskExecutor implements TaskExecutor<ScriptInputModel, ScriptOutputModel> {
    private static final Logger LOG = Logger.getInstance(ExecutionController.class);
    private static final Duration EXECUTION_TIMEOUT = Duration.of(15, ChronoUnit.MINUTES);

    @NotNull
    private Process startProcess(@NotNull ScriptInputModel inputModel) {
        try {
            List<String> command = new ArrayList<>();
            command.add("python");
            command.add(inputModel.scriptPath());
            command.add(inputModel.dataItem().input());

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            return processBuilder.start();
        } catch (IOException e) {
            throw new UncheckedIOException("Error while starting external process", e);
        }
    }

    @NotNull
    private Future<String> readLinesAsync(@NotNull InputStream inputStream) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> outputFuture = executorService.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            } catch (IOException e) {
                LOG.warn("Output reader IOException", e);
                throw new UncheckedIOException("Error while reading process output", e);
            }
        });
        executorService.shutdown();
        return outputFuture;
    }

    @NotNull
    @Override
    public ScriptOutputModel performExecution(@NotNull ScriptInputModel inputModel) throws InterruptedException {
        Process process = startProcess(inputModel);
        LOG.info(String.format("%d Process started", inputModel.id()));

        Future<String> outputFuture = readLinesAsync(process.getInputStream());
        try {
            process.waitFor(EXECUTION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            int exitCode = process.exitValue();
            String output = outputFuture.get();
            LOG.info(String.format("%d Process finished. Code: %d, Output: %s", inputModel.id(), exitCode, output));
            if (exitCode == 0) {
                return new ScriptOutputModel(inputModel.id(), inputModel.dataItem(), output);
            }
            throw new TaskExecutionException("Process finished with non-zero status code: " + exitCode);

        } catch (InterruptedException e) {
            outputFuture.cancel(true);
            process.destroyForcibly();
            LOG.info(String.format("%d Process interrupted", inputModel.id()));
            throw e;

        } catch (ExecutionException e) {
            LOG.info(String.format("%d Output reader ExecutionException", inputModel.id()));
            throw new TaskExecutionException("Process output reader failed", e);
        }
    }

    @NotNull
    @Override
    public ScriptOutputModel createFailureModel(@NotNull ScriptInputModel inputModel, @NotNull ExecutionStatus status) {
        return new ScriptOutputModel(inputModel.id(), status, inputModel.dataItem(), "");
    }
}
