package gs.com.gses.service.impl;

import java.time.LocalDateTime;


public class UtilityConst {

    public  static String token="";

    public final static String TRUCK_ORDER_COMPLETE_TOPIC = "GS/WMS/TruckOrder/Complete";
//    public final static String TRUCK_ORDER_COMPLETE_TOPIC = "TruckOrderCompleteTopic";

    public final static String PRINT_PROCESS_INBOUND_CODE = "printProcessInBoundCode";

    public final static String TRUCK_ORDER_ITEM_DEBIT = "truckOrderItemDebit";
    public final static String CHECK_TRUCK_ORDER_STATUS = "checkTruckOrderStatus";


    private final static LocalDateTime MQ_FAIL_HANDLER_TIME=null;

}
