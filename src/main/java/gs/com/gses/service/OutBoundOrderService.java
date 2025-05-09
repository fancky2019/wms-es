package gs.com.gses.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import gs.com.gses.model.elasticsearch.ShipOrderInfo;
import gs.com.gses.model.request.ShipOrderInfoRequest;
import gs.com.gses.model.response.PageData;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

@Service
public interface OutBoundOrderService {
    void taskComplete(long wmsTaskId) throws Exception;

    PageData<ShipOrderInfo> search(ShipOrderInfoRequest request) throws Exception;

    void addBatch() throws Exception;

    boolean deleteShipOrderInfo() ;

    void aggregationTopBucketQuery(ShipOrderInfoRequest request) throws JsonProcessingException;

    LinkedHashMap<String, BigDecimal> aggregationStatisticsQuery(ShipOrderInfoRequest request) throws Exception;

    LinkedHashMap<Object, Double> dateHistogramStatisticsQuery(ShipOrderInfoRequest request) throws JsonProcessingException;


    void initInventoryInfoFromDb()  ;


}
