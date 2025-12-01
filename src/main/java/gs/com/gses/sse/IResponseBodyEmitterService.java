package gs.com.gses.sse;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public interface IResponseBodyEmitterService {
    ResponseBodyEmitter createResponseBodyEmitterConnect(String userId) throws Exception;
    void closeResponseBodyEmitterConnect(String userId);
    void batchSendMessage(String msg);
    ResponseBodyEmitter getResponseBodyEmitterByUserId(String userId);
    ResponseBodyEmitter sendMsgToClient(String userId, String msg);
}
