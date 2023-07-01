package com.eldar.async.list;


import com.eldar.async.executor.ExecutorServiceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAsyncExecutor {
    public static final int DEFAULT_LIST_PARTITION_SIZE = 100; // specify the default partition size

    protected static final int TASK_TIMEOUT = 10; // specify the task timeout in seconds

    protected final ExecutorServiceManager executorServiceManager;

    // to encapsulate the functions (consumer, function) to one interface
    protected interface GenericOperation<T, R> {
        R accept(T t);
    }
}