package com.evaluator.core;

import com.evaluator.core.executor.TaskExecutionException;
import com.evaluator.core.executor.TaskExecutor;
import com.evaluator.core.listener.TaskExecutionListener;
import com.evaluator.core.model.ExecutionStatus;
import com.evaluator.core.model.IdentifiableModel;
import com.evaluator.core.model.OutputModel;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public class TaskExecutionService<I extends IdentifiableModel, O extends OutputModel> implements AutoCloseable {
    @NotNull
    private final Logger logger;
    @NotNull
    private final TaskExecutor<I, O> executor;
    @NotNull
    private final Duration awaitTerminationLimit;
    @NotNull
    private final ExecutorService executorService;
    @NotNull
    private final List<TaskExecutionListener<O>> executionListeners;

    public TaskExecutionService(int executionThreadCount, @NotNull TaskExecutor<I, O> executor, @NotNull Duration awaitTerminationLimit) {
        this.logger = Logger.getInstance(getClass().getName());
        this.executor = executor;
        this.awaitTerminationLimit = awaitTerminationLimit;
        this.executorService = Executors.newFixedThreadPool(executionThreadCount);
        this.executionListeners = new CopyOnWriteArrayList<>();
    }

    private void notifyListeners(Consumer<TaskExecutionListener<O>> consumer) {
        executionListeners.forEach(consumer);
    }

    private CompletableFuture<O> createExecutionFuture(@NotNull I inputModel) {
        return CompletableFuture.supplyAsync(() -> {
            notifyListeners(listener -> listener.executionStarted(inputModel));
            try {
                return executor.performExecution(inputModel);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info(String.format("%d Execution interrupted", inputModel.id()));
                throw new CancellationException("Interrupted");
            }
        }, executorService).exceptionally(e -> executor.createFailureModel(inputModel, ExecutionStatus.FAILED));
    }

    public void execute(@NotNull I inputModel) {
        CompletableFuture<O> future = createExecutionFuture(inputModel);
        future.thenAccept(result -> notifyListeners(listener -> listener.executionFinished(result)));
    }

    public void executeAndReduce(@NotNull I inputModel, int numberOfExecutions, @NotNull Function<List<O>, O> reduceFunction) {
        List<CompletableFuture<O>> futuresBatch = IntStream.rangeClosed(1, numberOfExecutions)
                .mapToObj(i -> createExecutionFuture(inputModel))
                .toList();

        CompletableFuture.allOf(futuresBatch.toArray(new CompletableFuture[0])).thenAccept(v -> {
            boolean isAnyCancelled = futuresBatch.stream().anyMatch(CompletableFuture::isCancelled);
            if (isAnyCancelled) {
                return;
            }
            List<O> results = futuresBatch.stream().map(CompletableFuture::join).toList();
            Optional<O> firstFailedResult = results.stream()
                    .filter(r -> r.status() != ExecutionStatus.SUCCESS)
                    .findFirst();
            O result = firstFailedResult.orElseGet(() -> reduceFunction.apply(results));
            notifyListeners(listener -> listener.executionFinished(result));
        });
    }

    public void addExecutionListener(@NotNull TaskExecutionListener<O> consumer) {
        executionListeners.add(consumer);
    }

    @Override
    public void close() {
        executionListeners.clear();
        executorService.shutdownNow();
        try {
            boolean isTerminated = executorService.awaitTermination(awaitTerminationLimit.toMillis(), TimeUnit.MILLISECONDS);
            if (!isTerminated) {
                logger.warn("AwaitTermination exceeded limit");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TaskExecutionException("Termination interrupted", e);
        }
    }
}
