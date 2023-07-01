package com.eldar.async.executor;

import com.eldar.async.AsyncProperties;
import com.eldar.async.decorator.TaskDecoratorResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class ExecutorFactory {

    private final AsyncProperties asyncProperties;
    private final TaskDecoratorResolver taskDecoratorResolver;

    public ThreadPoolTaskExecutor newTaskExecutor() {
        return newTaskExecutor(asyncProperties);
    }

    public ThreadPoolTaskExecutor newTaskExecutor(AsyncProperties asyncProperties) {
        AsyncProperties finalAsyncProperties = asyncProperties != null ? asyncProperties : this.asyncProperties;
        log.info("Creating a new task pool with following properties: {}", asyncProperties);
        ThreadPoolTaskExecutor executor = new TaskExecutorBuilder()
                .corePoolSize(finalAsyncProperties.getPool().getCoreSize())
                .maxPoolSize(finalAsyncProperties.getPool().getMaxCoreSize())
                .queueCapacity(finalAsyncProperties.getPool().getQueueCapacity())
                .keepAlive(finalAsyncProperties.getPool().getKeepAliveTimeInSec())
                .allowCoreThreadTimeOut(finalAsyncProperties.getPool().isAllowCoreThreadTimeout())
                .threadNamePrefix(finalAsyncProperties.getThreadNamePrefix())
                .taskDecorator(taskDecoratorResolver.getTaskDecorator())
                .build();

        executor.initialize();
        return executor;
    }

}
