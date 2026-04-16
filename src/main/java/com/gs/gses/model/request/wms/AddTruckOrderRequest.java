package com.gs.gses.model.request.wms;

import lombok.Data;

import java.util.List;

@Data
public class AddTruckOrderRequest {
    //    private final String msgId = UUID.randomUUID().toString().replaceAll("-", "");
    private Boolean async = false;
    private TruckOrderRequest truckOrderRequest;
    private List<TruckOrderItemRequest> truckOrderItemRequestList;
}
