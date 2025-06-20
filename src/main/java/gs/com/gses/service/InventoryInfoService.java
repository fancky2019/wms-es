package gs.com.gses.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import gs.com.gses.flink.DataChangeInfo;
import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.request.wms.InventoryInfoRequest;
import gs.com.gses.model.response.PageData;

import java.util.HashMap;
import java.util.List;


public interface InventoryInfoService {

    PageData<InventoryInfo> getInventoryInfoList(InventoryInfoRequest request) throws Exception;

    PageData<InventoryInfo> getInventoryInfoDefaultList(InventoryInfoRequest request) throws Exception;

    void initInventoryInfoFromDb() throws InterruptedException;

    HashMap<Long, List<InventoryInfo>> getDefaultAllocatedInventoryInfoList(InventoryInfoRequest request) throws Exception;

    HashMap<Long, List<InventoryInfo>> getAllocatedInventoryInfoList(InventoryInfoRequest request) throws Exception;
    void sink(DataChangeInfo dataChangeInfo) throws Exception ;
    void updateByInventoryItemDetail(DataChangeInfo dataChangeInfo) throws Exception;

    void updateByInventoryItem(DataChangeInfo dataChangeInfo) throws Exception;

    void updateByInventory(DataChangeInfo dataChangeInfo) throws Exception;

    void updateByLocation(DataChangeInfo dataChangeInfo) throws Exception;

    void updateByLaneway(DataChangeInfo dataChangeInfo) throws Exception;

    void test();


    void updateByInventoryItemDetailDb(Long id) throws InterruptedException;
    void updateByInventoryItemDb(Long id) throws InterruptedException;
    void updateByInventoryDb(Long id) throws InterruptedException;




}
