package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.mapper.wms.MqMessageMapper;
import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.model.enums.MqMessageSourceEnum;
import gs.com.gses.model.enums.MqMessageStatus;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.MqMessageRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.MqMessageResponse;
import gs.com.gses.model.utility.RedisKey;
import gs.com.gses.model.utility.RedisKeyConfigConst;
import gs.com.gses.multipledatasource.DataSource;
import gs.com.gses.multipledatasource.DataSourceType;
import gs.com.gses.rabbitMQ.RabbitMQConfig;
import gs.com.gses.service.MqMessageService;
import gs.com.gses.service.TruckOrderItemService;
import gs.com.gses.service.TruckOrderService;
import gs.com.gses.utility.LambdaFunctionHelper;
import gs.com.gses.utility.MqSendUtil;
import gs.com.gses.utility.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author author
 * @since 2023-11-15
 */
@Slf4j
@Service

//@Primary
//@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)  // 强制TARGET_CLASS代理获取完整bean,或者jdk动态代理通过PostConstruct内获取完整bean
//接口加   @Transactional(rollbackFor = Exception.class,
public class MqMessageServiceImpl extends ServiceImpl<MqMessageMapper, MqMessage> implements MqMessageService {

    /**
     *
     默认jdk 动态代理：
     Advisor 不全
     selfProxy 触发了 early reference  先创建了一个“半成品代理”
     不是 @Transactional 失效，而是你拿到的代理对象根本没有事务拦截器。
     @Async + 自注入 + 提前代理暴露。
     自注入selfProxy导致Spring在Bean创建过程中提前创建了一个不完整的代理（0 advisors）。

     改成cglib 动态代理：从applicationContext获取bean可获取全部Advisor信息。


     使用 @Lazy 后：aop 功能可能丢失
     真实对象 ← CGLIB代理（包含Advisors） ← Lazy代理（没有Advisors） ← Spring容器Bean
     */
    @Autowired
    @Lazy  // 防止循环依赖
    private MqMessageService lazySelfProxy;

//     通过setter方法注入，而不是字段注入
//    @Autowired
//    @Lazy
//    public void setSelfProxy(MqMessageService selfProxy) {
//        this.selfProxy = selfProxy;
//    }

    // 声明一个实例变量来持有完整的代理对象
    private MqMessageService selfProxy;
    @Autowired
    private ObjectProvider<MqMessageService> serviceProvider;
    //“代理套代理 + early reference”
//容器中取的双重代理（双重代理）事务增强器没有：targetSource [SingletonTargetSource for target object [com.sun.proxy.$Proxy276]]

    @PostConstruct
    public void init() {
        //事务生效可获取完整bean
        //生命周期顺序：实例化 → 依赖注入 → AOP代理完成 → @PostConstruct
        //在 Bean 初始化完成后获取完整的代理
//        this.selfProxy = applicationContext.getBean(MqMessageService.class);
        this.selfProxy = serviceProvider.getObject();
        // 验证代理完整性
        boolean isAopProxy = AopUtils.isAopProxy(selfProxy);
        boolean isCglibProxy = AopUtils.isCglibProxy(selfProxy);
        boolean isJdkProxy = AopUtils.isJdkDynamicProxy(selfProxy);
        log.info("Proxy info - AOP: {}, CGLIB: {}, JDK: {}", isAopProxy, isCglibProxy, isJdkProxy);
//        BeanPostProcessor
//        InitializingBean
    }

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
//    @Lazy
    private MqSendUtil mqSendUtil;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Executor executor;
    @Autowired
    private TransactionTemplate transactionTemplate;

//    private final @Lazy IProductTestService productTestService;

    //   @Lazy 解决循环依赖
//    @Autowired
//    @Lazy
//    private IProductTestService productTestService;

//    @Autowired
//    @Lazy
//    private TruckOrderItemService truckOrderItemService;
//
//    @Autowired
//    private TruckOrderService truckOrderService;


    //12次
    private int[] retryPeriod = new int[]{5, 10, 20, 40, 60, 2 * 60, 4 * 60, 8 * 60, 16 * 60, 32 * 60, 1 * 60 * 60, 2 * 60 * 60};


    @Override
//    @Transactional(rollbackFor = Exception.class,isolation = Isolation.REPEATABLE_READ)
    @Transactional(rollbackFor = Exception.class)
    public void transactionRepeatReadLock() throws Exception {
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        // 判断当前是否存在事务,如果没有开启事务是会报错的
        boolean isActualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();

        long id = 1979084944348372994L;
        MqMessage mqMessage = this.getById(id);
        String lockKey = RedisKey.UPDATE_MQ_MESSAGE_INFO + ":" + id;
        //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {

            //  return this.tryLock(waitTime, -1L, unit); 不指定释放时间，RedissonLock内部设置-1，
            lockSuccessfully = lock.tryLock(RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME, TimeUnit.SECONDS);
            if (!lockSuccessfully) {
                String msg = MessageFormat.format("Get lock {0} fail，wait time : {1} s", lockKey, RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME);
                throw new Exception(msg);
            }
            log.info("update get lock {}", lockKey);

            //MyBatis 的**一级缓存（Local Cache）**就是：SqlSession 级别的缓存，默认开启，
            // 用来缓存“同一个 SqlSession 内、相同 SQL 的查询结果”。
            //同一个 SqlSession 里，相同 SQL 不会再查数据库，而是从缓存拿。

            //CacheKey =Mapper方法ID +SQL语句 +参数值 +分页参数(RowBounds) +环境ID
            MqMessage mqMessage1 = this.getById(id);

            LambdaQueryWrapper<MqMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MqMessage::getVersion, mqMessage.getVersion());
            queryWrapper.eq(MqMessage::getId, mqMessage.getId());
            List<MqMessage> mqMessageList = this.list(queryWrapper);

            if (CollectionUtils.isEmpty(mqMessageList)) {
                throw new Exception("MqMessage " + id + " has been changed");
            }
            int n = 0;

