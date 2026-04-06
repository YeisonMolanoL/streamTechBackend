package com.TechPulseInnovations.streamTech.codeReception.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;

/**
 * Configuración para ejecutar tareas asincrónicas
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Pool de threads para tareas IMAP en background
     */
    @Bean(name = "imapTaskExecutor")
    public Executor imapTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("imap-");
        executor.initialize();
        return executor;
    }

    /**
     * Pool de threads para tareas generales asincrónicas
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("task-");
        executor.initialize();
        return executor;
    }
}
