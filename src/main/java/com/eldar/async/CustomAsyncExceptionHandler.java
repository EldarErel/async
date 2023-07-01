package com.eldar.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;


/**
 * When a method return type is a Future, exception handling is easy. Future.get() method will throw the exception.
 * if the return type is void, exceptions will not be propagated to the calling thread.
 * So, we need to add extra configurations to handle exceptions.
 * handleUncaughtException() method is invoked when there are any uncaught asynchronous exceptions
 */
@Slf4j
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(
            @NonNull Throwable ex,
            @NonNull Method method,
            @NonNull Object... params
    ) {
        log.warn("Unexpected exception occurred invoking async method: {}", method, ex);
        if (log.isDebugEnabled()) {
            // avoid passing possible "Expensive" data in case debug is not enable
            log.debug("Parameter value: [{}]", params);
        }
    }
}