            //如果根据版本号查询失效，就执行更新校验数据有没有被其他事务更改
            Integer oldVersion = mqMessage.getVersion();
            mqMessage.setVersion(mqMessage.getVersion() + 1);
            mqMessage.setLastModificationTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            LambdaUpdateWrapper<MqMessage> updateWrapper = new LambdaUpdateWrapper<MqMessage>();
            updateWrapper.set(MqMessage::getVersion, mqMessage.getVersion());
            updateWrapper.eq(MqMessage::getId, mqMessage.getId());
            updateWrapper.eq(MqMessage::getVersion, oldVersion);
            boolean re = this.update(mqMessage, updateWrapper);
            if (!re) {
                throw new Exception("MqMessage " + id + " has been changed");
            }


        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        } finally {
            //非事务操作在此释放
//            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
            redisUtil.releaseLockAfterTransaction(lock, lockSuccessfully);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public MqMessage add(MqMessage mqMessage) {
        this.save(mqMessage);
        return mqMessage;
    }

    @Override
    public List<MqMessage> addBatch(List<MqMessage> mqMessageList) {

        //SQL Server的JDBC驱动限制：SQL Server的JDBC驱动在批量插入时无法完美支持返回所有插入记录的主键值，只能返回最后一个插入记录的主键值
        this.saveBatch(mqMessageList);
        return mqMessageList;
//        this.customSaveBatch(truckOrderItemList);

//        return truckOrderItemList;
    }

    @Override
    public MqMessage addMessage(MqMessageRequest request) throws Exception {
        MqMessage mqMessage = createMessageByRequest(request);
        return this.add(mqMessage);
    }

    @NotNull
    private static MqMessage createMessageByRequest(MqMessageRequest request) {
        String msgId = UUID.randomUUID().toString().replaceAll("-", "");
        MqMessage mqMessage = new MqMessage();
        mqMessage.setMsgId(msgId);
        mqMessage.setBusinessId(request.getBusinessId());
        mqMessage.setBusinessKey(request.getBusinessKey());
        mqMessage.setMsgContent(request.getMsgContent());
        mqMessage.setExchange(request.getExchange());
        mqMessage.setRouteKey(request.getRouteKey());
        mqMessage.setQueue(request.getQueue());
        mqMessage.setTopic(request.getTopic());
        mqMessage.setMaxRetryCount(request.getMaxRetryCount());
        mqMessage.setRetry(true);
        mqMessage.setStatus(MqMessageStatus.NOT_PRODUCED.getValue());
        mqMessage.setTraceId(MDC.get("traceId"));
        mqMessage.setNextRetryTime(LocalDateTime.now());
        mqMessage.setDeleted(0);
        mqMessage.setVersion(1);
        mqMessage.setSendMq(request.getSendMq());
        //13位  毫秒时间戳，不是秒9位  转时间戳
        long localDateTimeMillis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        mqMessage.setCreationTime(localDateTimeMillis);
        mqMessage.setLastModificationTime(localDateTimeMillis);
        return mqMessage;
    }

    @Override
    public List<MqMessage> addMessageBatch(List<MqMessageRequest> requestList) throws Exception {
        List<MqMessage> mqMessageList = new ArrayList<>();
        for (MqMessageRequest request : requestList) {
//            String msgId = UUID.randomUUID().toString().replaceAll("-", "");
//            MqMessage mqMessage = new MqMessage();
//            mqMessage.setMsgId(msgId);
//            mqMessage.setBusinessId(request.getBusinessId());
//            mqMessage.setBusinessKey(request.getBusinessKey());
//            mqMessage.setMsgContent(request.getMsgContent());
//            mqMessage.setExchange(request.getExchange());
//            mqMessage.setRouteKey(request.getRouteKey());
//            mqMessage.setQueue(request.getQueue());
//            mqMessage.setTopic(request.getTopic());
//            mqMessage.setMaxRetryCount(request.getMaxRetryCount());
//            mqMessage.setRetry(true);
//            mqMessage.setStatus(MqMessageStatus.NOT_PRODUCED.getValue());
//            mqMessage.setTraceId(MDC.get("traceId"));
//
//            mqMessage.setDeleted(0);
//            mqMessage.setVersion(1);
//            mqMessage.setSendMq(request.getSendMq());
//            //13位  毫秒时间戳，不是秒9位  转时间戳
//            long localDateTimeMillis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
//            mqMessage.setCreationTime(localDateTimeMillis);
//            mqMessage.setLastModificationTime(localDateTimeMillis);
            MqMessage mqMessage = createMessageByRequest(request);
            mqMessageList.add(mqMessage);
        }
        return this.addBatch(mqMessageList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MqMessage mqMessage) throws Exception {

        String lockKey = RedisKey.UPDATE_MQ_MESSAGE_INFO + ":" + mqMessage.getId();
        //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {

            //  return this.tryLock(waitTime, -1L, unit); 不指定释放时间，RedissonLock内部设置-1，
            lockSuccessfully = lock.tryLock(RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME, TimeUnit.SECONDS);
            if (!lockSuccessfully) {
                String msg = MessageFormat.format("Get lock {0} fail，wait time : {1} s", lockKey, RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME);
                throw new Exception(msg);
            }
            log.info("update get lock {}", lockKey);

            Integer oldVersion = mqMessage.getVersion();
            mqMessage.setVersion(mqMessage.getVersion() + 1);
            mqMessage.setLastModificationTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            LambdaUpdateWrapper<MqMessage> updateWrapper = new LambdaUpdateWrapper<MqMessage>();
            updateWrapper.eq(MqMessage::getVersion, oldVersion);
            updateWrapper.eq(MqMessage::getId, mqMessage.getId());
            boolean re = this.update(mqMessage, updateWrapper);
            if (!re) {
                String message = MessageFormat.format("MqMessage update fail :id - {0} ,version - {1}", mqMessage.getId(), oldVersion);
                throw new Exception(message);
            }
            log.info("MqMessageUpdate id {} msgId {} status {}", mqMessage.getId(), mqMessage.getMsgId(), mqMessage.getStatus());


        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        } finally {
            //非事务操作在此释放
//            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
            redisUtil.releaseLockAfterTransaction(lock, lockSuccessfully);
        }

    }

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public void updateByMsgId(String msgId, int status) throws Exception {
//        this.updateById(mqMessage);
//        this.update(mqMessage, new LambdaUpdateWrapper<MqMessage>().eq(MqMessage::getId, mqMessage.getId()));
        Exception e = transactionTemplate.execute(transactionStatus -> {

            // 事务性操作
            // 如果操作成功，不抛出异常，事务将提交

            try {


                String lockKey = RedisKey.UPDATE_MQ_MESSAGE_INFO + ":" + msgId;
                //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
                RLock lock = redissonClient.getLock(lockKey);
                boolean lockSuccessfully = false;
                try {

//            lockSuccessfully = lock.tryLock(RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME, RedisKey.INIT_INVENTORY_INFO_FROM_DB_LEASE_TIME, TimeUnit.SECONDS);
                    //  return this.tryLock(waitTime, -1L, unit); 不指定释放时间，RedissonLock内部设置-1，
                    lockSuccessfully = lock.tryLock(RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME, TimeUnit.SECONDS);
                    if (!lockSuccessfully) {
                        String msg = MessageFormat.format("Get lock {0} fail，wait time : {1} s", lockKey, RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME);
                        throw new Exception(msg);
                    }
                    log.info("updateByMsgId get lock {}", lockKey);
                    LambdaQueryWrapper<MqMessage> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(MqMessage::getMsgId, msgId);
                    List<MqMessage> mqMessageList = this.list(queryWrapper);
                    MqMessage mqMessage = null;
                    if (!mqMessageList.isEmpty()) {
                        mqMessage = mqMessageList.get(0);
                    }
                    if (mqMessage == null) {
                        throw new Exception("Can't get MqMessage by MsgId :" + mqMessage.getMsgId());
                    }
//                    rabbitmq 同一条消息都已经消费了  rabbitmq才收到该消息的确认 confirm.要进行状态判断
                    if (status == MqMessageStatus.PRODUCE.getValue()) {
                        if (mqMessage.getStatus() != MqMessageStatus.NOT_PRODUCED.getValue()) {
                            log.info("MqMessage {} MqMessageStatus - {} is not NOT_PRODUCED", mqMessage.getId(), mqMessage.getStatus());
                            return null;
                        }

                    } else if (status == MqMessageStatus.CONSUMED.getValue()) {
                        if (mqMessage.getStatus() == MqMessageStatus.CONSUMED.getValue()) {
                            log.info("MqMessage {} has been CONSUMED", mqMessage.getId());
                            return null;
                        }
                    } else if (status == MqMessageStatus.CONSUME_FAIL.getValue()) {
                        if (mqMessage.getStatus() == MqMessageStatus.CONSUMED.getValue()) {
                            log.info("MqMessage {} has been CONSUMED", mqMessage.getId());
                            return null;
                        }
                    }

                    Integer oldVersion = mqMessage.getVersion();
                    mqMessage.setVersion(mqMessage.getVersion() + 1);
                    mqMessage.setStatus(status);
                    mqMessage.setLastModificationTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    LambdaUpdateWrapper<MqMessage> updateWrapper = new LambdaUpdateWrapper<MqMessage>();
                    updateWrapper.eq(MqMessage::getVersion, oldVersion);
                    updateWrapper.eq(MqMessage::getId, mqMessage.getId());
                    boolean re = this.update(mqMessage, updateWrapper);
                    if (!re) {
                        String message = MessageFormat.format("MqMessage update fail :id - {0} ,version - {1}", mqMessage.getId(), oldVersion);
                        throw new Exception(message);
                    }
                    log.info("MqMessageUpdateByMsgId id {} msgId {} status {}", mqMessage.getId(), mqMessage.getMsgId(), status);

                } catch (Exception ex) {
                    log.error("", ex);
                    throw ex;
                } finally {
                    //非事务操作在此释放
//            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
                    redisUtil.releaseLockAfterTransaction(lock, lockSuccessfully);
                }


                return null;
            } catch (Exception ex) {
                log.info("updateByMsgId {} fail", msgId);
                log.error("", ex);
                // 如果操作失败，抛出异常，事务将回滚
                transactionStatus.setRollbackOnly();
                return ex;
                //此处是定时任务 ，处理异常不抛出
//                    transactionStatus.setRollbackOnly();
//                    throw  e;
            }


        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStaus(long mqMessageId, MqMessageStatus status) throws Exception {
//        this.updateById(mqMessage);
//        this.update(mqMessage, new LambdaUpdateWrapper<MqMessage>().eq(MqMessage::getId, mqMessage.getId()));

        String lockKey = RedisKey.UPDATE_MQ_MESSAGE_INFO + ":" + mqMessageId;
        //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {

//            lockSuccessfully = lock.tryLock(RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME, RedisKey.INIT_INVENTORY_INFO_FROM_DB_LEASE_TIME, TimeUnit.SECONDS);
            //  return this.tryLock(waitTime, -1L, unit); 不指定释放时间，RedissonLock内部设置-1，
            lockSuccessfully = lock.tryLock(RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME, TimeUnit.SECONDS);
            if (!lockSuccessfully) {
                String msg = MessageFormat.format("Get lock {0} fail，wait time : {1} s", lockKey, RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME);
                throw new Exception(msg);
            }
            log.info("updateByMsgId get lock {}", lockKey);
            LambdaQueryWrapper<MqMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MqMessage::getId, mqMessageId);
            List<MqMessage> mqMessageList = this.list(queryWrapper);
            MqMessage mqMessage = null;
            if (!mqMessageList.isEmpty()) {
                mqMessage = mqMessageList.get(0);
            }
            if (mqMessage == null) {
                throw new Exception("Can't get MqMessage by MsgId :" + mqMessage.getMsgId());
            }

            Integer oldVersion = mqMessage.getVersion();
            mqMessage.setVersion(mqMessage.getVersion() + 1);
            mqMessage.setStatus(status.getValue());
            mqMessage.setLastModificationTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            LambdaUpdateWrapper<MqMessage> updateWrapper = new LambdaUpdateWrapper<MqMessage>();
            updateWrapper.eq(MqMessage::getVersion, oldVersion);
            updateWrapper.eq(MqMessage::getId, mqMessage.getId());
            boolean re = this.update(mqMessage, updateWrapper);
            if (!re) {
                String message = MessageFormat.format("MqMessage update fail :id - {0} ,version - {1}", mqMessage.getId(), oldVersion);
                throw new Exception(message);
            }
            log.info("MqMessageUpdateStaus id {} msgId {} status {}", mqMessage.getId(), mqMessage.getMsgId(), status);

        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        } finally {
            //非事务操作在此释放
//            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
            redisUtil.releaseLockAfterTransaction(lock, lockSuccessfully);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByMsgId(String msgId, int status, String queue) throws Exception {
        if (StringUtils.isEmpty(queue)) {
            log.info("updateByMsgId {} queue {} isEmpty return", msgId, queue);
            return;
        } else if (queue.equals(RabbitMQConfig.DIRECT_QUEUE_NAME)) {
            log.info("updateByMsgId {} queue {} return", msgId, queue);
            return;
        }
        log.info("updateByMsgId {} queue {} ", msgId, queue);
        MqMessageService selfProxy = applicationContext.getBean(MqMessageService.class);
        selfProxy.updateByMsgId(msgId, status);
    }

    @Override
    public PageData<MqMessageResponse> list(MqMessageRequest request) throws JsonProcessingException {
        // 设置时区为 GMT+8   UTC
        ZonedDateTime zonedDateTime = ZonedDateTime.now();

        //ZonedDateTime序列化  ZonedDateTime jackson
        ZoneId zoneId = ZoneId.of("UTC");
        ZonedDateTime utcDateTime = zonedDateTime.withZoneSameInstant(zoneId);
        //要带Z 否则jackson序列化异常
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        //2025-03-12T07:09:12.445Z
        String utcDateTimeStr = utcDateTime.format(formatter);
        String jsonZone = objectMapper.writeValueAsString(zonedDateTime);
        ZonedDateTime tt = objectMapper.readValue(jsonZone, ZonedDateTime.class);


        LambdaQueryWrapper<MqMessage> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(MqMessage::getStatus, 2);
        //排序
//        queryWrapper.orderByDesc(User::getAge)
//                .orderByAsc(User::getName);
        //避免排序
        long count = this.count(queryWrapper);


//        int skip = request.getPageSize() * (request.getPageIndex() - 1) * 10;
//        queryWrapper.last("limit " + skip + ",10");
//        String limit = MessageFormat.format("limit {0} , {1}", skip, request.getPageSize());
////        queryWrapper.last("limit "+(request.getPageIndex()-1)*10+","+(
//        queryWrapper.orderByDesc(ShipOrder::getId).last(limit);


//        queryWrapper.orderByDesc(MqMessage::getId).last("limit 10");
//        List<MqMessage> mqMessageList = this.list(queryWrapper);

        //注意：配置 MybatisPlusPageInterceptor
        // 创建分页对象 (当前页, 每页大小)
        Page<MqMessage> page = new Page<>(0, 10);
        //排序
        List<OrderItem> orderItems = LambdaFunctionHelper.getWithDynamicSort(request.getSortFieldList());
////            queryWrapper.orderBy(true, true, orderItems);
//        // ROW_NUMBER() OVER (ORDER BY id ASC, creationTime ASC) as __row_number__
        page.setOrders(orderItems);
        // 执行分页查询, sqlserver 使用通用表达式 WITH selectTemp AS
        IPage<MqMessage> mqMessagePage = this.baseMapper.selectPage(page, queryWrapper);

        // 获取结果   // 当前页数据
        List<MqMessage> mqMessageList = mqMessagePage.getRecords();
        long total = mqMessagePage.getTotal();


        List<MqMessageResponse> mqMessageResponseList = mqMessageList.stream().map(p -> {
            MqMessageResponse response = new MqMessageResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<MqMessageResponse> pageData = new PageData<>();
        pageData.setData(mqMessageResponseList);
        pageData.setCount(count);
        return pageData;
    }

    @Override
    public PageData<MqMessageResponse> getMqMessagePage(MqMessageRequest request) {
        LambdaQueryWrapper<MqMessage> messageQueryWrapper = new LambdaQueryWrapper<>();
        messageQueryWrapper.eq(MqMessage::getDeleted, 0);

        if (request.getId() != null && request.getId() > 0) {
            messageQueryWrapper.eq(MqMessage::getId, request.getId());
        }
        if (request.getBusinessId() != null && request.getBusinessId() > 0) {
            messageQueryWrapper.eq(MqMessage::getBusinessId, request.getBusinessId());
        }
        if (StringUtils.isNotEmpty(request.getBusinessKey())) {
            messageQueryWrapper.eq(MqMessage::getBusinessKey, request.getBusinessKey());
        }

        if (StringUtils.isNotEmpty(request.getBusinessKey())) {
            messageQueryWrapper.eq(MqMessage::getBusinessKey, request.getBusinessKey());
        }

        // 创建分页对象 (当前页, 每页大小)
        Page<MqMessage> page = new Page<>(request.getPageIndex(), request.getPageSize());
        if (CollectionUtils.isEmpty(request.getSortFieldList())) {
            List<Sort> sortFieldList = new ArrayList<>();
            Sort sort = new Sort();
            sort.setSortField("id");
            sort.setSortType("desc");
            sortFieldList.add(sort);
            request.setSortFieldList(sortFieldList);
        }
        if (CollectionUtils.isNotEmpty(request.getSortFieldList())) {
            List<OrderItem> orderItems = LambdaFunctionHelper.getWithDynamicSort(request.getSortFieldList());
            page.setOrders(orderItems);
        }

        if (request.getSearchCount() != null) {
            // 关键设置：不执行 COUNT 查询
            page.setSearchCount(request.getSearchCount());
        }

        // 执行分页查询, sqlserver 使用通用表达式 WITH selectTemp AS
        IPage<MqMessage> truckOrderItemPage = this.baseMapper.selectPage(page, messageQueryWrapper);

        // 获取当前页数据
        List<MqMessage> records = truckOrderItemPage.getRecords();
        long total = truckOrderItemPage.getTotal();

        List<MqMessageResponse> messageResponseResponseList = records.stream().map(p -> {
            MqMessageResponse response = new MqMessageResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());
//        if (true) {
//            return PageData.getDefault();
//        }

        //searchCount 可能false
        if (messageResponseResponseList.size() == 0) {
            log.info("No records found");
            return PageData.getDefault();
        }


        PageData<MqMessageResponse> pageData = new PageData<>();
        pageData.setData(messageResponseResponseList);
        pageData.setCount(total);
        return pageData;
    }

    @Override
    public void count(MqMessageRequest mqMessage) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(MqMessage mqMessage) {
        this.remove(new LambdaQueryWrapper<MqMessage>().eq(MqMessage::getId, mqMessage.getId()));
    }


    /**
     * 生产失败的重新发布到mq，消费失败的
     *
     * 测试：mqMessageOperation
     */
    @Override
    public void mqOperation() {
//        LambdaQueryWrapper<MqMessage> queryWrapper = new LambdaQueryWrapper<>();
//        //没有消费确认=发布失败+消费失败
//        queryWrapper.eq(MqMessage::getStatus, 2);
//        List<MqMessage> mqMessageList = this.list(queryWrapper);
//        List<MqMessage> unPushList = mqMessageList.stream().filter(p -> p.getStatus().equals(0)).collect(Collectors.toList());
//        //可设计单独的job 处理消费失败
//        List<MqMessage> consumerFailList = mqMessageList.stream().filter(p ->  p.getStatus().equals(1) &&  p.getStatus().equals(2)).collect(Collectors.toList());
//        rePublish(unPushList);
//        reConsume(consumerFailList);

        log.info("start executing mqOperation");

        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String operationLockKey = RedisKeyConfigConst.MQ_FAIL_HANDLER;
        //并发访问，加锁控制，此方法内没有事务操作。可以用try finally 释放资源 否则用 MqSendUtil releaseLock 方法
        RLock lock = redissonClient.getLock(operationLockKey);
        boolean lockSuccessfully = false;
        try {
            long waitTime = 10;
            long leaseTime = 30;
            lockSuccessfully = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (lockSuccessfully) {
                //无论成功失败都会更新一次数据库，使得UpdateTime 变更保持索引的数据少
                //联合索引（UpdateTime，Status）
                LocalDateTime startQueryTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String timeStr = formatter.format(LocalDateTime.now());

                //将更新时间写入redis
//                String latestExecutingTimeRedis = valueOperations.get(RedisKeyConfigConst.MQ_FAIL_HANDLER_TIME);
                //redis key 不存在
//                if (StringUtils.isEmpty(latestExecutingTimeRedis)) {
//                    startQueryTime = null;
//                } else {
//                    startQueryTime = LocalDateTime.parse(latestExecutingTimeRedis, formatter);
//                }
//                valueOperations.set(RedisKeyConfigConst.MQ_FAIL_HANDLER_TIME, timeStr);

                LambdaQueryWrapper<MqMessage> queryWrapper = new LambdaQueryWrapper<>();
                //mybatis-plus and or
//                //没有消费确认  (AandB)or(C)
//                queryWrapper.and(p->p.ne(MqMessage::getStatus, 2).eq(MqMessage::getId, 83));
////                queryWrapper.or(p->p.ne(MqMessage::getStatus, 2).eq(MqMessage::getId, 83));
//                queryWrapper.or(p->p.eq(MqMessage::getStatus,null));
//                mysql null 不运算 <>
                //<, <=, >, >=, <>  lt()，le()，gt()，ge()，ne()
//                List<Integer> idList = new ArrayList<>();
//                idList.add(1);
//                queryWrapper.in(MqMessage::getId, idList);
//                long   startQueryMillis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                if (startQueryTime != null) {
                    //时间没有设计好暂时注释
//                    long startQueryMillis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
//                    queryWrapper.ge(MqMessage::getLastModificationTime, startQueryMillis);
                }
//                queryWrapper.lt(MqMessage::getNextRetryTime, LocalDateTime.now());
                queryWrapper.and(p -> p.lt(MqMessage::getNextRetryTime, LocalDateTime.now()).or().isNull(MqMessage::getNextRetryTime));
//                queryWrapper.ne(MqMessage::getStatus, 2);
                queryWrapper.and(p -> p.isNull(MqMessage::getStatus).or(m -> m.ne(MqMessage::getStatus, 2)));
                String fieldName = LambdaFunctionHelper.getFieldName(MqMessage::getRetryCount);
                String retryCountColName = LambdaFunctionHelper.getColumnName(MqMessage::getRetryCount);
                String maxRetryCountColName = LambdaFunctionHelper.getColumnName(MqMessage::getMaxRetryCount);
                String applyStr = MessageFormat.format("{0} < {1}", retryCountColName, maxRetryCountColName);
                queryWrapper.and(p -> p.isNull(MqMessage::getMaxRetryCount).or(m -> m.apply(applyStr)));


                List<MqMessage> mqMessageList = this.list(queryWrapper);
                if (CollectionUtils.isEmpty(mqMessageList)) {
                    return;
                }
//                if (true) {
//                    return;
//                }
                //0:未生成 1：已生产 2：已消费 3:消费失败
                //未推送消息(未推送，推送失败
                List<MqMessage> unPushList = mqMessageList.stream().filter(p -> p.getSendMq() != null && p.getSendMq() && (p.getStatus() == null || p.getStatus().equals(MqMessageStatus.NOT_PRODUCED.getValue()))).collect(Collectors.toList());
                //可设计单独的job 处理消费失败.消费失败的，才走定时任务补偿处理
                List<MqMessage> consumerFailList = mqMessageList.stream().filter(p -> p.getStatus() != null && (p.getStatus().equals(MqMessageStatus.CONSUME_FAIL.getValue()))).collect(Collectors.toList());
                rePublish(unPushList);
                log.info("unPushList  size {}", unPushList.size());
                MqMessageService selfProxy = applicationContext.getBean(MqMessageService.class);
                Object object = selfProxy;
                // 从 ApplicationContext 获取代理对象
                MqMessageService proxyService = applicationContext.getBean(MqMessageService.class);
                log.info("consumerFailList  size {}", consumerFailList.size());
                selfProxy.reConsume(consumerFailList);


            } else {
                //如果controller是void 返回类型，此处返回 MessageResult<Void>  也不会返回给前段
                //超过waitTime ，扔未获得锁
                log.info("mqFailHandler:获取锁失败");
            }
        } catch (Exception e) {
            // throw  e;
            log.error("", e);
        } finally {
            //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
            //unlock 删除key
//            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }

            redisUtil.releaseLock(lock, lockSuccessfully);
        }


    }

    @Override
    public void rePublish(List<MqMessage> mqMessageList) {
//        CompletableFuture.runAsync(() ->
//        {
//            publish(mqMessageList);
//        });
        if (CollectionUtils.isEmpty(mqMessageList)) {
            return;
        }
        log.info("rePublish mqMessageList size {}", mqMessageList.size());
        executor.execute(() -> {
            publish(mqMessageList);
//            // 模拟耗时操作
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("Task completed.");
        });
    }

    private synchronized void publish(List<MqMessage> mqMessageList) {

        log.info("start executing publish");
        try {
            for (MqMessage message : mqMessageList) {
                MqMessage dbMessage = this.getById(message.getId());
                //已发送了
                if (!dbMessage.getStatus().equals(MqMessageStatus.NOT_PRODUCED.getValue())) {
                    continue;
                }
                log.info("rePublish MqMessage id {} msgId {}", message.getId(), message.getMsgId());
                mqSendUtil.send(message);
            }
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    /**
     * 处理消费失败
     * 加了 @Async  和xxl-hob 的线程池 不在同一个线程，使用mqFailHandlerExecutor 线程池
     * @param mqMessageList
     * @throws Exception
     */
    @Async("mqFailHandlerExecutor")
    @Override
    public void reConsume(List<MqMessage> mqMessageList) throws Exception {
        if (CollectionUtils.isEmpty(mqMessageList)) {
            return;
        }
        log.info("reConsume mqMessageList size {}", mqMessageList.size());
//        CompletableFuture.runAsync(() ->
//        {
//            consume(mqMessageList);
//        });
        MqMessageService selfProxy = applicationContext.getBean(MqMessageService.class);
        for (MqMessage message : mqMessageList) {

            if (message.getMaxRetryCount() == null || (message.getMaxRetryCount() != null && message.getMaxRetryCount() > message.getRetryCount())) {
                log.info("reConsume MqMessage id {} msgId {}", message.getId(), message.getMsgId());
//                consume(message);

//                selfProxy.MqMessageEventHandler(message, MqMessageSourceEnum.JOB);

                Exception e = transactionTemplate.execute(transactionStatus -> {
                    try {
                        selfProxy.MqMessageEventHandler(message, MqMessageSourceEnum.JOB);
                        return null;
                    } catch (Exception ex) {
                        log.info("executing reConsume message {} fail", message.getId());
                        log.error("", ex);
                        // 如果操作失败，抛出异常，事务将回滚
                        transactionStatus.setRollbackOnly();
                        return ex;
                    }
                });

            } else {
                log.info("exceed  max retry count {}", message.getId());
            }

        }
    }

    //    @Transactional(rollbackFor = Exception.class)
    public void consume(MqMessage message) throws Exception {
        log.info("start executing consume message {}", message.getId());
        //事务回滚 手动回滚事务 手动提交事务
        //事务回滚 手动回滚 手动控制事务，编程式事务
        //TransactionAspectSupport
        //PlatformTransactionManager 参见  com.example.demo.service.demo.PersonService
        //TransactionTemplate提供了更简洁的API来管理事务。它隐藏了底层的PlatformTransactionManager的使用
//        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();


//        事务模板方法
        // 在这里执行事务性操作
        // 操作成功则事务提交，否则事务回滚
        Exception e = transactionTemplate.execute(transactionStatus -> {

            // 事务性操作
            // 如果操作成功，不抛出异常，事务将提交

            try {

//
                switch (message.getQueue()) {
                    case RabbitMQConfig.DIRECT_QUEUE_NAME:
                        //根据对应的queue执行对应的service 消费代码
//                            boolean consumeSuccess = true;
//                            if (consumeSuccess) {
//
//                            }
                        // productTestService.mqMessageConsume(message);
                        //update message status
//                        message.setStatus(MqMessageStatus.CONSUMED.getValue());
//                        this.update(message);

//                    if (message.getId() % 2 != 0) {
//                        throw new Exception("test");
//                    }
                        break;
//                    case UtilityConst.TRUCK_ORDER_ITEM_DEBIT:
//                        truckOrderItemService.debit(message);
//                        break;
//                    case UtilityConst.CHECK_TRUCK_ORDER_STATUS:
//                        truckOrderService.synchronizeStatus(message);
//                        break;
                    default:
                        break;
                }


                MqMessage dbMessage = this.getById(message.getId());
                if (dbMessage != null && !dbMessage.getStatus().equals(MqMessageStatus.CONSUMED)) {
                    updateStaus(dbMessage.getId(), MqMessageStatus.CONSUMED);
                }

                return null;
            } catch (Exception ex) {
                log.info("executing consume message {} fail", message.getId());
                log.error("", ex);
                // 如果操作失败，抛出异常，事务将回滚
                transactionStatus.setRollbackOnly();
                return ex;
                //此处是定时任务 ，处理异常不抛出
//                    transactionStatus.setRollbackOnly();
//                    throw  e;
            }


//        TransactionCallbackWithoutResult

        });

        if (e != null) {
            //将该条事务的异常保存
            transactionTemplate.execute(transactionStatus -> {
                try {
                    setRetryInfo(message);
                    //失败了就更新一下版本号和更新时间，根据更新时间的 索引 提高查询速度
                    message.setStatus(gs.com.gses.model.enums.MqMessageStatus.CONSUME_FAIL.getValue());
                    message.setFailureReason(e.getMessage());
                    this.update(message);
                    return true;
                } catch (Exception ex) {
                    log.error("", ex);
                    transactionStatus.setRollbackOnly();
                    return false;
                }
            });
        }


    }


    private void setRetryInfo(MqMessage message) {
        int retryCount = message.getRetryCount() == null ? 0 : message.getRetryCount();
        int index = retryCount > retryPeriod.length ? retryPeriod.length - 1 : retryCount;
        LocalDateTime nextRetryTime = LocalDateTime.now().plusSeconds(retryPeriod[index]);
        message.setNextRetryTime(nextRetryTime);
        message.setRetryCount(++retryCount);
    }

    int i = 1;

    /**
     * redissonLock 可重入锁
     * @throws Exception
     */
    @Override
    public void redissonLockReentrantLock() throws Exception {

        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String operationLockKey = RedisKeyConfigConst.MQ_FAIL_HANDLER;
        //并发访问，加锁控制
        RLock lock = redissonClient.getLock(operationLockKey);

        try {
            long waitTime = 10;
            long leaseTime = 30;
            boolean lockSuccessfully = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (lockSuccessfully) {

                if (i == 1) {
                    i++;
                    redissonLockReentrantLock();
                }
                //do work
                mqSendUtil.releaseLock(lock);
            } else {
                log.info("redissonLockReentrantLock - {} get lock failed", RedisKeyConfigConst.MQ_FAIL_HANDLER);
            }
        } catch (Exception ex) {
            log.error("", ex);
            lock.unlock();
            throw ex;
        }
    }


    /**
     * 解决并发下 redissonLock 释放了 事务未提交
     * 包一层确保事务
     *
     * 如果调用的方法有 @Transactional 可以将调用方法设置 @Transactional(propagation = Propagation.REQUIRES_NEW)
     */
//    @Transactional(rollbackFor = Exception.class)
    public void redissonLockReleaseTransactionalUnCommit(int i) throws InterruptedException {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String operationLockKey = RedisKeyConfigConst.MQ_FAIL_HANDLER;
        //并发访问，加锁控制
        RLock lock = redissonClient.getLock(operationLockKey);
        MqMessageService selfProxy = applicationContext.getBean(MqMessageService.class);
        try {
            long waitTime = 10;
            long leaseTime = 30;


//            // 推荐使用默认看门狗模式
//            lock.lock();
//            try {
//                // 业务逻辑
//            } finally {
//                lock.unlock();
//            }

//            短期确定任务：
//// 设置合理的自动释放时间
            // waitTime=0 表示不等待， 只会进行一次尝试获取redis 锁，不会进行后续重试
//            waitTime=0 表示 非阻塞尝试获取锁：
//            如果锁可用，立即获取并返回 true
//            如果锁被其他客户端持有，立即返回 false（不等待）
            // leaseTime 设置锁自动释放时间
//            boolean acquired = lock.tryLock(0, 30, TimeUnit.SECONDS);
//            if (lock.tryLock(0, 30, TimeUnit.SECONDS)) {
//                try {
//                    // 业务逻辑
//                } finally {
//                    lock.unlock();
//                }
//            }


//            默认30秒leaseTime，但看门狗会每10秒检查并续期
//            只要线程存活且业务未完成，锁会一直持有
//            业务完成后必须手动unlock()
            // lock.lock();
            // 明确指定leaseTime会禁用看门狗
//            lock.lock(leaseTime, TimeUnit.SECONDS);
            boolean lockSuccessfully = lock.tryLock(waitTime, TimeUnit.SECONDS);
//            boolean lockSuccessfully = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);

            if (lockSuccessfully) {
                //do work
                //进行事务操作

                // 将业务逻辑封装到事务方法中
                //self-invocation‌（自我调用）是指在一个类的方法内部直接调用同一个类中的其他方法。
                //@Transactional self-invocation (in effect, a method within the target object calling another method of the target object) does not lead to an actual transaction at runtime
//            transactionalBusinessLogic();

                //被调用方会触发事务aop, 两个方法在不同事务内
                selfProxy.selfInvocationTransactionalBusinessLogic(i);

            } else {
                log.info("redissonLockReentrantLock - {} get lock failed", RedisKeyConfigConst.MQ_FAIL_HANDLER);
            }
        } finally {
//            Thread.currentThread().interrupt();
//            lock.unlock();
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();  // 最终释放
            }
        }
    }

    //value 指定事务名称
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void MqMessageEventHandler(MqMessage mqMessage, MqMessageSourceEnum sourceEnum) throws Exception {
        log.info("MqMessageEventHandler MqMessage - {}", objectMapper.writeValueAsString(mqMessage));
        TruckOrderService truckOrderService = applicationContext.getBean(TruckOrderService.class);
        TruckOrderItemService truckOrderItemService = applicationContext.getBean(TruckOrderItemService.class);

        //getCurrentTransactionName() 当前事务的名字（默认是 null）
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
//isActualTransactionActive() 最重要、最准：真正有无事务
//        它代表：
//        当前线程的底层数据库连接是否在事务模式中
//        Spring 是否在此线程中开启了 beginTransaction
//        回滚、提交是否会生效


        //        true → 说明当前线程中存在一个活动的事务（Connection 被绑定到线程）
        boolean isActualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
//        isSynchronizationActive() = 当前线程是否启用了 Spring 的事务同步机制（允许绑定资源和事务回调）。
//        并不完全等于“是否有事务”，但通常和事务同时开启。
        boolean isSynchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();


        String lockKey = RedisKey.UPDATE_MQ_MESSAGE_INFO + ":" + mqMessage.getId();
        //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {

            //  return this.tryLock(waitTime, -1L, unit); 不指定释放时间，RedissonLock内部设置-1，
            //获取不到锁直接返回，下一个定时任务周期在处理

            if (sourceEnum.equals(MqMessageSourceEnum.JOB)) {
                //不设置获取等待，直接返回获取锁结果
                lockSuccessfully = lock.tryLock();
            } else {
                //不设置超时释放
                lockSuccessfully = lock.tryLock(RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME, TimeUnit.SECONDS);
            }
            if (!lockSuccessfully) {
                String msg = MessageFormat.format("Get lock {0} fail，wait time : {1} s", lockKey, RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME);
                log.info(msg);
                return;
            }
            log.info("update get lock {}", lockKey);
            MqMessage dbMessage = this.getById(mqMessage.getId());
            if (dbMessage.getStatus().equals(MqMessageStatus.CONSUMED.getValue())) {
                log.info("Msg id {} has been consumed", mqMessage.getId());
                return;
            }

            //do business
            try {
                //调用方法要开启新事物，不然一个事务报错：Unexpected exception occurred invoking async method: public void
                //rabbitmq 消费失败兜底重试

                if (BooleanUtils.isTrue(dbMessage.getSendMq())) {
                    switch (dbMessage.getQueue()) {
                        case RabbitMQConfig.DIRECT_QUEUE_NAME:
                            break;
                        case RabbitMQConfig.DIRECT_MQ_MESSAGE_NAME:
                            truckOrderService.expungeStaleAttachment(dbMessage.getBusinessId());
                            break;
                        default:
                            break;
                    }
                } else {
                    //事件消息
                    //调用方法要开启新事物，不然一个事务报错：Unexpected exception occurred invoking async method: public void
                    String queueTopic = "";
                    queueTopic = StringUtils.isNotEmpty(dbMessage.getTopic()) ? dbMessage.getTopic() : dbMessage.getQueue();
                    switch (queueTopic) {
                        case UtilityConst.TRUCK_ORDER_ITEM_DEBIT:
                            truckOrderItemService.debit(mqMessage);
                            break;
                        case UtilityConst.CHECK_TRUCK_ORDER_STATUS:
                            truckOrderService.synchronizeStatus(mqMessage);
                            break;
                        default:
                            break;
                    }
                }
//                Object proxyObj = AopContext.currentProxy();
//                MqMessageService mqMessageService = null;
//                if (proxyObj instanceof MqMessageService) {
//                    mqMessageService = (MqMessageService) proxyObj;
//                    //   updateStaus 事务释放 redis 锁
//                    mqMessageService.updateStaus(dbMessage.getId(), MqMessageStatus.CONSUMED);
//                }

                updateStaus(dbMessage.getId(), MqMessageStatus.CONSUMED);
            } catch (Exception ex) {
                //调用方法要开启新事物，不然一个事务报错：Unexpected exception occurred invoking async method: public void
                //这样每次处理都会打异常信息
                log.error("", ex);
                setRetryInfo(dbMessage);
                dbMessage.setStatus(MqMessageStatus.CONSUME_FAIL.getValue());
                dbMessage.setFailureReason(ex.getMessage());
                this.update(dbMessage);
            }

        } catch (Exception ex) {
            log.error("", ex);
//            throw ex;
        } finally {
            //非事务操作在此释放
//            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
            redisUtil.releaseLockAfterTransaction(lock, lockSuccessfully);
        }
    }

    /**
     * //    @Transactional(propagation = Propagation.REQUIRES_NEW)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selfInvocationTransactionalBusinessLogic(int i) {
        // 业务逻辑操作数据库


        MqMessage mqMessage = this.getById(2);
        log.info("BeforeVersion {} - {}", i, mqMessage.getVersion());
        mqMessage.setVersion(mqMessage.getVersion() + 1);
        LambdaUpdateWrapper<MqMessage> updateWrapper3 = new LambdaUpdateWrapper<>();
        updateWrapper3.set(MqMessage::getVersion, mqMessage.getVersion());
        updateWrapper3.eq(MqMessage::getId, mqMessage.getId());
        this.update(updateWrapper3);
        log.info("AfterVersion {} - {}", i, mqMessage.getVersion());

    }

    /**
     *     //@Transactional 注解时遇到 "Methods annotated with '@Transactional' must be overridable" 错误，
     *     // 这是因为 Spring 的代理机制要求被 @Transactional 注解的方法必须是可重写的。
     *
     *
     * private（私有方法不可重写）
     *final（final 方法禁止重写）
     *static（静态方法不属于实例，不存在重写概念）
     *
     *
     * protected访问修饰符报错：
     * CGLIB 代理限制：
     * 当使用 CGLIB 代理时（proxyTargetClass=true 或类没有实现接口）
     * CGLIB 无法代理 protected 方法，导致事务注解不生效
     *
     * JDK 动态代理限制：
     * 当使用基于接口的代理时
     * protected 方法通常不会在接口中声明，因此不会被代理
     */

    @Transactional(rollbackFor = Exception.class)
    public void selfInvocationTransactional() {

    }


    @Override
    public void syncMethod() {
        MqMessageService proxyMqMessageService = (MqMessageService) AopContext.currentProxy();
        MqMessageService selfProxy = applicationContext.getBean(MqMessageService.class);
        //从applicationContext 获取的bean 有完整的 Advisor ，selfProxy 是提前暴露的半成品Advisor不全
        selfProxy.asyncMethod();

//        MqMessageService service = applicationContext.getBean(MqMessageService.class);
//        service.asyncMethod();

    }

    @Async("mqFailHandlerExecutor")
    @Override
    public void asyncMethod() {
//        异步方法内不能直接使用 AopContext.currentProxy()
//        AopContext.currentProxy() 是线程绑定的，不能在异步线程中直接使用
//        // AopContext内部使用ThreadLocal存储当前代理
//        private static final ThreadLocal<Object> currentProxy = new ThreadLocal<>();
//        异步方法内需要重新获取代理，通过 ApplicationContext.getBean()

        MqMessageService selfProxy = applicationContext.getBean(MqMessageService.class);
        String threadName = Thread.currentThread().getName();
        boolean proxy = AopUtils.isAopProxy(selfProxy);

        // 1. 检查代理类型
        boolean isAopProxy = AopUtils.isAopProxy(selfProxy);
        boolean isCglibProxy = AopUtils.isCglibProxy(selfProxy);
        boolean isJdkProxy = AopUtils.isJdkDynamicProxy(selfProxy);
        //自注入selfProxy导致Spring在Bean创建过程中提前创建了一个不完整的代理（0 advisors）。
        //使用jdk 动态代理，从容器中取得的bean事务不生效： Bean 可能还在创建过程中，获取到的是"提前暴露"的半成品代理
        //这个代理没有完整的 Advisor（增强器）信息，导致 @Transactional 注解不被识别
//        selfProxy.TranMethod();
        this.selfProxy.TranMethod();
        //使用lazySelfProxy 事务不生效
//        this.lazySelfProxy.TranMethod();


//        //从applicationContext 获取的bean 有完整的 Advisor ，selfProxy 是提前暴露的半成品Advisor不全
//        MqMessageService service = applicationContext.getBean(MqMessageService.class);
//        service.TranMethod();

        //@Async 切换了线程async 线程里：ThreadLocal 是空的，永远没有 currentProxy。 AopContext.currentProxy()=null 异常
//        MqMessageService service = (MqMessageService) AopContext.currentProxy();
//
//        service.TranMethod();

        int n = 0;
        //避免在@Async方法中调用@Transactional方法
//        Exception e = transactionTemplate.execute(transactionStatus -> {
//            try {
//                selfProxy.TranMethod();
//                return null;
//            } catch (Exception ex) {
//                log.error("", ex);
//                // 如果操作失败，抛出异常，事务将回滚
//                transactionStatus.setRollbackOnly();
//                return ex;
//            }
//        });


    }

    @Transactional(rollbackFor = Exception.class)
    @DataSource(DataSourceType.SLAVE)
    @Override
    public void TranMethod() {
        //从applicationContext 获取的bean 有完整的 Advisor ，selfProxy 是提前暴露的半成品Advisor不全
        MqMessageService service = applicationContext.getBean(MqMessageService.class);
        MqMessageService selfProxy = applicationContext.getBean(MqMessageService.class);
        String threadName = Thread.currentThread().getName();
        boolean isActualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        boolean isSynchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        boolean proxy = AopUtils.isAopProxy(selfProxy);

        // 1. 检查代理类型
        boolean isAopProxy = AopUtils.isAopProxy(selfProxy);
        boolean isCglibProxy = AopUtils.isCglibProxy(selfProxy);
        boolean isJdkProxy = AopUtils.isJdkDynamicProxy(selfProxy);

        int n = 0;
    }
}
