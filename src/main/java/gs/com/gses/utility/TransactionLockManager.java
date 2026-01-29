package gs.com.gses.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TransactionLockManager {
    // 配置常量
    private static final long DEFAULT_WAIT_TIME_SECONDS = 30L;
    private static final long DEFAULT_LEASE_TIME_SECONDS = -1L; // -1表示不自动释放

    @Autowired
    private RedissonClient redisson;

    private ThreadLocal<List<RLock>> threadLocks = ThreadLocal.withInitial(ArrayList::new);
    private ThreadLocal<Boolean> txSyncRegistered = ThreadLocal.withInitial(() -> false);

    //     ====================== 独立锁方法 ======================
    public boolean acquireLocks(List<Long> idList, String prefix) throws Exception {
        List<String> lockKeys = generateLockKeys(idList, prefix);
        return acquireLocks(lockKeys);
    }

    @NotNull
    private List<String> generateLockKeys(List<Long> idList, String prefix) {
        Assert.notEmpty(idList, "idList must not be empty");
        Assert.hasText(prefix, "prefix must not be empty");
        // 默认升序（从小到大）
        List<String> lockKeys = idList.stream()
                .distinct()
                // .sorted(Comparator.reverseOrder()) 降序
                .sorted()  // 等价于 .sorted(Comparator.naturalOrder())
                .map(id -> prefix + id)
                .collect(Collectors.toList());
        return lockKeys;
    }

    public boolean acquireLocks(List<String> lockKeys) throws Exception {
        // 按顺序获取所有锁
        for (String lockKey : lockKeys) {
            RLock lock = redisson.getLock(lockKey);
//            RLock multiLock = redisson.getMultiLock(lock1, lock2);
            // 或者明确指定waitTime，leaseTime为-1表示不设置TTL
            // 尝试获取锁，最多等待10秒
            // boolean isLocked = lock.tryLock(10, TimeUnit.SECONDS);
            boolean acquired = lock.tryLock(DEFAULT_WAIT_TIME_SECONDS, DEFAULT_LEASE_TIME_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                // 释放已获取的锁
                releaseAllLocks();
                throw new Exception("Failed to acquire lock: " + lockKey);
            }
            threadLocks.get().add(lock);
        }
        String lockKeyStr = StringUtils.join(lockKeys, ",");
        log.info("acquireLocks {} {}", true, lockKeyStr);
        return true;
    }

    public void releaseAllLocks() {
        List<RLock> locks = new ArrayList<>(threadLocks.get());
        Collections.reverse(locks);

        for (RLock lock : locks) {
            String lockName = "multiLock";
            if (lock instanceof RedissonMultiLock) {
                //UnsupportedOperationException
//                RedissonMultiLock multiLock = (RedissonMultiLock) lock;
//                lockName = multiLock.getName();

                log.info("start release multiLock");
                lock.unlock();
                log.info("release multiLock success ");
            } else {
                lockName = lock.getName();
                if (lock.isHeldByCurrentThread()) {
                    try {
                        log.info("start release lock, key: {}", lockName);
                        lock.unlock();
                        log.info("release lock success, key: {}", lockName);
                    } catch (Exception e) {
                        log.error("release fail {}", lockName, e);
                    }
                }
            }

        }
        threadLocks.remove();
    }

    // ====================== 联锁方法 ======================

    /**
     * RedissonMultiLock 不支持  lock.getName()
     * @param idList
     * @param prefix
     * @return
     * @throws Exception
     */
    public boolean acquireMultiLock(List<Long> idList, String prefix) throws Exception {
        List<String> lockKeys = generateLockKeys(idList, prefix);
        return acquireMultiLock(lockKeys);
    }

    public boolean acquireMultiLock(List<String> lockKeys) throws Exception {
        List<RLock> acquiredLocks = lockKeys.stream()
                .map(redisson::getLock)
                .collect(Collectors.toList());
        RLock[] lockArray = acquiredLocks.toArray(new RLock[0]);
        RLock multiLock = redisson.getMultiLock(lockArray);
        boolean acquired = multiLock.tryLock(DEFAULT_WAIT_TIME_SECONDS, DEFAULT_LEASE_TIME_SECONDS, TimeUnit.SECONDS);
        if (acquired) {
            threadLocks.get().add(multiLock);
        }
        String lockKeyStr = StringUtils.join(lockKeys, ",");
        log.info("acquireMultiLock {} {}", acquired, lockKeyStr);
        return acquired;
    }

    public void releaseLockAfterTransaction() throws Exception {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new Exception("not in Transactional method");
        }

        if (Boolean.TRUE.equals(txSyncRegistered.get())) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        try {
                            releaseAllLocks();
                        } catch (Exception ex) {
                            log.error("", ex);
                        } finally {
                            txSyncRegistered.remove();
                        }
                    }
                }
        );

        txSyncRegistered.set(true);


    }

}
