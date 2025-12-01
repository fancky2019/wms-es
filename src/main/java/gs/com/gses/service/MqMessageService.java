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

import java.util.List;

/**
 * @author lirui
 * @description 针对表【MqMessage】的数据库操作Service
 * @createDate 2024-08-12 14:23:55
 */
public interface MqMessageService extends IService<MqMessage> {
    void add(MqMessage mqMessage);

    void addBatch(List<MqMessage> mqMessageList);

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
}
