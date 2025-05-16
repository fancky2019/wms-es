package gs.com.gses.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import gs.com.gses.flink.DataChangeInfo;
import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.entity.Inventory;
import gs.com.gses.model.entity.InventoryItemDetail;
import gs.com.gses.model.request.InventoryInfoRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;


public interface InventoryInfoService {

    PageData<InventoryInfo> getInventoryInfoList(InventoryInfoRequest request) throws Exception;

    PageData<InventoryInfo> getInventoryInfoDefaultList(InventoryInfoRequest request) throws Exception;

    void initInventoryInfoFromDb();

    HashMap<Long, List<InventoryInfo>> getDefaultAllocatedInventoryInfoList(InventoryInfoRequest request) throws Exception;

    HashMap<Long, List<InventoryInfo>> getAllocatedInventoryInfoList(InventoryInfoRequest request) throws Exception;

    void updateByInventoryItemDetail(DataChangeInfo dataChangeInfo) throws JsonProcessingException;

    void updateByInventoryItem(DataChangeInfo dataChangeInfo) throws JsonProcessingException;


    void updateByInventory(DataChangeInfo dataChangeInfo) throws JsonProcessingException;





}
