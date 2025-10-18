package gs.com.gses.utility;


import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.rabbitMQ.producer.DirectExchangeProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class MqSendUtil {

    @Autowired
    DirectExchangeProducer directExchangeProducer;

    /**
     *
     * 使用消息表方案：强一致性
     *
     * synchronized
     *
     * @param mqMessage
     */
    public void send(MqMessage mqMessage) {
        //处理事务回调发送信息到mq
        //boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        // 判断当前是否存在事务,如果没有开启事务是会报错的
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // 无事务，
            sentAsync(mqMessage);
            return;
        }

        //事务回调：事务同步，此处待处理， 所有事务提交了才会执行 事务回调
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//            @Override
//            public void afterCompletion(int status) {
//                //先执行事务afterCommit，然后执行afterCompletion
//                //afterCommit,afterCompletion
//                //afterCompletion 事务完成
//                // 调用父类的事务提交方法,空方法
//                //   super.afterCompletion(status);
//
//                //事务完成有可能是 回滚 在事务完成之后调用（无论是提交还是回滚）

            ////                int STATUS_COMMITTED = 0;
            ////                int STATUS_ROLLED_BACK = 1;
            ////                int STATUS_UNKNOWN = 2;
//                if (status == 0) {
//                    sentAsync(mqMessage);
//                    int m=0;
//                }
//
//            }

            //在事务成功提交之后调用。
            @Override
            public void afterCommit() {
                System.out.println("send email after transaction commit...");
                sentAsync(mqMessage);
            }
        });

    }


    /**
     * synchronized
     *
     * @param lock
     */
    public void releaseLock(RLock lock) {
        //处理事务回调发送信息到mq
        //boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        // 判断当前是否存在事务,如果没有开启事务是会报错的
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // 无事务，
            lock.unlock();
            log.info("release lock success");
            return;
        }

        //事务回调：事务同步，此处待处理， 所有事务提交了才会执行 事务回调
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCompletion(int status) {
                //先执行事务afterCommit，然后执行afterCompletion
                //afterCommit,afterCompletion
                //afterCompletion 事务完成
                // 调用父类的事务提交方法,空方法
                //   super.afterCompletion(status);

                //事务完成有可能是 回滚
//                int STATUS_COMMITTED = 0;
//                int STATUS_ROLLED_BACK = 1;
//                int STATUS_UNKNOWN = 2;
                if (lock != null) {
                    log.info("release lock success");
                    lock.unlock();
                }


            }


        });

    }

    /**
     *
     * @param lock
     * @param lockSuccessfully
     */
    public void releaseLock(RLock lock, Boolean lockSuccessfully) {
        //处理事务回调发送信息到mq
        //boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        // 判断当前是否存在事务,如果没有开启事务是会报错的
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // 无事务，
            doReleaseLock(lock, lockSuccessfully);
            return;
        }

        //事务回调：事务同步，此处待处理， 所有事务提交了才会执行 事务回调
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCompletion(int status) {
                //先执行事务afterCommit，然后执行afterCompletion
                //afterCommit,afterCompletion
                //afterCompletion 事务完成
                // 调用父类的事务提交方法,空方法
                //   super.afterCompletion(status);

                //事务完成有可能是 回滚
//                int STATUS_COMMITTED = 0;
//                int STATUS_ROLLED_BACK = 1;
//                int STATUS_UNKNOWN = 2;
//                if (lock != null) {
//                    log.info("release lock success");
//                    lock.unlock();
//                }

                doReleaseLock(lock, lockSuccessfully);
            }


        });

    }

    private void doReleaseLock(RLock lock, Boolean lockSuccessfully) {
        if (lockSuccessfully) {
            if (lock != null) {
                try {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                } catch (Exception e) {
                    log.error("doReleaseLock failed: ", e);
                }
            }
        }
    }


    private void sentAsync(MqMessage mqMessage) {
        if (mqMessage == null) {
            return;
        }

//        try {

        CompletableFuture.runAsync(() -> {
            //主线程无法捕捉子线程抛出的异常，除非设置捕捉 Thread.setDefaultUncaughtExceptionHandler
//            try {
            directExchangeProducer.produceMqMessage(mqMessage, null);
//            } catch (Exception e) {
//                log.error("", e);
//            }
//            rabbitMQTest.produceTest(mqMessage);
//                try {
//                    Thread.sleep(60*1000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
        }).exceptionally(ex -> {
            //1、消息发送失败，写入消息表.但是，此处和DB操作不在一个事务里。可能此处保存db失败造成数据不一致
            //2、消息发送失败，更新消息表消息的状态为未发送
            // 处理异常
            System.err.println("Exception caught: " + ex.getMessage());
//                try {
//                    throw ex;
//                } catch (Throwable e) {
//                    throw new RuntimeException(e);
//                }
            return null;
//                return "默认值"; // 提供默认值
        });
//        //没有捕捉到CompletableFuture 抛出的异常
//        } catch (Throwable ex) {
//            throw ex;
//        }
    }
}
