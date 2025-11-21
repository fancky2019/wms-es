package gs.com.gses.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
//@Primary // 指定为默认实现
public class ThreadPoolExecutorConfig {
    //报错
//public class ThreadPoolExecutorConfig implements AsyncConfigurer {

    /**
     * @Async 线程池的异常不会进入全局异常处理。
     *
     *
     *  *默认只能捕捉到主线程中的异常，无法捕捉到线程池内线程抛出的异常。这是因为线程池内的线程是由线程池管理的，异常不会自动传播到主线程。
     *  *
     *  * 线程池（异步任务）中的异常 不会被 Spring 的全局异常处理器捕获，因为这些异常发生在线程池的子线程中，而 Spring 的异常处理器只作用于主线程（MVC 请求线程）。
     *
     *
     *内部创建 ThreadPoolTaskExecutor  返回 ExecutorService
     *ExecutorService
     *ThreadPoolExecutor
     */


//


//    @Override
//    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
//        return (ex, method, params) -> {
//                        // 处理异常
//            System.err.println("Exception message - " + ex.getMessage());
//            System.err.println("Method name - " + method.getName());
//            for (Object param : params) {
//                System.err.println("Parameter value - " + param);
//            }
//        };

//        return new AsyncUncaughtExceptionHandler()
//        {
//
//            @Override
//            public void handleUncaughtException(Throwable ex, Method method, Object... params) {
//
//            }
//        };
//    }

//    public static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
//
//        @Override
//        public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
//            // 处理异常
//            System.err.println("Exception message - " + throwable.getMessage());
//            System.err.println("Method name - " + method.getName());
//            for (Object param : obj) {
//                System.err.println("Parameter value - " + param);
//            }
//        }
//    }


    /**
     * 异步方法上直接加 @Async("threadPoolExecutor")
     * @return
     */
    @Bean(name = "threadPoolExecutor")
    @Primary
    public Executor threadPoolExecutor() {
//        ExecutorService extends Executor
        //内部使用 LinkedBlockingQueue
        //内部使用 ThreadPoolExecutor
        //内部使用 ThreadPoolTaskExecutor
        ThreadPoolTaskExecutor threadPoolExecutor = new ThreadPoolTaskExecutor();
        int processNum = Runtime.getRuntime().availableProcessors(); // 返回可用处理器的Java虚拟机的数量
        int corePoolSize = (int) (processNum / (1 - 0.2));
        int maxPoolSize =  (int) (processNum / (1 - 0.5));

        //现成创建多了处理器也不执行
//        int corePoolSize = 3 * processNum;
//        int maxPoolSize = 3 * processNum;

        threadPoolExecutor.setCorePoolSize(corePoolSize); // 核心池大小
        threadPoolExecutor.setMaxPoolSize(maxPoolSize); // 最大线程数
        //内部使用 LinkedBlockingQueue
        threadPoolExecutor.setQueueCapacity(maxPoolSize * 1000); // 队列程度
        threadPoolExecutor.setThreadPriority(Thread.MAX_PRIORITY);
        threadPoolExecutor.setDaemon(false);
        threadPoolExecutor.setKeepAliveSeconds(3000);// 线程空闲时间
        threadPoolExecutor.setThreadNamePrefix("ThreadName-Executor-"); // 线程名字前缀
//        threadPoolExecutor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略
        threadPoolExecutor.initialize();
        return threadPoolExecutor;
    }

    /**
     * 不同业务使用不同线程池
     * 异步方法上直接加 @Async("threadPoolExecutor")
     * @return
     */
    @Bean(name = "mqFailHandlerExecutor")
    public Executor mqFailHandlerExecutor() {
        ThreadPoolTaskExecutor threadPoolExecutor = new ThreadPoolTaskExecutor();
        int processNum = Runtime.getRuntime().availableProcessors(); // 返回可用处理器的Java虚拟机的数量
        int corePoolSize = (int) (processNum / (1 - 0.2));
        int maxPoolSize = (int) (processNum / (1 - 0.5));
        threadPoolExecutor.setCorePoolSize(corePoolSize); // 核心池大小
        threadPoolExecutor.setMaxPoolSize(maxPoolSize); // 最大线程数
        //内部使用 LinkedBlockingQueue
        threadPoolExecutor.setQueueCapacity(maxPoolSize * 1000); // 队列程度
        threadPoolExecutor.setThreadPriority(Thread.MAX_PRIORITY);
        threadPoolExecutor.setDaemon(false);
        threadPoolExecutor.setKeepAliveSeconds(3000);// 线程空闲时间
        threadPoolExecutor.setThreadNamePrefix("mqFailHandler-Executor-"); // 线程名字前缀
        threadPoolExecutor.initialize();
        return threadPoolExecutor;
    }
}

