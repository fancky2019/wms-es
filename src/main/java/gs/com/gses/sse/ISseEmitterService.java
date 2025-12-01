package gs.com.gses.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ISseEmitterService {

    SseEmitter createSseConnect(String userId) throws Exception;

    void pushTest(String userId);

    void closeSseConnect(String userId);

    void batchSendMessage(String msg);

    SseEmitter getSseEmitterByUserId(String userId);

    SseEmitter sendMsgToClient(String userId, String msg);
}
