package com.eldar.async;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Configuration properties for async task execution.
 */
@Configuration
@ConfigurationProperties(
        prefix = "async.task.execution",
        ignoreUnknownFields = false
)
@Validated
@Data
public class AsyncProperties {

    @Valid
    private final Pool pool = new Pool();

    /**
     * Prefix to use for the names of newly created threads.
     */
    @NotEmpty
    private String threadNamePrefix = "async-task-";

    /**
     * Bean name for {@link org.springframework.core.task.TaskDecorator} for newly created threads.
     */
    @NotEmpty
    private String taskDecoratorBean = "ContextAwareTaskDecorator";

    @Data
    public static class Pool {

        /**
         * Core number of threads.
         */
        @Min(1)
        @Max(32)
        private int coreSize = 8;

        /**
         * Maximum allowed number of threads. If tasks are filling up the queue, the pool
         * can expand up to that size to accommodate the load. Ignored if the queue is
         * unbounded.
         */
        @Min(8)
        @Max(64)
        private int maxCoreSize = 16;

        /**
         * Queue capacity. An unbounded capacity does not increase the pool and therefore
         * ignores the "max-core-size" property.
         */
        @Positive
        private int queueCapacity = 100;

        /**
         * Time limit for which threads may remain idle before being terminated.
         */
        @DurationMin(seconds = 0)
        @DurationMax(minutes = 2)
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration keepAliveTimeInSec = Duration.ofSeconds(60);

        /**
         * Whether core threads are allowed to time out. This enables dynamic growing and
         * shrinking of the pool.
         */
        private boolean allowCoreThreadTimeout = false;
    }
}