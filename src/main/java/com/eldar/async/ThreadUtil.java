package com.eldar.async;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class ThreadUtil {

    public static final String COULD_NOT_GET_RESULTS_FROM_FUTURE = "Could not get results from future: {} ";
    public static final String THREAD_INTERRUPTED = "Thread interrupted";
    public static final int DEFAULT_GET_TIMEOUT = 5 * 1000; // 5 seconds

    public static <U> Supplier<U> withContext(Supplier<U> supplier) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        SecurityContext securityContextCopy = getCopyOfSecurityContext();

        return () -> {
            setMDCContext(contextMap);
            SecurityContextHolder.setContext(securityContextCopy);
            try {
                return supplier.get();
            } finally {
                clearContext();
            }
        };
    }

    private static void clearContext() {
        try {
            MDC.clear();
        } catch (Exception e) {
            log.warn("Failed to clear MDC context", e);
        }
        try {
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.warn("Failed to clear Security context", e);
        }
    }

    public static Runnable withContext(Runnable task) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        SecurityContext securityContextCopy = getCopyOfSecurityContext();
        return () -> {
            setMDCContext(contextMap);
            SecurityContextHolder.setContext(securityContextCopy);
            try {
                task.run();
            } finally {
                clearContext();
            }
        };
    }

    public static SecurityContext getCopyOfSecurityContext() {
        // Capture the current SecurityContext
        SecurityContext originalContext = SecurityContextHolder.getContext();
        // Create a copy of the current SecurityContext
        SecurityContext contextCopy = SecurityContextHolder.createEmptyContext();
        contextCopy.setAuthentication(originalContext.getAuthentication());
        return contextCopy;
    }

    public static <T> T handleFutureResult(CompletableFuture<T> future) {
        return future.handle((result, exception) -> {
            if (exception != null) {
                throw new CompletionException(exception);
            }
            return result;
        }).join();
    }


    private static void setMDCContext(Map<String, String> contextMap) {
        MDC.clear();
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        }
    }
    public static <T> T getFromFuture(CompletableFuture<T> future, RuntimeException exceptionToThrow) {
        return getFromFuture(future, exceptionToThrow, DEFAULT_GET_TIMEOUT);
    }

    public static <T> Optional<T> getFromFuture(CompletableFuture<T> future) {
        return getFromFuture(future, DEFAULT_GET_TIMEOUT);
    }

    public static <T> Optional<T> getFromFuture(CompletableFuture<T> future, long timeoutMs) {
        try {
            return Optional.of(future.get(timeoutMs < 0 ? DEFAULT_GET_TIMEOUT : timeoutMs, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            log.warn(THREAD_INTERRUPTED, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn(COULD_NOT_GET_RESULTS_FROM_FUTURE, e.getMessage());
        }
        return Optional.empty();
    }

    public static <T> T getFromFuture(CompletableFuture<T> future, RuntimeException exceptionToThrow, long timeoutMs) {
        try {
            return future.get(timeoutMs < 0 ? DEFAULT_GET_TIMEOUT : timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.warn(THREAD_INTERRUPTED, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn(COULD_NOT_GET_RESULTS_FROM_FUTURE, e.getMessage());
        }
        throw exceptionToThrow;
    }
}