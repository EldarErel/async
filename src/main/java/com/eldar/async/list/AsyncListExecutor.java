package com.eldar.async.list;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public interface AsyncListExecutor {
    /**
     * This method is used to execute a function on a list of items asynchronous, by splitting the list into partitions
     * and executing the function on each partition using a thread pool.
     *
     * @param items         - the list of items to execute the function on
     * @param function      - the function(Consumer) to execute on the list
     * @param partitionSize - the size of the partition to split the list into
     * @param <T>           - the type of the items in the list
     */
    <T> void withPartition(List<T> items, Consumer<List<T>> function, int partitionSize);

    /**
     * This method is used to execute a function on a list of items asynchronous, by splitting the list into partitions
     * and executing the function on each partition using a thread pool.
     *
     * @param items         - the list of items to execute the function on
     * @param function      - the function(Consumer) to execute on the list
     * @param partitionSize - the size of the partition to split the list into
     * @param executor      - the executor to use for the execution
     * @param <T>           - the type of the items in the list
     */
    <T> void withPartition(List<T> items, Consumer<List<T>> function, int partitionSize, Executor executor);

    /**
     * This method is used to execute a function on a list of items asynchronous, by splitting the list into partitions
     * and executing the function on each partition using a custom thread pool.
     *
     * @param items           - the list of items to execute the function on
     * @param function        - the function to execute on the list
     * @param partitionSize   - the size of the partition to split the list into
     * @param executor        - the executor to use for the execution
     * @param combineFunction - the function to combine the results of the function on each partition
     * @param <T>             - the type of the items in the list
     * @param <R>             - the return type of the function
     * @return the result of the function
     */
    <T, R> R withPartition(List<T> items, Function<List<T>, R> function, int partitionSize, Executor executor,
                           Function<List<R>, R> combineFunction);


    /**
     * This method is used to execute a function on a list of items asynchronous, by splitting the list into partitions
     * and executing the function on each partition using the default thread pool.
     *
     * @param items           - the list of items to execute the function on
     * @param function        - the function to execute on the list
     * @param partitionSize   - the size of the partition to split the list into
     * @param combineFunction - the function to combine the results of the function on each partition
     * @param <T>             - the type of the items in the list
     * @param <R>             - the return type of the function
     * @return the result of the function
     */
    <T, R> R withPartition(List<T> items, Function<List<T>, R> function, int partitionSize, Function<List<R>
            , R> combineFunction);

    /**
     * This method is used to execute a function on a list of items asynchronous, by splitting the list into partitions
     * @param items - the list of items to execute the function on
     * @param function - the function to execute on the list
     * @param partitionSize - the size of the partition to split the list into
     * @param isToThrow - if true, the exception will be thrown as Cause under 'ExecutionException', otherwise it will be logged
     * @param <T> - the type of the items in the list
     */
    <T> void withPartition(List<T> items, Consumer<List<T>> function, int partitionSize, boolean isToThrow);


    /**
     * This method is used to execute a function on a list of items asynchronous, by splitting the list into partitions
     * With the default partition size
     * @param items - the list of items to execute the function on
     * @param function - the function to execute on the list
     * @param isToThrow - if true, the exception will be thrown as Cause under 'ExecutionException', otherwise it will be logged
     * @param <T> - the type of the items in the list
     */
    <T> void withPartition(List<T> items, Consumer<List<T>> function, boolean isToThrow);
}
