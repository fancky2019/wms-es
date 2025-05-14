package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.elasticsearch.ShipOrderInfoRepository;
import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.elasticsearch.ShipOrderInfo;
import gs.com.gses.model.entity.*;
import gs.com.gses.model.request.ShipOrderInfoRequest;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.response.PageData;
import gs.com.gses.service.*;
import gs.com.gses.utility.PooledObjectFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OutBoundOrderServiceImpl implements OutBoundOrderService {
    // private static final Logger log = LoggerFactory.getLogger(OutBoundOrderServiceImpl.class);
    @Autowired
    private ShipOrderInfoRepository shipOrderInfoRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private WmsTaskService wmsTaskService;
    @Autowired
    private WmsTaskArchivedService wmsTaskArchivedService;
    @Autowired
    private ShipPickOrderItemService shipPickOrderItemService;
    @Autowired
    private ShipPickOrderService shipPickOrderService;

    @Autowired
    private ShipOrderItemService shipOrderItemService;
    @Autowired
    private ShipOrderService shipOrderService;

    @Autowired
    private ApplyShipOrderItemService applyShipOrderItemService;
    @Autowired
    private ApplyShipOrderService applyShipOrderService;

    @Autowired
    private WaveShipOrderItemRelationService waveShipOrderItemRelationService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryItemService inventoryItemService;

    @Autowired
    private InventoryItemDetailService inventoryItemDetailService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private LanewayService lanewayService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private OrgnizationService orgnizationService;

    @Autowired
    private PackageUnitService packageUnitService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void taskComplete(long wmsTaskId) throws Exception {
        WmsTaskArchived wmsTaskArchived = wmsTaskArchivedService.getById(wmsTaskId);
        WmsTask wmsTask = wmsTaskService.getById(wmsTaskId);
        if (wmsTaskArchived == null && wmsTask == null) {
            throw new Exception("taskId - " + wmsTaskId + " doesn't exist");
        }
        Long shipPickOrderItemId = 0L;
        Long inventoryItemId = 0L;
        Long materialId = 0L;
        if (wmsTask != null) {
            shipPickOrderItemId = wmsTask.getRelationOrderItemId();
            inventoryItemId = wmsTask.getInventoryItemId();
            materialId = wmsTask.getMaterialId();
        } else {
            shipPickOrderItemId = wmsTaskArchived.getRelationOrderItemId();
            inventoryItemId = wmsTaskArchived.getInventoryItemId();
            materialId = wmsTaskArchived.getMaterialId();
        }

        ShipPickOrderItem shipPickOrderItem = shipPickOrderItemService.getById(shipPickOrderItemId);
        ShipPickOrder shipPickOrder = shipPickOrderService.getById(shipPickOrderItem.getShipPickOrderId());
        ShipOrderItem shipOrderItem = shipOrderItemService.getById(shipPickOrderItem.getShipOrderItemId());
        ShipOrder shipOrder = shipOrderService.getById(shipOrderItem.getShipOrderId());
        LambdaQueryWrapper<WaveShipOrderItemRelation> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WaveShipOrderItemRelation::getShipOrderItemId, shipOrderItem.getId());
        List<WaveShipOrderItemRelation> shipOrderItemRelations = waveShipOrderItemRelationService.list(lambdaQueryWrapper);
        InventoryItem inventoryItem = inventoryItemService.getById(inventoryItemId);
        Inventory inventory = inventoryService.getById(inventoryItem.getInventoryId());
        Material material = materialService.getById(materialId);

        ShipOrderInfo shipOrderInfo = null;
        List<ShipOrderInfo> shipOrderInfoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(shipOrderItemRelations)) {
            List<Long> applyshiporderitemids = shipOrderItemRelations.stream().map(p -> p.getApplyShipOrderItemId()).collect(Collectors.toList());
            List<ApplyShipOrderItem> applyShipOrderItems = applyShipOrderItemService.listByIds(applyshiporderitemids);
            List<Long> applyShipOrderIds = applyShipOrderItems.stream().map(p -> p.getApplyShipOrderId()).collect(Collectors.toList());

            List<ApplyShipOrder> applyShipOrders = applyShipOrderService.listByIds(applyShipOrderIds);
            for (ApplyShipOrderItem applyShipOrderItem : applyShipOrderItems) {
                List<ApplyShipOrder> cuApplyShipOrderList = applyShipOrders.stream().filter(p -> p.getId().equals(applyShipOrderItem.getApplyShipOrderId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(cuApplyShipOrderList)) {
                    continue;
                }
                ApplyShipOrder applyShipOrder = cuApplyShipOrderList.get(0);
                shipOrderInfo = new ShipOrderInfo();
                shipOrderInfo.setApplyShipOrderId(applyShipOrder.getId());
                shipOrderInfo.setApplyShipOrderCode(applyShipOrder.getXCode());
                shipOrderInfo.setApplyShipOrderItemId(applyShipOrderItem.getId());
                shipOrderInfo.setApplyShipOrderItemRequiredPkgQuantity(applyShipOrderItem.getRequiredNumber());
                shipOrderInfo.setApplyShipOrderItemAllocatedPkgQuantity(applyShipOrderItem.getAllocatedNumber());

                setShipOrderInfo(shipOrderInfo, material,
                        shipOrder,
                        shipOrderItem,
                        shipPickOrder,
                        shipPickOrderItem,
                        inventory,
                        inventoryItem,
                        wmsTask, wmsTaskArchived);
                shipOrderInfoList.add(shipOrderInfo);
            }
        } else {
            shipOrderInfo = new ShipOrderInfo();
            setShipOrderInfo(shipOrderInfo, material,
                    shipOrder,
                    shipOrderItem,
                    shipPickOrder,
                    shipPickOrderItem,
                    inventory,
                    inventoryItem,
                    wmsTask, wmsTaskArchived);
            shipOrderInfoList.add(shipOrderInfo);
        }
        shipOrderInfoRepository.saveAll(shipOrderInfoList);
    }

    private void setShipOrderInfo(ShipOrderInfo shipOrderInfo, Material material,
                                  ShipOrder shipOrder,
                                  ShipOrderItem shipOrderItem,
                                  ShipPickOrder shipPickOrder,
                                  ShipPickOrderItem shipPickOrderItem,
                                  Inventory inventory,
                                  InventoryItem inventoryItem,
                                  WmsTask wmsTask, WmsTaskArchived wmsTaskArchived) {
        shipOrderInfo.setId(shipOrder.getId());
        shipOrderInfo.setShipOrderCode(shipOrder.getXCode());
        shipOrderInfo.setShipOrderItemAllocatedPkgQuantity(shipOrderItem.getAlloactedPkgQuantity());
        shipOrderInfo.setShipOrderItemRequiredPkgQuantity(shipOrderItem.getRequiredPkgQuantity());
        shipOrderInfo.setShipPickOrderId(shipPickOrder.getId());
        shipOrderInfo.setShipPickOrderItemId(shipPickOrderItem.getId());
        shipOrderInfo.setShipPickOrderItemRequiredPkgQuantity(shipPickOrderItem.getPlanPkgQuantity());
        shipOrderInfo.setShipOrderItemAllocatedPkgQuantity(shipPickOrderItem.getAllocatedPkgQuantity());
        shipOrderInfo.setInventoryId(inventory.getId());
        shipOrderInfo.setInventoryItemId(inventoryItem.getId());
        shipOrderInfo.setMaterialId(material.getId());
        shipOrderInfo.setMaterialName(material.getXName());
        shipOrderInfo.setMaterialCode(material.getXCode());
        shipOrderInfo.setSerialNo("");
        shipOrderInfo.setWorkOrderId(-1L);
        shipOrderInfo.setLocationId(-1L);
        shipOrderInfo.setTaskCompletedTime(LocalDateTime.now());
        if (wmsTask != null) {
            shipOrderInfo.setWmsTaskId(wmsTask.getId());
            shipOrderInfo.setTaskNo(wmsTask.getTaskNo());
            shipOrderInfo.setInventoryItemDetailId(wmsTask.getInventoryItemDetailId());
            shipOrderInfo.setPallet(wmsTask.getPalletCode());
            shipOrderInfo.setMovedPkgQuantity(wmsTask.getMovedPkgQuantity());
            shipOrderInfo.setMaterialId(wmsTask.getMaterialId());
        } else {
            shipOrderInfo.setWmsTaskId(wmsTaskArchived.getId());
            shipOrderInfo.setTaskNo(wmsTaskArchived.getTaskNo());
            shipOrderInfo.setInventoryItemDetailId(wmsTaskArchived.getInventoryItemDetailId());
            shipOrderInfo.setPallet(wmsTaskArchived.getPalletCode());
            shipOrderInfo.setMovedPkgQuantity(wmsTaskArchived.getMovedPkgQuantity());
            shipOrderInfo.setMaterialId(wmsTaskArchived.getMaterialId());
        }

    }


    @Override
    public PageData<ShipOrderInfo> search(ShipOrderInfoRequest request) throws Exception {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (request.getId() != null && request.getId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("id", request.getId()));
        }
        if (request.getApplyShipOrderId() != null && request.getApplyShipOrderId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("applyShipOrderId", request.getApplyShipOrderId()));
        }

        if (StringUtils.isNotEmpty(request.getApplyShipOrderCode())) {
            //guid 设置keyword  不成功 ES8
//            boolQueryBuilder.must(QueryBuilders.termQuery("guid.keyword", request.getGuid()));
            //es7
            boolQueryBuilder.must(QueryBuilders.termQuery("applyShipOrderCode", request.getApplyShipOrderCode()));
        }

        if (request.getApplyShipOrderItemId() != null && request.getApplyShipOrderItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("applyShipOrderItemId", request.getApplyShipOrderItemId()));
        }


        if (StringUtils.isNotEmpty(request.getShipOrderCode())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("shipOrderCode", request.getShipOrderCode()));
        }

        if (request.getShipOrderItemId() != null && request.getShipOrderItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("shipOrderItemId", request.getShipOrderItemId()));
        }
        if (request.getShipPickOrderId() != null && request.getShipPickOrderId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("shipPickOrderId", request.getShipPickOrderId()));
        }
        if (request.getShipPickOrderItemId() != null && request.getShipPickOrderItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("shipPickOrderItemId", request.getShipPickOrderItemId()));
        }
        if (request.getWmsTaskId() != null && request.getWmsTaskId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("wmsTaskId", request.getWmsTaskId()));
        }
        if (StringUtils.isNotEmpty(request.getTaskNo())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("taskNo", request.getTaskNo()));
        }
        if (request.getInventoryId() != null && request.getInventoryId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryId", request.getInventoryId()));
        }
        if (request.getInventoryItemId() != null && request.getInventoryItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemId", request.getInventoryItemId()));
        }
        if (request.getInventoryItemDetailId() != null && request.getInventoryItemDetailId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemDetailId", request.getInventoryItemDetailId()));
        }
        if (StringUtils.isNotEmpty(request.getPallet())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("pallet", request.getPallet()));
        }
        if (request.getMaterialId() != null && request.getMaterialId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("materialId", request.getMaterialId()));
        }
        if (StringUtils.isNotEmpty(request.getMaterialName())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("materialName", request.getMaterialName()));
        }
        if (StringUtils.isNotEmpty(request.getMaterialCode())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("materialCode", request.getMaterialCode()));
        }
        if (StringUtils.isNotEmpty(request.getSerialNo())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("serialNo", request.getSerialNo()));
        }
        if (request.getWorkOrderId() != null && request.getWorkOrderId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("workOrderId", request.getWorkOrderId()));
        }
        if (request.getLocationId() != null && request.getLocationId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("locationId", request.getLocationId()));
        }


