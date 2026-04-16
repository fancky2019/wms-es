package com.gs.gses.sse;

public class SseConst {

    /**
     * 心跳消息内容
     */
    public static final String HEARTBEAT_MESSAGE = "ping";

    //region EventName
    public static final String HEARTBEAT = "heartbeat";
    public static final String MESSAGE = "message";
    public static final String ERROR = "error";
    public static final String CONNECTED = "connect";
   //endregion
}
