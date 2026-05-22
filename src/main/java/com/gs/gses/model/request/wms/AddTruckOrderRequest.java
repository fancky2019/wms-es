package com.gs.gses.model.request.wms;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AddTruckOrderRequest {
    //    private final String msgId = UUID.randomUUID().toString().replaceAll("-", "");
    private Boolean async = false;

    @NotNull(message = "TruckOrder can not be empty")
    private TruckOrderRequest truckOrderRequest;

    @Valid  // 重要：这个注解会让Spring验证嵌套对象
    @NotNull(message = "TruckOrderItem can not be empty")
    private List<TruckOrderItemRequest> truckOrderItemRequestList;
}
