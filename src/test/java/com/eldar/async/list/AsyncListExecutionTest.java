package com.eldar.async.list;

import com.eldar.async.AsyncProperties;
import com.eldar.async.decorator.ContextAwareTaskDecorator;
import com.eldar.async.decorator.TaskDecoratorResolver;
import com.eldar.async.executor.ExecutorFactory;
import com.eldar.async.executor.ExecutorServiceManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AsyncListExecutionTest {
    private static final int PARTITION_SIZE = 3;

    private static AsyncListExecutor asyncExecution;

    @BeforeAll
    static void beforeAll() {
        AsyncProperties asyncProperties = new AsyncProperties();
        TaskDecoratorResolver taskDecoratorResolver = Mockito.mock(TaskDecoratorResolver.class);
        doReturn(new ContextAwareTaskDecorator()).when(taskDecoratorResolver).getTaskDecorator();
        ExecutorFactory executorFactory = new ExecutorFactory(asyncProperties, taskDecoratorResolver);
        ExecutorServiceManager executorServiceManager = new ExecutorServiceManager(executorFactory);
        asyncExecution = new AsyncListExecution(executorServiceManager);
    }

    @Test
    @DisplayName("Testing empty list value should not invoke the function")
    void testExecuteOnListWithPartition_EmptyList() {
        List<Object> items = Collections.emptyList();
        Consumer<List<Object>> function = mock(Consumer.class);
        doAnswer(invocation -> {
            fail("Function should not be called for an empty list");
            return null;
        }).when(function).accept(anyList());

        asyncExecution.withPartition(items, function, PARTITION_SIZE);

        verify(function, never()).accept(anyList());

    }

    @Test
    @DisplayName("Testing partition size less than one should not invoke the function and should throw exception")
    void testExecuteOnListWithPartition_PartitionSizeLessThanOne() {
        List<Object> items = List.of("");
        Consumer<List<Object>> function = mock(Consumer.class);
        doAnswer(invocation -> {
            fail("Function should not be called for an invalid partition size");
            return null;
        }).when(function).accept(anyList());

        assertThrows(IllegalArgumentException.class,
                () -> asyncExecution.withPartition(items, function, 0));

        verify(function, never()).accept(anyList());
    }

    @Test
    @DisplayName("Testing withPartition with the default executor should invoke the function and success")
    void testExecuteOnListWithPartition_Success() throws InterruptedException {
        List<Object> items = new ArrayList<>();
        items.add(1);
        items.add(2);
        List<Integer> testItems = new CopyOnWriteArrayList<>();
        Consumer<List<Object>> function = mock(Consumer.class);
        doAnswer(invocation -> {
            List<Object> partitions = invocation.getArgument(0);
            testItems.addAll(partitions.stream().map(item -> (Integer) item).toList());
            return null;
        }).when(function).accept(anyList());

        asyncExecution.withPartition(items, function, PARTITION_SIZE);

        // Allow time for tasks to complete
        Thread.sleep(1000);

        // Verify that the function was called with one partition
        items.forEach(item -> verify(function).accept(items));
        assertEquals(2, testItems.size());
        testItems.sort(Integer::compareTo);
        assertEquals(1, testItems.get(0));
        assertEquals(2, testItems.get(1));

    }

    @Test
    @DisplayName("Testing withPartition with the custom executor should invoke the function and success")
    void testExecuteOnListWithPartition_CustomExecutorService() {
        List<Object> items = new ArrayList<>();
        items.add(1);
        items.add(2);
        List<Integer> testItems = new CopyOnWriteArrayList<>();
        Consumer<List<Object>> function = mock(Consumer.class);
        doAnswer(invocation -> {
            List<Object> partitions = invocation.getArgument(0);
            testItems.add((Integer) partitions.get(0));
            return null;
        }).when(function).accept(anyList());
        ExecutorService executor = Executors.newFixedThreadPool(2);

        asyncExecution.withPartition(items, function, 1, executor);

        // Verify that the function was called
        verify(function).accept(Collections.singletonList(items.get(0)));
        assertEquals(2, testItems.size());
        testItems.sort(Integer::compareTo);
        assertEquals(1, testItems.get(0));
        assertEquals(2, testItems.get(1));
        // Shutdown the executor
        executor.shutdown();
    }


    @Test
    @DisplayName(("Testing withPartition with function that returns a value - with custom executor"))
    void testOnListWithPartition_CustomExecutor() {
        // Prepare input data
        List<Integer> items = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> expectedResults = Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
        // Create a function to multiply each item by 2
        Function<List<Integer>, List<Integer>> function = item -> item.stream().map(i -> i * 2).toList();
        // Create an executor with a thread pool
        Executor executor = Executors.newFixedThreadPool(5);

        // Execute the method and get the result
        List<Integer> result = asyncExecution.withPartition(items, function, PARTITION_SIZE, executor,
                item -> item.stream().flatMap(Collection::stream).toList());

        // Assert the result
        assertEquals(expectedResults, result);
    }

    @Test
    @DisplayName(("Testing withPartition with function that returns a value - with default executor"))
    void testOnListWithPartition_DefaultExecutor() {
        // Prepare input data
        List<Integer> items = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> expectedResults = Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
        // Create a function to multiply each item by 2
        Function<List<Integer>, List<Integer>> function = item -> item.stream().map(i -> i * 2).toList();
        // Create an executor with a thread pool

        // Execute the method and get the result
        List<Integer> result = asyncExecution.withPartition(items, function, PARTITION_SIZE,
                item -> item.stream().flatMap(Collection::stream).toList());

        // Assert the result
        assertEquals(expectedResults, result);
    }

    @Test
    @DisplayName(("Testing withPartition with function that returns a value - with null executor"))
    void testOnListWithPartition_NullExecutor() {
        // Prepare input data
        List<Integer> items = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // Create a function (not used in this test)
        Function<List<Integer>, Integer> function = item -> item.get(0);


        // Execute the method and get the result
        assertThrows(IllegalArgumentException.class,
                () -> asyncExecution.withPartition(items, function, PARTITION_SIZE, null, item -> item.get(0)));
    }

    @Test
    @DisplayName(("Testing withPartition with a function that throws an exception - should only log the exception"))
    void testOnListWithPartition_MethodThrowsException() {
        // Prepare input data
        List<Integer> items = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        // Create a function to divide items by 0 (should throw an exception)
        Function<List<Integer>, List<Integer>> function = item -> item.stream().map(i -> i / 0).toList();

        // Execute the method and get the result (should not throw an exception)
        List<Integer> result = asyncExecution.withPartition(items, function, PARTITION_SIZE,
                item -> item.stream().flatMap(Collection::stream).toList());

        // Assert the result
        assertEquals(Collections.emptyList(), result);
    }


}
