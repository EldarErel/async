# AsyncListExecutor
This document provides a brief description of the AsyncListExecutor interface, which offers methods for asynchronous execution of functions on lists of items.

## Overview
AsyncListExecutor is an interface designed to perform operations on lists asynchronously. It provides methods to process lists by dividing them into smaller partitions and executing a provided function on each partition concurrently. This can significantly speed up the processing time for large lists, especially when the function to apply is computationally expensive.

## Methods
The AsyncListExecutor interface provides several methods for different use cases:

`withPartition (List<T> items, Consumer<List<T>> function, int partitionSize)`: This method executes a Consumer function on a list of items asynchronously. The list is divided into partitions of a specified size, and the function is executed on each partition using a thread pool.

`withPartition (List<T> items, Consumer<List<T>> function, int partitionSize, Executor executor)`: Similar to the first method, but it allows specifying a custom Executor for the execution.

`withPartition (List<T> items, Function<List<T>, R> function, int partitionSize, Executor executor, Function<List<R>, R> combineFunction)`: This method is used to execute a Function on a list of items asynchronously. The list is divided into partitions of a specified size, and the function is executed on each partition using a custom Executor. The results of the function on each partition are then combined using the combineFunction.

`withPartition (List<T> items, Function<List<T>, R> function, int partitionSize, Function<List<R>, R> combineFunction)`: Similar to the third method, but uses the default thread pool for execution.

## Parameters
`items`: The list of items to execute the function on.

`function`: The function to execute on each partition of the list. This can be a Consumer or a Function.

`partitionSize`: The size of the partitions to split the list into.

`executor`: (Optional) The Executor to use for the execution. If not provided, a default thread pool will be used.

`combineFunction`: (Optional) The function to combine the results of the function on each partition. This is only used when the function is a Function that returns a result.

## Generics
`T`: The type of the items in the list.

`R`: The return type of the function (only when using a Function).

## Usage
Here's a basic usage example of AsyncListExecutor interface:

```java
AsyncListExecutor asyncListExecutor = ... // obtain an instance of AsyncListExecutor

List<Integer> items = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
Consumer<List<Integer>> function = partition -> partition.forEach(System.out::println);
int partitionSize = 2;

asyncListExecutor.withPartition(items, function, partitionSize);
```
In this example, we create a list of integers and a consumer function that prints each integer. We then call withPartition() on the AsyncListExecutor, passing the list, the function, and a partition size of 2. This will divide the list into partitions of 2 items each and execute the function on each partition concurrently.

## Conclusion
The AsyncListExecutor interface provides a powerful way to process large lists efficiently by utilizing concurrent execution.
Depending on the specific requirements, different methods can be used to control the partition size, the Executor, and how to combine the results.