//        List<String> includeList = new ArrayList<>();
//        includeList.add("applyShipOrderId");
//        includeList.add("materialName");
//        String[] ii=  includeList.toArray(new String[0]);
//        String[] includes = new String[]{"applyShipOrderId", "materialName"};


        List<SortBuilder<?>> sortBuilderList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(request.getSortFieldList())) {
            SortOrder sortOrder = null;
            for (Sort sort : request.getSortFieldList()) {
                switch (sort.getSortType().toLowerCase()) {
                    case "asc":
                        sortOrder = SortOrder.ASC;
                        break;
                    case "desc":
                        sortOrder = SortOrder.DESC;
                        break;
                    default:
                        throw new Exception("不支持的排序");
                }
                sortBuilderList.add(SortBuilders.fieldSort(sort.getSortField())
                        .order(sortOrder));
            }
        }

//        sortBuilderList.add(SortBuilders.fieldSort("id").order(SortOrder.DESC));
//        //taskCompletedTime task_completed_time
//        sortBuilderList.add(SortBuilders.fieldSort("taskCompletedTime").order(SortOrder.DESC));


        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                //查询条件:es支持分词查询，最小是一个词，要精确匹配分词
                //在指定字段中查找值
//                .withQuery(QueryBuilders.queryStringQuery("合肥").field("product_name").field("produce_address"))
                // .withQuery(QueryBuilders.multiMatchQuery("安徽合肥", "product_name", "produce_address"))
                //必须要加keyword，否则查不出来
                .withQuery(boolQueryBuilder)
                //SEARCH_AFTER 不用指定 from size
//                .withQuery(QueryBuilders.rangeQuery("price").from("5").to("9"))//多个条件and 的关系
                //分页：page 从0开始
                .withPageable(PageRequest.of(request.getPageIndex(), request.getPageSize()))
                //排序
//                .withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("task_completed_time").order(SortOrder.DESC))
                .withSorts(sortBuilderList)
                .withSourceFilter(new SourceFilter() {

                    //两个都不设置 返回全部


                    //返回的字段
                    @Override
                    public String[] getIncludes() {
//                        return includeList.toArray(new String[0]);
//
                        if (request.getSourceFieldList() != null) {
                            return request.getSourceFieldList().toArray(new String[0]);
                        } else {
                            return new String[0];
                        }

                    }

                    //不需要返回的字段
                    @Override
                    public String[] getExcludes() {
                        return new String[0];
                    }
                })
                //高亮字段显示
