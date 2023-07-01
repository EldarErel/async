package com.eldar.async.decorator;

import com.eldar.async.AsyncProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskDecoratorResolver {
    private final AsyncProperties asyncProperties;
    private final ApplicationContext applicationContext;

    public TaskDecorator getTaskDecorator() {
        String taskDecoratorBeanName = asyncProperties.getTaskDecoratorBean();
        if (!StringUtils.hasText(taskDecoratorBeanName)) {
            log.debug("Task Decorator was not provided using configuration, using default");
            return new ContextAwareTaskDecorator();
        }

        if (taskDecoratorBeanName.equals("ContextAwareTaskDecorator")) {
            log.debug("Task Decorator was provided with default");
            return new ContextAwareTaskDecorator();
        }

        try {
            return applicationContext.getBean(taskDecoratorBeanName, TaskDecorator.class);
        } catch (Exception e) {
            log.warn("WARNING: Task Decorator : {} not found, using default", taskDecoratorBeanName);
            return new ContextAwareTaskDecorator();
        }
    }
}
