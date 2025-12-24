package gs.com.gses.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import gs.com.gses.model.entity.MqMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.enums.MqMessageSourceEnum;
import gs.com.gses.model.enums.MqMessageStatus;
import gs.com.gses.model.request.wms.MqMessageRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.MqMessageResponse;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Inherited;
import java.util.List;

/**
 * @author lirui
 * @description 针对表【MqMessage】的数据库操作Service
 * @createDate 2024-08-12 14:23:55
 */
public interface MqMessageService extends IService<MqMessage> {
    MqMessage add(MqMessage mqMessage);

    List<MqMessage>  addBatch(List<MqMessage> mqMessageList);

    MqMessage addMessage(MqMessageRequest mqMessage) throws Exception;

    List<MqMessage>  addMessageBatch(List<MqMessageRequest> requestList) throws Exception;

    void delete(MqMessage mqMessage);

    void update(MqMessage mqMessage) throws Exception;

    void updateByMsgId(String msgId, int status) throws Exception;

    void updateByMsgId(String msgId, int status, String queue) throws Exception;

    void updateStaus(long mqMessageId, MqMessageStatus status) throws Exception;

    PageData<MqMessageResponse> list(MqMessageRequest mqMessage) throws Exception;

    void page(MqMessageRequest mqMessage);

    void count(MqMessageRequest mqMessage);

    /**
     * 失败处理
     */
    void mqOperation();

    void rePublish(List<MqMessage> mqMessageList);

    void reConsume(List<MqMessage> mqMessageList) throws Exception;

    void redissonLockReentrantLock() throws Exception;

    void selfInvocationTransactionalBusinessLogic(int i);

    void redissonLockReleaseTransactionalUnCommit(int i) throws InterruptedException;

    void MqMessageEventHandler(MqMessage message, MqMessageSourceEnum sourceEnum) throws Exception;


    void syncMethod();

    /*
    @Async @Transactional 注解的推荐用法是：加在具体实现类的方法上，而不是接口上
    代理机制限制：
    如果使用 JDK 动态代理，接口上的注解可以被读取
    但如果使用 CGLIB 代理（类代理），接口上的注解可能无法被识别
    Java 注解继承：
    接口上的 @Transactional 默认不会被实现类继承（除非使用 @Inherited，但该注解对接口无效）
    配置混乱：
    如果接口和实现类都有 @Transactional，以实现类的为准，容易造成混淆
//    如果接口和实现类都有 @Transactional，以实现类的为准，容易造成混淆
//
*/
//    @Async("mqFailHandlerExecutor")
    void asyncMethod();
//    @Transactional(rollbackFor = Exception.class)
    void TranMethod();
}
