package com.gs.gses.model.utility;

public class RedisKeyConfigConst {
    public static  final  String KEY_LOCK_SUFFIX=":operation";
    public static  final  String MQ_FAIL_HANDLER="mqFailHandler:mqOperation";
    public static  final  String MQ_FAIL_HANDLER_TIME="mqFailHandler:latestExecutingTime";
    public static  final  String MQ_PUBLISH="Redisson:MqPublish";
}
