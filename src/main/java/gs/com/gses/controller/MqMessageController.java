package gs.com.gses.controller;

import gs.com.gses.model.response.MessageResult;
import gs.com.gses.rabbitMQ.monitor.QueueStats;
import gs.com.gses.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/mqMessage")
public class MqMessageController {

    @Autowired
    private MqMessageService mqMessageService;

    @PostMapping(value = "/transactionRepeatReadLock")
    public MessageResult<Void> transactionRepeatReadLock() throws Exception {
        boolean proxy = AopUtils.isAopProxy(mqMessageService);

        // 1. 检查代理类型
        boolean isAopProxy = AopUtils.isAopProxy(mqMessageService);
        boolean isCglibProxy = AopUtils.isCglibProxy(mqMessageService);
        boolean isJdkProxy = AopUtils.isJdkDynamicProxy(mqMessageService);
        //看代理是jdk 还是cglib.jdk 代理service层方法上的事务会失效，要把service设置成cglib代理 @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
        //    CGLIB:    gs.com.gses.service.impl.TruckOrderItemServiceImpl$$EnhancerBySpringCGLIB$$aaef177e
//    JDK:    MqMessageService：class = com.sun.proxy.$Proxy274
        log.info("class = {}", mqMessageService.getClass());
        this.mqMessageService.transactionRepeatReadLock();
        return MessageResult.success();
    }
}
