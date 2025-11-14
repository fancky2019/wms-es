package gs.com.gses.utility;

import gs.com.gses.model.utility.RedisKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.PublicKey;
import java.util.*;

@Slf4j
@Component
public class RedisUtil {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public <HK, HV> Map<HK, HV> getHashEntries(String key, Collection<HK> hashKeys) {
        List<HV> values = redisTemplate.<HK, HV>opsForHash().multiGet(key, hashKeys);
        Map<HK, HV> result = new HashMap<>();

        Iterator<HK> keyIter = hashKeys.iterator();
        Iterator<HV> valueIter = values.iterator();

        while (keyIter.hasNext() && valueIter.hasNext()) {
            result.put(keyIter.next(), valueIter.next());
        }

        return result;
    }

    public void releaseLockAfterTransaction(RLock lock, boolean lockSuccessfully) throws Exception {
        //处理事务回调发送信息到mq
        //boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        // 判断当前是否存在事务,如果没有开启事务是会报错的
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // 无事务，非事务方法内会立即释放锁，在某些事务传播不支持事务的方法内会有并发问题。强制在事务内
//            releaseLock(lock, lockSuccessfully);
            throw new Exception("not in Transactional method");
//            return;
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
                releaseLock(lock, lockSuccessfully);


            }


        });

    }

    public void releaseLock(RLock lock, boolean lockSuccessfully) {
        if (lockSuccessfully && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("release lock success");
        }
    }
}