//                .withHighlightFields(new HighlightBuilder.Field("product_name"))
                .withTrackTotalHits(true)//解除最大1W条限制
                .build();
//        nativeSearchQuery.setTrackTotalHitsUpTo(10000000);
        SearchHits<ShipOrderInfo> search = elasticsearchRestTemplate.search(nativeSearchQuery, ShipOrderInfo.class);
        List<ShipOrderInfo> shipOrderInfoList = search.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());

        long count = search.getTotalHits();
        PageData<ShipOrderInfo> pageData = new PageData<>();
        pageData.setCount(count);
        pageData.setData(shipOrderInfoList);
//        elasticsearchRestTemplate.bulkUpdate();
//        elasticsearchRestTemplate.bulkIndex();
//        elasticsearchRestTemplate.delete()
//        elasticsearchRestTemplate.save()
        return pageData;
    }

    @Override
    public void addBatch() throws Exception {
        String[] names = {"上海市", "徐汇区", "漕河泾", "闵行区", "中国", "鞋子", "帽子", "太阳", "月亮",
                "初中", "高中", "小学", "大学", "佘山", "浦东区", "陆家嘴", "张江", "北京市", "黄山",
                "复旦", "同济", "海洋", "石油", "乌龟", "王八", "苹果树", "梨树", "电影", "香蕉",
                "小猫", "狼狗", "鸡肉", "牛肉", "金枪鱼",
        };
        int length = names.length;
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(1010);
        //默认8个
        config.setMaxTotal(1050);
        GenericObjectPool<ShipOrderInfo> objectPool = new GenericObjectPool<>(new PooledObjectFactoryImpl<>(ShipOrderInfo.class), config);
        ShipOrderInfo shipOrderInfo = null;
        //磁盘空间不足，只能写500W
        for (long j = 7731; j < 10000; j++) {
            List<ShipOrderInfo> shipOrderInfoList = new ArrayList<>();
            for (long m = 0; m < 1000; m++) {
                long i = j * 1000 + m;
                long id = i + 1;
                BigDecimal quantity = BigDecimal.valueOf(i);
//                shipOrderInfo = objectPool.borrowObject();
                shipOrderInfo = new ShipOrderInfo();
                shipOrderInfo.setApplyShipOrderId(id);
                shipOrderInfo.setApplyShipOrderCode("ApplyShipOrderCode" + i);
                shipOrderInfo.setApplyShipOrderItemId(id);
                shipOrderInfo.setApplyShipOrderItemRequiredPkgQuantity(quantity);
                shipOrderInfo.setApplyShipOrderItemAllocatedPkgQuantity(quantity);

                shipOrderInfo.setId(id);
                shipOrderInfo.setShipOrderCode("ShipOrderCode" + i);
                shipOrderInfo.setShipOrderItemAllocatedPkgQuantity(quantity);
                shipOrderInfo.setShipOrderItemRequiredPkgQuantity(quantity);
                shipOrderInfo.setShipPickOrderId(id);
                shipOrderInfo.setShipPickOrderItemId(id);
                shipOrderInfo.setShipPickOrderItemRequiredPkgQuantity(quantity);
                shipOrderInfo.setShipOrderItemAllocatedPkgQuantity(quantity);
                shipOrderInfo.setInventoryId(id);
                shipOrderInfo.setInventoryItemId(id);
                shipOrderInfo.setMaterialId(id);
                int index = (int) i % length;
                shipOrderInfo.setMaterialName(names[index]);
                shipOrderInfo.setMaterialCode("material" + i);
                shipOrderInfo.setSerialNo("SerialNo" + i);
                shipOrderInfo.setWorkOrderId(id);
                shipOrderInfo.setLocationId(id);
                shipOrderInfo.setTaskCompletedTime(LocalDateTime.now());

                shipOrderInfo.setWmsTaskId(id);
                shipOrderInfo.setTaskNo("WmsTask" + i);
                shipOrderInfo.setInventoryItemDetailId(id);
                shipOrderInfo.setPallet("Palletcode" + i);
                shipOrderInfo.setMovedPkgQuantity(quantity);
                shipOrderInfo.setMaterialId(id);

                shipOrderInfo.setMaterialProperty1("MaterialProperty1_" + i);
                shipOrderInfo.setMaterialProperty2("MaterialProperty2_" + i);
                shipOrderInfo.setMaterialProperty3("MaterialProperty3_" + i);
                shipOrderInfo.setMaterialProperty4("MaterialProperty4_" + i);
                shipOrderInfo.setMaterialProperty5("MaterialProperty5_" + i);
                shipOrderInfo.setMaterialProperty6("MaterialProperty6_" + i);
                shipOrderInfo.setMaterialProperty7("MaterialProperty7_" + i);
                shipOrderInfo.setMaterialProperty8("MaterialProperty8_" + i);
                shipOrderInfo.setMaterialProperty9("MaterialProperty9_" + i);
                shipOrderInfo.setMaterialProperty10("MaterialProperty10_" + i);
                shipOrderInfo.setMaterialProperty11("MaterialProperty11_" + i);
                shipOrderInfo.setMaterialProperty12("MaterialProperty12_" + i);
                shipOrderInfo.setMaterialProperty13("MaterialProperty13_" + i);
                shipOrderInfo.setMaterialProperty14("MaterialProperty14_" + i);
                shipOrderInfo.setMaterialProperty15("MaterialProperty15_" + i);
                shipOrderInfo.setMaterialProperty16("MaterialProperty16_" + i);
                shipOrderInfo.setMaterialProperty17("MaterialProperty17_" + i);
                shipOrderInfo.setMaterialProperty18("MaterialProperty18_" + i);
                shipOrderInfo.setMaterialProperty19("MaterialProperty19_" + i);
                shipOrderInfo.setMaterialProperty20("MaterialProperty20_" + i);
                shipOrderInfo.setMaterialProperty21("MaterialProperty21_" + i);
                shipOrderInfo.setMaterialProperty22("MaterialProperty22_" + i);
                shipOrderInfo.setMaterialProperty23("MaterialProperty23_" + i);
                shipOrderInfo.setMaterialProperty24("MaterialProperty24_" + i);
                shipOrderInfo.setMaterialProperty25("MaterialProperty25_" + i);
                shipOrderInfo.setMaterialProperty26("MaterialProperty26_" + i);
                shipOrderInfo.setMaterialProperty27("MaterialProperty27_" + i);
                shipOrderInfo.setMaterialProperty28("MaterialProperty28_" + i);
                shipOrderInfo.setMaterialProperty29("MaterialProperty29_" + i);
                shipOrderInfo.setMaterialProperty30("MaterialProperty30_" + i);
                shipOrderInfo.setMaterialProperty31("MaterialProperty31_" + i);
                shipOrderInfo.setMaterialProperty32("MaterialProperty32_" + i);
                shipOrderInfo.setMaterialProperty33("MaterialProperty33_" + i);
                shipOrderInfo.setMaterialProperty34("MaterialProperty34_" + i);
                shipOrderInfo.setMaterialProperty35("MaterialProperty35_" + i);
                shipOrderInfo.setMaterialProperty36("MaterialProperty36_" + i);
                shipOrderInfo.setMaterialProperty37("MaterialProperty37_" + i);
                shipOrderInfo.setMaterialProperty38("MaterialProperty38_" + i);
                shipOrderInfo.setMaterialProperty39("MaterialProperty39_" + i);
                shipOrderInfo.setMaterialProperty40("MaterialProperty40_" + i);
                shipOrderInfo.setMaterialProperty41("MaterialProperty41_" + i);
                shipOrderInfo.setMaterialProperty42("MaterialProperty42_" + i);
                shipOrderInfo.setMaterialProperty43("MaterialProperty43_" + i);
                shipOrderInfo.setMaterialProperty44("MaterialProperty44_" + i);
                shipOrderInfo.setMaterialProperty45("MaterialProperty45_" + i);
                shipOrderInfo.setMaterialProperty46("MaterialProperty46_" + i);
                shipOrderInfo.setMaterialProperty47("MaterialProperty47_" + i);
                shipOrderInfo.setMaterialProperty48("MaterialProperty48_" + i);
                shipOrderInfo.setMaterialProperty49("MaterialProperty49_" + i);
                shipOrderInfo.setMaterialProperty50("MaterialProperty50_" + i);

                shipOrderInfo.setShipOrderItemProperty1("ShipOrderItemProperty1_" + i);
                shipOrderInfo.setShipOrderItemProperty2("ShipOrderItemProperty2_" + i);
                shipOrderInfo.setShipOrderItemProperty3("ShipOrderItemProperty3_" + i);
                shipOrderInfo.setShipOrderItemProperty4("ShipOrderItemProperty4_" + i);
                shipOrderInfo.setShipOrderItemProperty5("ShipOrderItemProperty5_" + i);
                shipOrderInfo.setShipOrderItemProperty6("ShipOrderItemProperty6_" + i);
                shipOrderInfo.setShipOrderItemProperty7("ShipOrderItemProperty7_" + i);
                shipOrderInfo.setShipOrderItemProperty8("ShipOrderItemProperty8_" + i);
                shipOrderInfo.setShipOrderItemProperty9("ShipOrderItemProperty9_" + i);
                shipOrderInfo.setShipOrderItemProperty10("ShipOrderItemProperty10_" + i);
                shipOrderInfo.setShipOrderItemProperty11("ShipOrderItemProperty11_" + i);
                shipOrderInfo.setShipOrderItemProperty12("ShipOrderItemProperty12_" + i);
                shipOrderInfo.setShipOrderItemProperty13("ShipOrderItemProperty13_" + i);
                shipOrderInfo.setShipOrderItemProperty14("ShipOrderItemProperty14_" + i);
                shipOrderInfo.setShipOrderItemProperty15("ShipOrderItemProperty15_" + i);
                shipOrderInfo.setShipOrderItemProperty16("ShipOrderItemProperty16_" + i);
                shipOrderInfo.setShipOrderItemProperty17("ShipOrderItemProperty17_" + i);
                shipOrderInfo.setShipOrderItemProperty18("ShipOrderItemProperty18_" + i);
                shipOrderInfo.setShipOrderItemProperty19("ShipOrderItemProperty19_" + i);
                shipOrderInfo.setShipOrderItemProperty20("ShipOrderItemProperty20_" + i);

                shipOrderInfoList.add(shipOrderInfo);
            }

            //仓储慢,几乎比模板慢了一半
//            shipOrderInfoRepository.saveAll(shipOrderInfoList);
            elasticsearchRestTemplate.save(shipOrderInfoList);
//            for (ShipOrderInfo obj : shipOrderInfoList) {
//                objectPool.returnObject(obj);
//            }
        }
//        objectPool.clear();
    }

    @Override
    public boolean deleteShipOrderInfo() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(ShipOrderInfo.class);
        return indexOperations.delete();
    }


    //region 返回桶内的前多少条

    /**
     * 返回指定条件的，每个分组的前多少条 （返回桶内的前多少条）
     * @param request
     * @throws JsonProcessingException
     */
    @Override
    public void aggregationTopBucketQuery(ShipOrderInfoRequest request) throws JsonProcessingException {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (request.getId() != null && request.getId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("id", request.getId()));
        }
        if (request.getApplyShipOrderId() != null && request.getApplyShipOrderId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("applyShipOrderId", request.getApplyShipOrderId()));
        }

        if (StringUtils.isNotEmpty(request.getApplyShipOrderCode())) {
            //guid 设置keyword  不成功 ES8
//            boolQueryBuilder.must(QueryBuilders.termQuery("guid.keyword", request.getGuid()));
            //es7
            boolQueryBuilder.must(QueryBuilders.termQuery("applyShipOrderCode", request.getApplyShipOrderCode()));
        }

        if (request.getApplyShipOrderItemId() != null && request.getApplyShipOrderItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("applyShipOrderItemId", request.getApplyShipOrderItemId()));
        }


        if (StringUtils.isNotEmpty(request.getShipOrderCode())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("shipOrderCode", request.getShipOrderCode()));
        }

        if (request.getShipOrderItemId() != null && request.getShipOrderItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("shipOrderItemId", request.getShipOrderItemId()));
        }
        if (request.getShipPickOrderId() != null && request.getShipPickOrderId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("shipPickOrderId", request.getShipPickOrderId()));
        }
        if (request.getShipPickOrderItemId() != null && request.getShipPickOrderItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("shipPickOrderItemId", request.getShipPickOrderItemId()));
        }
        if (request.getWmsTaskId() != null && request.getWmsTaskId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("wmsTaskId", request.getWmsTaskId()));
        }
        if (StringUtils.isNotEmpty(request.getTaskNo())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("taskNo", request.getTaskNo()));
        }
        if (request.getInventoryId() != null && request.getInventoryId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryId", request.getInventoryId()));
        }
        if (request.getInventoryItemId() != null && request.getInventoryItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemId", request.getInventoryItemId()));
        }
        if (request.getInventoryItemDetailId() != null && request.getInventoryItemDetailId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemDetailId", request.getInventoryItemDetailId()));
        }
        if (StringUtils.isNotEmpty(request.getPallet())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("pallet", request.getPallet()));
        }
        if (request.getMaterialId() != null && request.getMaterialId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("materialId", request.getMaterialId()));
        }
        if (StringUtils.isNotEmpty(request.getMaterialName())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("materialName", request.getMaterialName()));
        }
        if (StringUtils.isNotEmpty(request.getMaterialCode())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("materialCode", request.getMaterialCode()));
        }
        if (StringUtils.isNotEmpty(request.getSerialNo())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("serialNo", request.getSerialNo()));
        }
        if (request.getWorkOrderId() != null && request.getWorkOrderId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("workOrderId", request.getWorkOrderId()));
        }
        if (request.getLocationId() != null && request.getLocationId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("locationId", request.getLocationId()));
        }

        //聚合查询
        //根据productGroupId进行分桶
        TermsAggregationBuilder applyShipOrderCodeAgg = AggregationBuilders.terms("agg_applyShipOrderCode").field("applyShipOrderCode").size(20);
