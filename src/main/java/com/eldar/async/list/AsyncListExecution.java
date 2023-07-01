package com.eldar.async.list;


import com.eldar.async.executor.ExecutorServiceManager;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;


@Slf4j
@Service
public class AsyncListExecution extends AbstractAsyncExecutor implements AsyncListExecutor {

    public AsyncListExecution(ExecutorServiceManager executorServiceManager) {
        super(executorServiceManager);
    }

    @Override
    public <T> void withPartition(List<T> items, Consumer<List<T>> function, int partitionSize) {
        this.withPartition(items, function, partitionSize, executorServiceManager.getDefaultExecutor());
    }

    @Override
    public <T> void withPartition(List<T> items, Consumer<List<T>> function, boolean isToThrow) {
        this.withPartition(items, function, DEFAULT_LIST_PARTITION_SIZE, executorServiceManager.getDefaultExecutor(), isToThrow);
    }

    @Override
    public <T> void withPartition(List<T> items, Consumer<List<T>> function, int partitionSize, boolean isToThrow) {
        this.withPartition(items, function, partitionSize, executorServiceManager.getDefaultExecutor(), isToThrow);
    }

    @Override
    public <T, R> R withPartition(List<T> items, Function<List<T>, R> function, int partitionSize,
                                  Function<List<R>, R> combineFunction) {
        return this.withPartition(items, function, partitionSize, executorServiceManager.getDefaultExecutor(),
                combineFunction);
    }

    @Override
    public <T> void withPartition(List<T> items, Consumer<List<T>> function, int partitionSize,
                                  Executor executor) {
        GenericOperation<List<T>, Void> genericOperation = t -> {
            function.accept(t);
            return null;
        };

        withPartition(items, genericOperation, partitionSize, executor, false);
    }

    private <T> void withPartition(List<T> items, Consumer<List<T>> function, int partitionSize,
                                  Executor executor, boolean isToThrow) {
        GenericOperation<List<T>, Void> genericOperation = t -> {
            function.accept(t);
            return null;
        };

        withPartition(items, genericOperation, partitionSize, executor, isToThrow);
    }


    @Override
    public <T, R> R withPartition(List<T> items, Function<List<T>, R> function, int partitionSize,
                                  Executor executor, Function<List<R>, R> combineFunction) {
        GenericOperation<List<T>, R> genericOperation = function::apply;

        List<R> results = withPartition(items, genericOperation, partitionSize, executor, false);
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        // null values can be returned from the function when error occurs
        results = results.stream().filter(Objects::nonNull).toList();
        return combineFunction.apply(results);
    }

    private <T> boolean isValidaParameters(List<T> items, int partitionSize, Executor executor) {
        if (CollectionUtils.isEmpty(items)) {
            log.warn("Items list is empty or null, terminating execution.");
            return false;
        }
        if (partitionSize <= 0) {
            throw new IllegalArgumentException("Partition size must be greater than 0");
        }
        if (executor == null) {
            throw new IllegalArgumentException("Executor must not be null");
        }
        return true;
    }

    private <T, R> List<R> withPartition(List<T> items, GenericOperation<List<T>, R> function, int partitionSize,
                                         Executor executor, boolean isToThrowException) {
        if (!isValidaParameters(items, partitionSize, executor)) {
            return Collections.emptyList();
        }
        List<List<T>> partitions = Lists.partition(items, partitionSize);
        List<CompletableFuture<R>> futures = new ArrayList<>();
        for (int i = 0; i < partitions.size(); i++) {
            int partitionNumber = i;
            List<T> currentPartition = partitions.get(partitionNumber);
            futures.add(CompletableFuture.supplyAsync(() -> exceptionHandlingWrapper(currentPartition, function, partitionNumber,isToThrowException), executor));
        }

        CompletableFuture<List<R>> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApplyAsync(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList(), executor);
        try {
            return allFutures.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted state...
            log.warn("Thread was interrupted: " + e.getMessage());
            if (isToThrowException) {
                throw new RuntimeException(e);
            }
        } catch (TimeoutException e) {
            log.warn("Timeout: Not all tasks completed within the specified timeout of " + TASK_TIMEOUT + " seconds");
            if (isToThrowException) {
                throw new RuntimeException(e);
            }
        } catch (ExecutionException e) {
            log.warn("Execution exception: " + e.getMessage());
            if (isToThrowException) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            log.warn("Exception: " + e.getMessage());
            if (isToThrowException) {
                throw e;
            }
        }
        return Collections.emptyList();
    }

    // this method is used to handle exceptions in the function
    // the method returns null in case of an exception
    // currently the exception is only logged, but it can be handled differently
    private <T,R> R exceptionHandlingWrapper(T parameters , GenericOperation<T, R> operation, int partitionNumber,boolean isToThrowException) {
        try {
            return operation.accept(parameters);
        } catch (Exception e) {
            log.warn("Exception occurred while executing operation on partition number {} with parameters {}: {}",
                    partitionNumber, parameters, e.getMessage());
            if(isToThrowException){
                throw e;
            }
        }
        return null;
    }

}