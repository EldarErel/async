package com.eldar.async.decorator;

import com.eldar.async.ThreadUtil;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

/**
 * A callback interface for a decorator to be applied to any Runnable about to be executed.
 */
public class ContextAwareTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable task) {
        return ThreadUtil.withContext(task);
    }

}