//        TermsAggregationBuilder stateAgg = AggregationBuilders.terms("GroupId").field("pickupTime").size(Integer.MAX_VALUE);
        //  aggregationBuilder.order(BucketOrder.aggregation("applyShipOrderItemAllocatedPkgQuantity", true));//根据count数量排序


        //region spring data es 暂时未支持 multi_terms多个字段分组
        //多个字段聚合 类似 group  by field1 ,field2
        List<String> aggFields = new ArrayList<>();
        aggFields.add("shipOrderCode");
        aggFields.add("applyShipOrderCode");
        TermsAggregationBuilder multiTermsAggregationBuilder = null;
        multiTermsAggregationBuilder = multiTermsBuildTermsAggregationBuilder(aggFields);
//        MultiTermsAggregate.of()
//        MultiTermsAggregationBuilder

        TermsAggregationBuilder aggregationBuilder = null;
        for (String fieldName : aggFields) {
            if (aggregationBuilder == null) {
                aggregationBuilder = AggregationBuilders.terms(fieldName)
                        .field(fieldName);
            } else {
                aggregationBuilder.subAggregation(AggregationBuilders.terms(fieldName)
                        .field(fieldName));
            }
        }


        //        TermsAggregationBuilder aggregationBuilder = scriptBuildTermsAggregationBuilder(aggFields);
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders
                .terms("groupByFields")
                .field("applyShipOrderCode")
                .subAggregation(AggregationBuilders.terms("groupByFields2").field("shipOrderCode"));
        //endregion


        //region script
        String scriptContent = aggFields.stream().map(one -> String.format("doc['%s'].value", one))
                .collect(Collectors.joining("+'" + SEPARATOR + "'+"));

        String field1 = "applyShipOrderCode";
        String field2 = "shipOrderCode";
