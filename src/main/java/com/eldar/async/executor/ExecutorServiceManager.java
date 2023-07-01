package com.eldar.async.executor;

import com.eldar.async.AsyncProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
@DependsOn("executorFactory")
public class ExecutorServiceManager {

    public static final String DEFAULT_EXECUTOR = "default";
    private final ExecutorFactory executorFactory;
    private final ConcurrentHashMap<String, ThreadPoolTaskExecutor> executorMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        executorMap.put(DEFAULT_EXECUTOR, executorFactory.newTaskExecutor());
    }

    @PreDestroy
    public void destroy() {
        shutdown();
    }

    public Executor getDefaultExecutor() {
        Executor executor = executorMap.get(DEFAULT_EXECUTOR);
        if (executor == null) {
            executor = initExecutor();
        }
        return executor;
    }

    public Executor getExecutor(String name) {
        return executorMap.get(name);
    }

    /**
     *  Create a new executor with the given name
     * @param name - executor name
     */
    public Executor newExecutor(String name) {
        // null properties will use default properties
       return newExecutor(name, null);
    }

    public Executor newExecutor(String name, AsyncProperties asyncProperties) {
        if (!StringUtils.hasText(name) || DEFAULT_EXECUTOR.equals(name)) {
            log.warn("Executor name is empty or default, returning default executor");
            return executorMap.get(DEFAULT_EXECUTOR);
        }
        if (executorMap.containsKey(name)) {
            log.warn("Executor with name {} already exists, returning existing executor", name);
            return executorMap.get(name);
        }
        ThreadPoolTaskExecutor executor = executorFactory.newTaskExecutor(asyncProperties);
        ThreadPoolTaskExecutor oldExecutor = executorMap.put(name, executor);
        if (oldExecutor != null) { // should never happen
            log.error("Shutting down old executor as it was overridden");
            oldExecutor.shutdown();
        }
        return executor;
    }

    // init default executor
    public Executor initExecutor() {
        return executorMap.computeIfAbsent(DEFAULT_EXECUTOR, key -> {
            log.info("Initializing default executor");
            ThreadPoolTaskExecutor executor = executorFactory.newTaskExecutor();
            return executor;
        });
    }

    public void shutdown() {
        executorMap.values().forEach(ThreadPoolTaskExecutor::shutdown);
        executorMap.clear();
    }
}
