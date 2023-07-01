package com.eldar.async.executor;


import com.eldar.async.AsyncProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExecutorServiceManagerTest {

    private static final String DEFAULT_EXECUTOR = "default";

    private ExecutorServiceManager executorServiceManager;

    @Mock
    private ExecutorFactory executorFactory;

    @Mock
    private ThreadPoolTaskExecutor defaultExecutor;

    @Mock
    private ThreadPoolTaskExecutor newExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(executorFactory.newTaskExecutor()).thenReturn(defaultExecutor);
        executorServiceManager = new ExecutorServiceManager(executorFactory);
        executorServiceManager.init();
    }

    @Test
    @DisplayName("Test destroy method should shutdown default executor")
    void testDestroy() {
        executorServiceManager.destroy();

        verify(defaultExecutor).shutdown();
    }

    @Test
    @DisplayName("Test getDefaultExecutor should return default executor")
    void testGetDefaultExecutor_ExistingExecutor() {
        Executor executor = executorServiceManager.getDefaultExecutor();

        assertEquals(defaultExecutor, executor);
    }

    @Test
    @DisplayName("Test getDefaultExecutor should return default executor after shutdown")
    void testGetDefaultExecutor_recoveryAfterShutDown() {
        executorServiceManager = new ExecutorServiceManager(executorFactory);
        executorServiceManager.init();
        executorServiceManager.shutdown();

        Executor executor = executorServiceManager.getDefaultExecutor();

        verify(defaultExecutor).shutdown();
        assertNotNull(executor);
    }

    @Test
    @DisplayName("Test to get an executor by name should return the executor")
    void testGetExecutor_ExistingExecutor() {
        Executor executor = executorServiceManager.getExecutor(DEFAULT_EXECUTOR);

        assertEquals(defaultExecutor, executor);
    }

    @Test
    @DisplayName("Test to get an executor by name should return null as executor doesn't exist")
    void testGetExecutor_NonExistingExecutor() {
        Executor executor = executorServiceManager.getExecutor("nonexisting");

        assertNull(executor);
    }

    @Test
    @DisplayName("Attempt to create executor with empty name should return default executor")
    void testNewExecutor_EmptyName() {
        Executor executor = executorServiceManager.newExecutor("");

        assertEquals(defaultExecutor, executor);
    }

    @Test
    @DisplayName("Attempt to create executor null name should return default executor")
    void testNewExecutor_DefaultName() {
        Executor executor = executorServiceManager.newExecutor(DEFAULT_EXECUTOR);

        assertEquals(defaultExecutor, executor);
    }

    @Test
    @DisplayName("Attempt to create executor with existing name should return existing executor")
    void testNewExecutor_ExistingExecutor() {
        when(executorFactory.newTaskExecutor(any())).thenReturn(newExecutor);
        Executor expectedExecutor = executorServiceManager.newExecutor("newExecutor");
        when(executorFactory.newTaskExecutor()).thenReturn(new ThreadPoolTaskExecutor());

        Executor actualExecutor = executorServiceManager.newExecutor("newExecutor");

        assertEquals(actualExecutor, expectedExecutor);
    }

    @Test
    @DisplayName("Creating new executor should return new executor")
    void testNewExecutor_NewExecutor() {
        when(executorFactory.newTaskExecutor(any())).thenReturn(newExecutor);
        Executor expectedExecutor = executorServiceManager.newExecutor("newExecutor");
        when(executorFactory.newTaskExecutor()).thenReturn(new ThreadPoolTaskExecutor());

        Executor actualExecutor = executorServiceManager.getExecutor("newExecutor1");

        assertNotEquals(expectedExecutor, actualExecutor);
    }

    @Test
    @DisplayName("Creating new executor with async properties should return new executor with those configurations")
    void testNewExecutor_NewExecutorWithAsyncProperties() {
        AsyncProperties asyncProperties = new AsyncProperties();
        ThreadPoolTaskExecutor newExecutor = new ThreadPoolTaskExecutor();
        newExecutor.setCorePoolSize(3);
        newExecutor.setThreadNamePrefix("test-async-properties");
        asyncProperties.getPool().setCoreSize(3);
        asyncProperties.setThreadNamePrefix("test-async-properties");
        doReturn(newExecutor).when(executorFactory).newTaskExecutor(asyncProperties);

        ThreadPoolTaskExecutor actualExecutor = (ThreadPoolTaskExecutor) executorServiceManager.newExecutor("newExecutor1", asyncProperties);

        assertEquals(newExecutor, actualExecutor);
        assertEquals(newExecutor.getCorePoolSize(), actualExecutor.getCorePoolSize());
        assertEquals(newExecutor.getThreadNamePrefix(), actualExecutor.getThreadNamePrefix());
    }

    @Test
    @DisplayName("Test initExecutor should return default executor")
    void testInitExecutor() {
        Executor executor = executorServiceManager.initExecutor();

        assertEquals(defaultExecutor, executor);
    }

    @Test
    @DisplayName("Test shutdown should shutdown all executors")
    void testShutdown() {
        executorServiceManager = new ExecutorServiceManager(executorFactory);
        executorServiceManager.init();
        when(executorFactory.newTaskExecutor(any())).thenReturn(newExecutor);
        Executor executor1 =  executorServiceManager.newExecutor("executor1");
        Executor executor2 = executorServiceManager.newExecutor("executor2");

        executorServiceManager.shutdown();

        verify((ThreadPoolTaskExecutor)executor1, atLeastOnce()).shutdown();
        verify((ThreadPoolTaskExecutor)executor2, atLeastOnce()).shutdown();
        assertNull(executorServiceManager.getExecutor("executor1"));
        assertNull(executorServiceManager.getExecutor("executor2"));
    }
}