//        String script = "doc['" + 字段1 + "'].values +'/'+ doc['" + 字段2 + "'].values +'/'+ doc['" + 字段3 + "'].values"; // 编写script语句
        // 编写script语句
        String scriptStr = "doc['" + field1 + "'].value +'|'+ doc['" + field2 + "'].value";
        // 新建一个script对象
        Script script = new Script(scriptContent);

        List<BucketOrder> orders = new ArrayList<>();

//        BucketOrder.aggregation("applyShipOrderItemAllocatedPkgQuantity", false );//根据count数量排序

        // 创建一个聚合查询对象
        TermsAggregationBuilder scriptAggregationBuilder =
                AggregationBuilders
                        .terms("aggregation_name")
                        .script(script)
                        // 返回桶数
                        .size(2);

        //创建一个 top_hits 聚合
        TopHitsAggregationBuilder topHitsAggregation =
                AggregationBuilders.topHits("top_docs")
                        .sort("applyShipOrderItemAllocatedPkgQuantity", SortOrder.ASC)
                        .sort("shipPickOrderItemRequiredPkgQuantity", SortOrder.DESC)
                        //分页，可不要，直接指定size
                        .from(0)
                        // 设置每个桶内 返回的文档数目
                        .size(5);
        //可将 scriptAggregationBuilder 的值复制到postman 中格式化查看，就是对应的dsl 语句
        //将 top_hits 聚合添加到桶聚合中。
        scriptAggregationBuilder.subAggregation(topHitsAggregation);

        int debug = 1;
        //endregion

        //  scriptAggregationBuilder = scriptBuildTermsAggregationBuilder(aggFields);


//        TermsAggregationBuilder pKeyAggregation = AggregationBuilders.terms("pKey").field("pKey");
//        TermsAggregationBuilder runningStateAggregation =AggregationBuilders.terms("runningState").field("runningState");
//        TermsAggregationBuilder useStateAggregation =AggregationBuilders.terms("useState").field("useState");
//        BucketSortPipelineAggregationBuilder bucket = new BucketSortPipelineAggregationBuilder("page",null).from(pageNum-1).size(pageSize);
//
//        pKeyAggregation.subAggregation(runningStateAggregation);
//        pKeyAggregation.subAggregation(useStateAggregation);
//        pKeyAggregation.subAggregation(bucket);
//        queryBuilder.withAggregations(pKeyAggregation);


        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                //查询条件:es支持分词查询，最小是一个词，要精确匹配分词
                //在指定字段中查找值
//                .withQuery(QueryBuilders.queryStringQuery("合肥").field("product_name").field("produce_address"))
                // .withQuery(QueryBuilders.multiMatchQuery("安徽合肥", "product_name", "produce_address"))

                .withQuery(boolQueryBuilder)//必须要加keyword，否则查不出来
                //SEARCH_AFTER 不用指定 from size
//                .withQuery(QueryBuilders.rangeQuery("price").from("5").to("9"))//多个条件and 的关系
                //分页：page 从0开始
                //  .withPageable(PageRequest.of(request.getPageIndex(), request.getPageSize()))
                //排序
                //  .withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC))
                //高亮字段显示
//                .withHighlightFields(new HighlightBuilder.Field("product_name"))
                .withTrackTotalHits(true)//解除最大1W条限制


                //  .addAggregation(AggregationBuilders.max("maxAge").field("age"))
//                .addAggregation(multiTermsAggregationBuilder)
                .addAggregation(scriptAggregationBuilder)

                .build();
