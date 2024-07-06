package com.jh.dividendpj.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer { // 스케줄러가 여러개 있을 경우 각 스케줄의 종료 여부와 상관 없이 실행 될 수 있도록 여러 스레드 사용 설정
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPool = new ThreadPoolTaskScheduler();

        int n = Runtime.getRuntime().availableProcessors(); // core 갯수
        threadPool.setPoolSize(n); // 스레드 갯수 설정
        threadPool.initialize();

        taskRegistrar.setTaskScheduler(threadPool);
    }
}
