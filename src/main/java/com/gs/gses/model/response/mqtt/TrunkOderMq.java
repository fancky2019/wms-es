package com.gs.gses.model.response.mqtt;

import com.gs.gses.model.response.wms.TruckOrderItemResponse;
import com.gs.gses.model.response.wms.TruckOrderResponse;
import lombok.Data;

import java.util.List;

@Data
public class TrunkOderMq {
    private String msgId;
    private List<TruckOrderResponse> truckOrderResponseList;
    private List<TruckOrderItemResponse> truckOrderItemResponseList;
}
