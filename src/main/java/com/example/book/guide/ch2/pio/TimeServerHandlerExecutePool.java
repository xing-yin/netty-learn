package com.example.book.guide.ch2.pio;

import java.util.concurrent.*;

/**
 * 自定义的线程池:解决每个请求一个线程的问题，伪异步
 *
 * 线程池和消息队列均有界，无论并发多大，都不会线程个数过于膨胀或 OOM
 *
 * @author Alan Yin
 * @date 2020/7/13
 */

public class TimeServerHandlerExecutePool {

    private ExecutorService executor;

    public TimeServerHandlerExecutePool(int maxPoolSize, int queueSize) {
        executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                maxPoolSize,
                120L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize)
        );
    }

    public void execute(Runnable task) {
        executor.execute(task);
    }
}
