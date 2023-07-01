package com.eldar.async;

import com.eldar.async.executor.ExecutorServiceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

/**
 * Used to Preserving Context information in Asynchronous Methods
 * By default, Spring uses a SimpleAsyncTaskExecutor to actually run annotated @Async methods asynchronously
 * This now becomes the default executor to run methods/classes annotated with @Async
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private final ExecutorServiceManager executorServiceManager;

    /**
     * <p>A default task executor.</p>
     * <p>The {@link Executor} instance to be used when processing async method invocations.</p>
     */
    @Override
    public Executor getAsyncExecutor() {
        return executorServiceManager.getDefaultExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

}