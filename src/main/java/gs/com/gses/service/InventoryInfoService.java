package gs.com.gses.service;

import gs.com.gses.flink.DataChangeInfo;
import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.request.wms.InventoryInfoRequest;
import gs.com.gses.model.request.wms.ShipOrderItemRequest;
import gs.com.gses.model.response.PageData;

import java.util.HashMap;
import java.util.List;


public interface InventoryInfoService {

    PageData<InventoryInfo> getInventoryInfoPage(InventoryInfoRequest request) throws Exception;

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

    String allocatedReason(ShipOrderItemRequest request) throws Exception;

    void addByInventoryItemDetailInfo(Long inventoryItemDetailId) throws Exception;
}