//        nativeSearchQuery.setTrackTotalHitsUpTo(10000000);
        SearchHits<ShipOrderInfo> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, ShipOrderInfo.class);
        //返回所有命中的 有不在bucket中的
        // List<ShipOrderInfo> shipOrderInfoList = searchHits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());


        AggregationsContainer<?> aggregationsContainer = searchHits.getAggregations();
        Object obj = aggregationsContainer.aggregations();
        Aggregations aggregations = (Aggregations) aggregationsContainer.aggregations();


        Map<String, Aggregation> map = aggregations.getAsMap();
        //key    ShipOrderCode91|ApplyShipOrderCode92
        HashMap<Object, Long> hashMap1 = new HashMap<>();
        HashMap<Object, List<ShipOrderInfo>> bucketHitsMap = new HashMap<>();
        for (Aggregation aggregation : map.values()) {
            Terms terms1 = aggregations.get(aggregation.getName());
            for (Terms.Bucket bucket : terms1.getBuckets()) {
                Object key = bucket.getKey();
                long count = bucket.getDocCount();
                hashMap1.put(key, count);

                Aggregations bucketAggregations = bucket.getAggregations();

                org.elasticsearch.search.SearchHits bucketSearchHits = ((ParsedTopHits) bucketAggregations.asList().get(0)).getHits();
                List<ShipOrderInfo> bucketHitList = new ArrayList<>();
                for (org.elasticsearch.search.SearchHit searchHit : bucketSearchHits.getHits()) {

//                    //字段名和对应的值
//                    Map<String, Object> smap = searchHit.getSourceAsMap();
                    String json = searchHit.getSourceAsString();
                    ShipOrderInfo shipOrderInfo = objectMapper.readValue(json, ShipOrderInfo.class);
                    bucketHitList.add(shipOrderInfo);
                }
                bucketHitsMap.put(key, bucketHitList);
            }
        }


        int m = 0;
    }

    private static String SEPARATOR = "|";

    public static TermsAggregationBuilder scriptBuildTermsAggregationBuilder(List<String> aggregationFields) {
        String content = aggregationFields.stream().map(one -> String.format("doc['%s'].value", one))
                .collect(Collectors.joining("+'" + SEPARATOR + "'+"));

        TermsAggregationBuilder builder = AggregationBuilders.terms(aggregationFields.get(0));
        builder.script(new Script(ScriptType.INLINE, "painless", content, new HashMap<String, Object>()));
        builder.size(Integer.MAX_VALUE);
        return builder;
    }

    /**
     *测试下来此方法不行 ，script 可以用
     * 直接 multi_terms 聚合
     * 构造分组聚合Builder
     */
    public static TermsAggregationBuilder multiTermsBuildTermsAggregationBuilder
    (List<String> aggregationFields) {

//        AggregationBuilder TermsAggregationBuilder

        Iterator<String> iterator = aggregationFields.iterator();
        TermsAggregationBuilder builderRoot = null;
        TermsAggregationBuilder builderCursor = null;
        while (iterator.hasNext()) {
            String field = iterator.next();
            TermsAggregationBuilder builder = AggregationBuilders.terms(aggregationFields.stream().collect(Collectors.joining())).field(field);
            builder.size(Integer.MAX_VALUE);
            if (builderRoot == null) {
                builderRoot = builder;
                builderCursor = builderRoot;
            } else {
                builderCursor.subAggregation(builder);
                builderCursor = builder;
            }
        }
        return builderRoot;
    }

    //endregion

    /**
     * 最大、最小、求和、平均
     * @param request
     * @throws JsonProcessingException
     */
    @Override
    public LinkedHashMap<String, BigDecimal> aggregationStatisticsQuery(ShipOrderInfoRequest request) throws Exception {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (request.getId() != null && request.getId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("id", request.getId()));
        }
        if (request.getApplyShipOrderId() != null && request.getApplyShipOrderId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("applyShipOrderId", request.getApplyShipOrderId()));
        }

        if (StringUtils.isNotEmpty(request.getApplyShipOrderCode())) {
            //guid 设置keyword  不成功 ES8
//            boolQueryBuilder.must(QueryBuilders.termQuery("guid.keyword", request.getGuid()));
            //es7
            boolQueryBuilder.must(QueryBuilders.termQuery("applyShipOrderCode", request.getApplyShipOrderCode()));
        }

        if (request.getApplyShipOrderItemId() != null && request.getApplyShipOrderItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("applyShipOrderItemId", request.getApplyShipOrderItemId()));
        }


        if (StringUtils.isNotEmpty(request.getShipOrderCode())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("shipOrderCode", request.getShipOrderCode()));
        }

        if (request.getShipOrderItemId() != null && request.getShipOrderItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("shipOrderItemId", request.getShipOrderItemId()));
        }
        if (request.getShipPickOrderId() != null && request.getShipPickOrderId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("shipPickOrderId", request.getShipPickOrderId()));
        }
        if (request.getShipPickOrderItemId() != null && request.getShipPickOrderItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("shipPickOrderItemId", request.getShipPickOrderItemId()));
        }
        if (request.getWmsTaskId() != null && request.getWmsTaskId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("wmsTaskId", request.getWmsTaskId()));
        }
        if (StringUtils.isNotEmpty(request.getTaskNo())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("taskNo", request.getTaskNo()));
        }
        if (request.getInventoryId() != null && request.getInventoryId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryId", request.getInventoryId()));
        }
        if (request.getInventoryItemId() != null && request.getInventoryItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemId", request.getInventoryItemId()));
        }
        if (request.getInventoryItemDetailId() != null && request.getInventoryItemDetailId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemDetailId", request.getInventoryItemDetailId()));
        }
        if (StringUtils.isNotEmpty(request.getPallet())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("pallet", request.getPallet()));
        }
        if (request.getMaterialId() != null && request.getMaterialId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("materialId", request.getMaterialId()));
        }
        if (StringUtils.isNotEmpty(request.getMaterialName())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("materialName", request.getMaterialName()));
        }
        if (StringUtils.isNotEmpty(request.getMaterialCode())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("materialCode", request.getMaterialCode()));
        }
        if (StringUtils.isNotEmpty(request.getSerialNo())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("serialNo", request.getSerialNo()));
        }
        if (request.getWorkOrderId() != null && request.getWorkOrderId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("workOrderId", request.getWorkOrderId()));
        }
        if (request.getLocationId() != null && request.getLocationId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("locationId", request.getLocationId()));
        }

        if (CollectionUtils.isEmpty(request.getBucketFieldList())) {
            throw new Exception("BucketField is empty");
        }

        if (CollectionUtils.isEmpty(request.getAggregationFieldList())) {
            throw new Exception("AggregationFieldList is empty");
        }


        List<SumAggregationBuilder> sumAggregationBuilders = new ArrayList<>();
        List<BucketOrder> bucketOrderList = new ArrayList<>();

        for (String field : request.getAggregationFieldList()) {
            String agg_name = "sum_" + field;
            SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum(agg_name).field(field);
            sumAggregationBuilders.add(sumAggregationBuilder);

//            BucketOrder.aggregation("applyShipOrderItemAllocatedPkgQuantity", false );//根据count数量排序
            //不指定聚合名称就是按照_count 统计
            BucketOrder bucketOrder = BucketOrder.aggregation(agg_name, false);
            bucketOrderList.add(bucketOrder);

        }

        //region script
        //多个字段进行分桶
        String scriptContent = request.getBucketFieldList().stream().map(one -> String.format("doc['%s'].value", one))
                .collect(Collectors.joining("+'" + SEPARATOR + "'+"));
        // 新建一个script对象
        Script script = new Script(scriptContent);
        // 创建一个聚合查询对象
        TermsAggregationBuilder scriptAggregationBuilder =
                AggregationBuilders
                        .terms("aggregation_name")
                        .script(script)
                        .order(bucketOrderList)
                        // 返回桶数
                        .size(Integer.MAX_VALUE);


        //可将 scriptAggregationBuilder 的值复制到postman 中格式化查看，就是对应的dsl 语句
        //将 top_hits 聚合添加到桶聚合中。


        //对桶进行统计
        AvgAggregationBuilder avgAggregationBuilder = AggregationBuilders.avg("avg_AllocatedPkgQuantity").field("applyShipOrderItemAllocatedPkgQuantity");
        // SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("sum_AllocatedPkgQuantity").field("applyShipOrderItemAllocatedPkgQuantity");
        MaxAggregationBuilder maxAggregationBuilder = AggregationBuilders.max("max_AllocatedPkgQuantity").field("applyShipOrderItemAllocatedPkgQuantity");
        MinAggregationBuilder minAggregationBuilder = AggregationBuilders.min("min_AllocatedPkgQuantity").field("applyShipOrderItemAllocatedPkgQuantity");
