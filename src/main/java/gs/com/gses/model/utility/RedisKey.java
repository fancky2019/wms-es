package gs.com.gses.model.utility;

public class RedisKey {
    public static final String UPDATE_INVENTORY_INFO = "redisson:updateInventoryInfo";

    public static final Integer INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME=300;
    public static final Integer INIT_INVENTORY_INFO_FROM_DB_LEASE_TIME=1500;
    public static final String INIT_INVENTORY_INFO_FROM_DB = "redisson:initInventoryInfoFromDb";


    public static final String SHIP_ORDER_COMPLETE= "shipOrderComplete:";

    public static final String CREATE_WORKING_DIRECTORY= "createWorkingDirectory:";


    //region 表锁
    public static final String UPDATE_MQ_MESSAGE_INFO = "redisson:updateMqMessage";

    //endregion

}