//        //直方图  根据天统计  单独写一个
//        DateHistogramAggregationBuilder dateHistogramAggregationBuilder =
//                AggregationBuilders.dateHistogram("agg_taskCompletedTime").field("taskCompletedTime")
////        DateHistogramInterval.MONTH(1)
//                        .calendarInterval(DateHistogramInterval.days(1)).format("yyyy-MM-dd");
//.format("yyyy-MM-dd HH:mm").minDocCount(0)
//                .extendedBounds(new LongBounds(startTime, endTime)).timeZone(ZoneId.of("Asia/Shanghai"));

        for (SumAggregationBuilder builder : sumAggregationBuilders) {
            scriptAggregationBuilder.subAggregation(builder);
        }
        scriptAggregationBuilder.subAggregation(avgAggregationBuilder);

        scriptAggregationBuilder.subAggregation(maxAggregationBuilder);
        scriptAggregationBuilder.subAggregation(minAggregationBuilder);
//        applyShipOrderCodeAgg.subAggregation(dateHistogramAggregationBuilder);

        int n = 0;


        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                //查询条件:es支持分词查询，最小是一个词，要精确匹配分词
                //在指定字段中查找值
//                .withQuery(QueryBuilders.queryStringQuery("合肥").field("product_name").field("produce_address"))
                // .withQuery(QueryBuilders.multiMatchQuery("安徽合肥", "product_name", "produce_address"))

                .withQuery(boolQueryBuilder)//必须要加keyword，否则查不出来
                //SEARCH_AFTER 不用指定 from size
//                .withQuery(QueryBuilders.rangeQuery("price").from("5").to("9"))//多个条件and 的关系
                //分页：page 从0开始
                //  .withPageable(PageRequest.of(request.getPageIndex(), request.getPageSize()))
                //排序
                //  .withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC))
                //高亮字段显示
//                .withHighlightFields(new HighlightBuilder.Field("product_name"))
                .withTrackTotalHits(true)//解除最大1W条限制
                //  .addAggregation(AggregationBuilders.max("maxAge").field("age"))
//                .addAggregation(multiTermsAggregationBuilder)
                .addAggregation(scriptAggregationBuilder)
                .build();
//        nativeSearchQuery.setTrackTotalHitsUpTo(10000000);
        SearchHits<ShipOrderInfo> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, ShipOrderInfo.class);
        //返回所有命中的 有不在bucket中的
        // List<ShipOrderInfo> shipOrderInfoList = searchHits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());


        AggregationsContainer<?> aggregationsContainer = searchHits.getAggregations();
        Object obj = aggregationsContainer.aggregations();
        Aggregations aggregations = (Aggregations) aggregationsContainer.aggregations();


        Map<String, Aggregation> map = aggregations.getAsMap();
        //key    ShipOrderCode91|ApplyShipOrderCode92
        HashMap<Object, Long> countHashMap = new HashMap<>();
        HashMap<Object, Double> sumHashMap = new HashMap<>();
        HashMap<Object, Double> avgHashMap = new HashMap<>();
        LinkedHashMap<String, BigDecimal> resultMap = new LinkedHashMap<>();
        for (Aggregation aggregation : map.values()) {
            Terms terms1 = aggregations.get(aggregation.getName());
            for (Terms.Bucket bucket : terms1.getBuckets()) {
                Object key = bucket.getKey();
                long count = bucket.getDocCount();
                countHashMap.put(key, count);

                Aggregations bucketAggregations = bucket.getAggregations();
                List<Aggregation> bucketAggregationsList = bucketAggregations.asList();
//                //要和子聚合保持一致
//                List<Avg> bucketAggregationsList =
//                        bucketAggregations.asList().stream().map(p->(Avg)p).collect(Collectors.toList());
//


                for (int i = 0; i < bucketAggregationsList.size(); i++) {
                    Aggregation abucketAggregation = bucketAggregationsList.get(i);
                    String aggregationName = abucketAggregation.getName();

                    ParsedSingleValueNumericMetricsAggregation parsed = (ParsedSingleValueNumericMetricsAggregation) bucketAggregationsList.get(i);
                    BigDecimal bigDecimal = new BigDecimal(parsed.getValueAsString());
                    resultMap.put(parsed.getName().replace("sum_", ""), bigDecimal);
                    switch (aggregationName) {
                        case "avgAggregationBuilder":
                            double avg = ((ParsedAvg) abucketAggregation).getValue();
                            avgHashMap.put(key, avg);
                            break;
                        case "sumAggregationBuilder":
                            double sum = ((ParsedSum) abucketAggregation).getValue();
                            sumHashMap.put(key, sum);
                            break;
                        case "maxAggregationBuilder":
                            double max = ((ParsedMax) abucketAggregation).getValue();
                            break;
                        case "minAggregationBuilder":
                            double min = ((ParsedMin) abucketAggregation).getValue();
                            break;
                        case "agg_taskCompletedTime":
                            ParsedDateHistogram dateHistogram = (ParsedDateHistogram) abucketAggregation;
                            List<?> dateHistogramBuckets = dateHistogram.getBuckets();
                            for (int t = 0; t < dateHistogramBuckets.size(); t++) {
                                Object parsedBucketObj = dateHistogramBuckets.get(t);
                                ParsedDateHistogram.ParsedBucket parsedBucket = (ParsedDateHistogram.ParsedBucket) parsedBucketObj;
                                parsedBucket.getKey();
                                String parsedBucketKey = parsedBucket.getKeyAsString();
                                int mm = 0;
                            }
                            break;
                        default:
                            break;
                    }
                }
//               String aggregationName=    bucketAggregations.asList().get(0).getName();


                //       //直方图  根据天统计
//                double sum = ((ParsedSum) bucketAggregations.asList().get(0)).getValue();
//                double avg = ((ParsedAvg) bucketAggregations.asList().get(1)).getValue();
//                double max = ((ParsedMax) bucketAggregations.asList().get(2)).getValue();
//                double min = ((ParsedMin) bucketAggregations.asList().get(3)).getValue();
//                sumHashMap.put(key, sum);
//                avgHashMap.put(key, avg);

            }
        }


        int m = 0;
        return resultMap;
    }


    /**
     * 直方图
     * @param request
     * @throws JsonProcessingException
     */
    @Override
    public LinkedHashMap<Object, Double> dateHistogramStatisticsQuery(ShipOrderInfoRequest request) throws JsonProcessingException {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (request.getId() != null && request.getId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("id", request.getId()));
        }
        if (request.getApplyShipOrderId() != null && request.getApplyShipOrderId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("applyShipOrderId", request.getApplyShipOrderId()));
        }

        if (StringUtils.isNotEmpty(request.getApplyShipOrderCode())) {
            //guid 设置keyword  不成功 ES8
//            boolQueryBuilder.must(QueryBuilders.termQuery("guid.keyword", request.getGuid()));
            //es7
            boolQueryBuilder.must(QueryBuilders.termQuery("applyShipOrderCode", request.getApplyShipOrderCode()));
        }

        if (request.getApplyShipOrderItemId() != null && request.getApplyShipOrderItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("applyShipOrderItemId", request.getApplyShipOrderItemId()));
        }


        if (StringUtils.isNotEmpty(request.getShipOrderCode())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("shipOrderCode", request.getShipOrderCode()));
        }

        if (request.getShipOrderItemId() != null && request.getShipOrderItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("shipOrderItemId", request.getShipOrderItemId()));
        }
        if (request.getShipPickOrderId() != null && request.getShipPickOrderId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("shipPickOrderId", request.getShipPickOrderId()));
        }
        if (request.getShipPickOrderItemId() != null && request.getShipPickOrderItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("shipPickOrderItemId", request.getShipPickOrderItemId()));
        }
        if (request.getWmsTaskId() != null && request.getWmsTaskId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("wmsTaskId", request.getWmsTaskId()));
        }
        if (StringUtils.isNotEmpty(request.getTaskNo())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("taskNo", request.getTaskNo()));
        }
        if (request.getInventoryId() != null && request.getInventoryId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryId", request.getInventoryId()));
        }
        if (request.getInventoryItemId() != null && request.getInventoryItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemId", request.getInventoryItemId()));
        }
        if (request.getInventoryItemDetailId() != null && request.getInventoryItemDetailId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemDetailId", request.getInventoryItemDetailId()));
        }
        if (StringUtils.isNotEmpty(request.getPallet())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("pallet", request.getPallet()));
        }
        if (request.getMaterialId() != null && request.getMaterialId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("materialId", request.getMaterialId()));
        }
        if (StringUtils.isNotEmpty(request.getMaterialName())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("materialName", request.getMaterialName()));
        }
        if (StringUtils.isNotEmpty(request.getMaterialCode())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("materialCode", request.getMaterialCode()));
        }
        if (StringUtils.isNotEmpty(request.getSerialNo())) {

            boolQueryBuilder.must(QueryBuilders.termQuery("serialNo", request.getSerialNo()));
        }
        if (request.getWorkOrderId() != null && request.getWorkOrderId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("workOrderId", request.getWorkOrderId()));
        }
        if (request.getLocationId() != null && request.getLocationId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("locationId", request.getLocationId()));
        }

        //聚合查询
        //根据productGroupId进行分桶


        //        //直方图  根据天统计  单独写一个
//        DateHistogramAggregationBuilder dateHistogramAggregationBuilder =
//                AggregationBuilders.dateHistogram("agg_taskCompletedTime").field("taskCompletedTime")
////        DateHistogramInterval.MONTH(1)
//                        .calendarInterval(DateHistogramInterval.days(1)).format("yyyy-MM-dd");
//.format("yyyy-MM-dd HH:mm").minDocCount(0)
//                .extendedBounds(new LongBounds(startTime, endTime)).timeZone(ZoneId.of("Asia/Shanghai"));


        //根据productGroupId进行分桶
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder =
                AggregationBuilders.dateHistogram("agg_taskCompletedTime").field("taskCompletedTime")
//        DateHistogramInterval.MONTH(1)
                        .calendarInterval(DateHistogramInterval.days(1)).offset("-8h").format("yyyy-MM-dd").minDocCount(0);
        ;
//.format("yyyy-MM-dd HH:mm").minDocCount(0)
//                .extendedBounds(new LongBounds(startTime, endTime)).timeZone(ZoneId.of("Asia/Shanghai"));

        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.sum("sum_AllocatedPkgQuantity").field("applyShipOrderItemAllocatedPkgQuantity"));

        //        //创建一个 top_hits 聚合 排序
//        TopHitsAggregationBuilder topHitsAggregation =
//                AggregationBuilders.topHits("top_docs")
//                        .sort("taskCompletedTime", SortOrder.ASC);
//
//
//        //可将 scriptAggregationBuilder 的值复制到postman 中格式化查看，就是对应的dsl 语句
//        //将 top_hits 聚合添加到桶聚合中。
//        dateHistogramAggregationBuilder.subAggregation(topHitsAggregation);


        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                //查询条件:es支持分词查询，最小是一个词，要精确匹配分词
                //在指定字段中查找值
//                .withQuery(QueryBuilders.queryStringQuery("合肥").field("product_name").field("produce_address"))
                // .withQuery(QueryBuilders.multiMatchQuery("安徽合肥", "product_name", "produce_address"))

                .withQuery(boolQueryBuilder)//必须要加keyword，否则查不出来
                //SEARCH_AFTER 不用指定 from size
//                .withQuery(QueryBuilders.rangeQuery("price").from("5").to("9"))//多个条件and 的关系
                //分页：page 从0开始
                //  .withPageable(PageRequest.of(request.getPageIndex(), request.getPageSize()))
                //排序
                //  .withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC))
                //高亮字段显示
//                .withHighlightFields(new HighlightBuilder.Field("product_name"))
                .withTrackTotalHits(true)//解除最大1W条限制
                //  .addAggregation(AggregationBuilders.max("maxAge").field("age"))
//                .addAggregation(multiTermsAggregationBuilder)
                .addAggregation(dateHistogramAggregationBuilder)
                .build();
//        nativeSearchQuery.setTrackTotalHitsUpTo(10000000);
        SearchHits<ShipOrderInfo> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, ShipOrderInfo.class);
        //返回所有命中的 有不在bucket中的
        // List<ShipOrderInfo> shipOrderInfoList = searchHits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());


        AggregationsContainer<?> aggregationsContainer = searchHits.getAggregations();
        Object obj = aggregationsContainer.aggregations();
        Aggregations aggregations = (Aggregations) aggregationsContainer.aggregations();


        Map<String, Aggregation> map = aggregations.getAsMap();


        ParsedDateHistogram terms = (ParsedDateHistogram) map.get("agg_taskCompletedTime");
        //HashMap key  会乱序  key 会乱
        LinkedHashMap<Object, Double> dateHashMap = new LinkedHashMap<>();
        for (Histogram.Bucket bucket : terms.getBuckets()) {

            String key = bucket.getKeyAsString();
            if (bucket.getAggregations().get("sum_AllocatedPkgQuantity") != null) {
                ParsedSum sum = bucket.getAggregations().get("sum_AllocatedPkgQuantity");
                dateHashMap.put(key, sum.getValue());
            }

        }


        return dateHashMap;
    }




//    /**
//     * 创建索引及映射
//     * @return
//     */
//    public <T> Boolean createIndexAndMapping(Class<T> clas) {
//
//
//        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(clas);
//        //创建索引
//        boolean result = indexOperations.create();
//        if (result) {
//            //生成映射
//            Document mapping = indexOperations.createMapping();
//            //推送映射
//            return indexOperations.putMapping(mapping);
//        } else {
//            return result;
//        }
//    }


}
