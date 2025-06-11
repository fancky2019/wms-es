package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.elasticsearch.ShipOrderInfoRepository;
import gs.com.gses.flink.DataChangeInfo;
import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.entity.*;
import gs.com.gses.model.request.wms.InventoryInfoRequest;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.response.PageData;
import gs.com.gses.service.*;
import gs.com.gses.utility.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import org.elasticsearch.search.aggregations.metrics.TopHitsAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InventoryInfoServiceImpl implements InventoryInfoService {


    @Autowired
    private ShipOrderInfoRepository shipOrderInfoRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

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

    @Qualifier("upperObjectMapper")  // 明确指定名称
    @Autowired
    private ObjectMapper upperObjectMapper;


    @Autowired
    private BasicInfoCacheService basicInfoCacheService;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisUtil redisUtil;


    private static String SEPARATOR = "|";

    public static LocalDateTime INIT_INVENTORY_TIME = null;

    public static final String LOCK_KEY = "redisson:updateInventoryInfo";

    private final static int WAIT_TIME = 300000;
    private final static int LEASE_TIME = 300000;


    @Override
    public void initInventoryInfoFromDb() throws InterruptedException {
        INIT_INVENTORY_TIME = LocalDateTime.now();
        String initInventoryTimeStr = INIT_INVENTORY_TIME.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        redisTemplate.opsForValue().set("InitInventoryTime", initInventoryTimeStr);
        StopWatch stopWatch = new StopWatch("initInventoryInfoFromDb");
        stopWatch.start("initInventoryInfoFromDb");

        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(InventoryInfo.class);

        // 删除索引
        if (indexOperations.exists()) {
            indexOperations.delete();

        }

        createIndexAndMapping(InventoryInfo.class);

        Map<String, Location> locationMap = redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.locationPrefix);
        Map<String, Laneway> lanewayMap = redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.lanewayPrefix);
        Map<String, Zone> zoneMap = redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.zonePrefix);
        Map<String, Warehouse> warehouseMap = redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.warehousePrefix);
        Map<String, Orgnization> orgnizationMap = redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.orgnizationPrefix);

        Map<String, Material> materialMap = redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.materialPrefix);
        Map<String, PackageUnit> packageUnitMap = redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.packageUnitPrefix);

        long count = this.inventoryItemDetailService.count();
        int step = 1000;
        long times = count / step;
        long left = count / step;
        if (left > 0) {
            times++;
        }
        //current :pageIndex  ,size:pageSize
        Page<InventoryItemDetail> page = new Page<>(1, step);
        long pageIndex = 0L;
        long totalIndexSize = 0L;
        while (times > 0) {
            times--;
            page.setCurrent(++pageIndex);
            page.setSearchCount(false);
            //page 内部调用selectPage
            IPage<InventoryItemDetail> inventoryItemDetailPage = this.inventoryItemDetailService.page(page);
            Integer size = addByInventoryItemDetailInfo(inventoryItemDetailPage.getRecords(),
                    materialMap,
                    packageUnitMap,
                    locationMap,
                    lanewayMap,
                    zoneMap,
                    warehouseMap,
                    orgnizationMap
            );
            totalIndexSize += size;
            log.info("totalIndexSize - {} inventory_info - {}", totalIndexSize, size);
        }

        stopWatch.stop();
//        stopWatch.start("BatchInsert_Trace2");
        long mills = stopWatch.getTotalTimeMillis();
        log.info("initInventoryInfoFromDb complete {} ms", mills);
        log.info("inventory_info total:" + totalIndexSize);
    }


    private Integer addByInventoryItemDetailInfo(List<InventoryItemDetail> inventoryItemDetailList,
                                                 Map<String, Material> materialMap,
                                                 Map<String, PackageUnit> packageUnitMap,
                                                 Map<String, Location> locationMap,
                                                 Map<String, Laneway> lanewayMap,
                                                 Map<String, Zone> zoneMap,
                                                 Map<String, Warehouse> warehouseMap,
                                                 Map<String, Orgnization> orgnizationMap) throws InterruptedException {
        List<InventoryInfo> inventoryInfos = new ArrayList<>();
        List<Long> inventoryItemIdList = inventoryItemDetailList.stream().map(p -> p.getInventoryItemId()).distinct().collect(Collectors.toList());
        List<InventoryItem> inventoryItemList = this.inventoryItemService.listByIds(inventoryItemIdList);
        List<Long> inventoryIdList = inventoryItemList.stream().map(p -> p.getInventoryId()).distinct().collect(Collectors.toList());
        List<Inventory> inventoryList = this.inventoryService.listByIds(inventoryIdList);


        if (inventoryList.size() == 1) {
            locationMap = new HashMap<>();
            lanewayMap = new HashMap<>();
            zoneMap = new HashMap<>();
            materialMap = new HashMap<>();
            warehouseMap = new HashMap<>();
            orgnizationMap = new HashMap<>();
            packageUnitMap = new HashMap<>();


            Inventory inventory = inventoryList.get(0);
            Location location = (Location) redisTemplate.opsForHash().get(BasicInfoCacheServiceImpl.locationPrefix, inventory.getLocationId().toString());
            if (location == null) {
                location = this.basicInfoCacheService.loadFromDbLocation(inventory.getLocationId());
            }
            locationMap.put(location.getId().toString(), location);


            Laneway laneway = (Laneway) redisTemplate.opsForHash().get(BasicInfoCacheServiceImpl.locationPrefix, location.getLanewayId().toString());
            if (laneway == null) {
                laneway = this.basicInfoCacheService.loadFromDbLaneway(location.getLanewayId());
            }
            lanewayMap.put(laneway.getId().toString(), laneway);


            Zone zone = (Zone) redisTemplate.opsForHash().get(BasicInfoCacheServiceImpl.locationPrefix, laneway.getZoneId().toString());
            if (zone == null) {
                zone = this.basicInfoCacheService.loadFromDbZone(laneway.getZoneId());
            }
            zoneMap.put(zone.getId().toString(), zone);

            Warehouse warehouse = (Warehouse) redisTemplate.opsForHash().get(BasicInfoCacheServiceImpl.locationPrefix, zone.getWarehouseId().toString());
            if (warehouse == null) {
                warehouse = this.basicInfoCacheService.loadFromDbWarehouse(zone.getWarehouseId());
            }
            warehouseMap.put(warehouse.getId().toString(), warehouse);


            List<Long> organiztionSupplierIdList = inventoryItemList.stream().filter(p -> p.getOrganiztionSupplierId() != null).map(p -> p.getOrganiztionSupplierId()).distinct().collect(Collectors.toList());
            List<Long> organiztionIdList = inventoryItemList.stream().filter(p -> p.getOrganiztionSupplierId() != null).map(p -> p.getOrganiztionId()).distinct().collect(Collectors.toList());

            List<Long> allOrganiztionIdList = new ArrayList<>();
            allOrganiztionIdList.addAll(organiztionSupplierIdList);
            allOrganiztionIdList.addAll(organiztionIdList);
            if (!allOrganiztionIdList.isEmpty()) {
                List<String> allOrganiztionIdStrList = allOrganiztionIdList.stream().map(p -> p.toString()).collect(Collectors.toList());
                List<Orgnization> orgnizationList = redisTemplate.opsForHash().multiGet(BasicInfoCacheServiceImpl.orgnizationPrefix, allOrganiztionIdStrList);
                orgnizationMap = orgnizationList.stream().collect(Collectors.toMap(p -> p.getId().toString(), item -> item));
            }
            List<String> materialIdList = inventoryItemDetailList.stream().map(p -> p.getMaterialId().toString()).distinct().collect(Collectors.toList());
            materialMap = redisUtil.getHashEntries(BasicInfoCacheServiceImpl.materialPrefix, materialIdList);

            List<String> packageUnitMapIdList = inventoryItemDetailList.stream().map(p -> p.getPackageUnitId().toString()).distinct().collect(Collectors.toList());
            packageUnitMap = redisUtil.getHashEntries(BasicInfoCacheServiceImpl.packageUnitPrefix, packageUnitMapIdList);
        }
        if (materialMap == null || materialMap.keySet().isEmpty()) {
            log.info("addByInventoryItemDetailInfo fail,materialMap is empty");
            return 0;
        }


        InventoryInfo inventoryInfo = null;
        List<Long> materialIdList = inventoryItemDetailList.stream().map(p -> p.getMaterialId()).distinct().collect(Collectors.toList());
        for (InventoryItemDetail inventoryItemDetail : inventoryItemDetailList) {
            inventoryInfo = new InventoryInfo();
            InventoryItem inventoryItem = inventoryItemList.stream().filter(p -> p.getId().equals(inventoryItemDetail.getInventoryItemId())).findFirst().orElse(null);
            Inventory inventory = inventoryList.stream().filter(p -> p.getId().equals(inventoryItem.getInventoryId())).findFirst().orElse(null);
            Location location = locationMap.get(inventory.getLocationId().toString());
            Laneway laneway = lanewayMap.get(location.getLanewayId().toString());
            Zone zone = zoneMap.get(laneway.getZoneId().toString());
            Material material = materialMap.get(inventoryItemDetail.getMaterialId().toString());

//            Material material = (Material) redisTemplate.opsForHash().get(BasicInfoCacheServiceImpl.materialPrefix, inventoryItemDetail.getMaterialId().toString());
            if (material == null) {
                material = this.basicInfoCacheService.loadFromDbMaterial(inventoryItemDetail.getMaterialId());
            }

            Warehouse warehouse = warehouseMap.get(inventory.getWhid().toString());

            if (warehouse != null) {
                inventoryInfo.setWhid(warehouse.getId());
                inventoryInfo.setWhCode(warehouse.getXCode());
            }

            if (location == null) {
                log.error("ignoreIncrement location is null " + inventory.getLocationId().toString());
                continue;
            }
            inventoryInfo.setLocationId(location.getId());
            inventoryInfo.setLocationCode(location.getXCode());
            inventoryInfo.setLocationXStatus(location.getXStatus());
            inventoryInfo.setLocationIsLocked(location.getIsLocked());
            inventoryInfo.setForbidOutbound(location.getForbidOutbound());
            inventoryInfo.setIsCountLocked(location.getIsCountLocked());
            inventoryInfo.setLocationXType(location.getXType());


            if (laneway != null) {
                inventoryInfo.setLanewayId(laneway.getId());
                inventoryInfo.setLanewayCode(laneway.getXCode());
                inventoryInfo.setLanewayXStatus(laneway.getXStatus());
            }

            if (zone != null) {
                inventoryInfo.setZoneId(zone.getId());
                inventoryInfo.setZoneCode(zone.getXCode());
            }


            //inventory
            inventoryInfo.setInventoryId(inventory.getId());
            inventoryInfo.setPallet(inventory.getPallet());
            inventoryInfo.setInventoryAllocatedSmallUnitQuantity(inventory.getAllocatedSmallUnitQuantity());
            inventoryInfo.setInventoryAllocatedPackageQuantity(inventory.getAllocatedPackageQuantity());
            inventoryInfo.setInventoryQCStatus(inventory.getQCStatus());
            inventoryInfo.setInventoryXStatus(inventory.getXStatus());
            inventoryInfo.setInventoryIsLocked(inventory.getIsLocked());
            inventoryInfo.setInventoryIsSealed(inventory.getIsSealed());
            inventoryInfo.setInventoryIsScattered(inventory.getIsScattered());
            inventoryInfo.setInventoryIsExpired(inventory.getIsExpired());
            inventoryInfo.setInventoryComments(inventory.getComments());
            inventoryInfo.setWeight(inventory.getWeight());
            inventoryInfo.setLength(inventory.getLength());
            inventoryInfo.setWidth(inventory.getWidth());
            inventoryInfo.setHeight(inventory.getHeight());
            inventoryInfo.setInventoryStr1(inventory.getStr1());
            inventoryInfo.setInventoryStr2(inventory.getStr2());
            inventoryInfo.setInventoryStr3(inventory.getStr3());
            inventoryInfo.setInventoryStr4(inventory.getStr4());
            inventoryInfo.setInventoryStr5(inventory.getStr5());
            inventoryInfo.setInventoryPackageQuantity(inventory.getPackageQuantity());
            inventoryInfo.setInventorySmallUnitQuantity(inventory.getSmallUnitQuantity());
            inventoryInfo.setLevelCount(inventory.getLevelCount());
            inventoryInfo.setConveyorCode(inventory.getConveyorCode());
            inventoryInfo.setApplyOrOrderCode(inventory.getApplyOrOrderCode());
            inventoryInfo.setOrginAGVID(inventory.getOrginAGVID());
            inventoryInfo.setOrginLocationCode(inventory.getOrginLocationCode());
            inventoryInfo.setPalletType(inventory.getPalletType());
            inventoryInfo.setVolume(inventory.getVolume());

            //inventoryItem
            inventoryInfo.setInventoryItemId(inventoryItem.getId());
            //1592409600000  过滤不合法的时间
            if (inventoryItem.getExpiredTime() != null && inventoryItem.getExpiredTime().compareTo(1592409600000L) > 0) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItem.getExpiredTime()), ZoneOffset.of("+8"));
                inventoryInfo.setInventoryItemExpiredTime(localDateTime);
            }
            inventoryInfo.setInventoryItemAllocatedPackageQuantity(inventoryItem.getAllocatedPackageQuantity());
            inventoryInfo.setInventoryItemPackageQuantity(inventoryItem.getPackageQuantity());
            inventoryInfo.setInventoryItemIsLocked(inventoryItem.getIsLocked());
            inventoryInfo.setInventoryItemXStatus(inventoryItem.getXStatus());
            inventoryInfo.setInventoryItemIsExpired(inventoryItem.getIsExpired());
            inventoryInfo.setInventoryItemComments(inventoryItem.getComments());
            inventoryInfo.setInventoryItemStr1(inventoryItem.getStr1());
            inventoryInfo.setInventoryItemStr2(inventoryItem.getStr2());
            inventoryInfo.setInventoryItemStr3(inventoryItem.getStr3());
            inventoryInfo.setInventoryItemStr4(inventoryItem.getStr4());
            inventoryInfo.setInventoryItemStr5(inventoryItem.getStr5());


            if (inventoryItem.getOrganiztionId() != null) {
                Orgnization orgnization = orgnizationMap.get(inventoryItem.getOrganiztionId());
                if (orgnization != null) {
                    inventoryInfo.setOrganiztionId(orgnization.getId());
                    inventoryInfo.setOrganiztionCode(orgnization.getXCode());
                }

            }
            if (inventoryItem.getOrganiztionSupplierId() != null) {
                Orgnization orgnization = orgnizationMap.get(inventoryItem.getOrganiztionSupplierId());
                if (orgnization != null) {
                    inventoryInfo.setOrganiztionSupplierId(orgnization.getId());
                    inventoryInfo.setOrganiztionSupplierCode(orgnization.getXCode());
                }

            }

            //inventoryItemDetail
            inventoryInfo.setInventoryItemDetailId(inventoryItemDetail.getId());
            inventoryInfo.setCarton(inventoryItemDetail.getCarton());
            inventoryInfo.setSerialNo(inventoryItemDetail.getSerialNo());
            if (material == null) {
                log.error("ignoreIncrement material is null " + inventoryItemDetail.getMaterialId().toString());
                continue;
            }

            inventoryInfo.setMaterialId(inventoryItemDetail.getMaterialId());
            inventoryInfo.setMaterialCode(material.getXCode());
            inventoryInfo.setBatchNo(inventoryItemDetail.getBatchNo());
            inventoryInfo.setBatchNo2(inventoryItemDetail.getBatchNo2());
            inventoryInfo.setBatchNo3(inventoryItemDetail.getBatchNo3());

            if (inventoryItemDetail.getPackageUnitId() != null) {
                PackageUnit packageUnit = packageUnitMap.get(inventoryItemDetail.getPackageUnitId());
//                PackageUnit packageUnit = (PackageUnit) redisTemplate.opsForHash().get(BasicInfoCacheServiceImpl.packageUnitPrefix, inventoryItemDetail.getPackageUnitId().toString());
                if (packageUnit != null) {
                    inventoryInfo.setPackageUnitId(packageUnit.getId());
                    inventoryInfo.setPackageUnitCode(packageUnit.getUnit());
                }

            }

            inventoryInfo.setSmallUnitQuantity(inventoryItemDetail.getSmallUnitQuantity());
            inventoryInfo.setPackageQuantity(inventoryItemDetail.getPackageQuantity());
            inventoryInfo.setAllocatedSmallUnitQuantity(inventoryItemDetail.getAllocatedSmallUnitQuantity());
            inventoryInfo.setAllocatedPackageQuantity(inventoryItemDetail.getAllocatedPackageQuantity());
            inventoryInfo.setQCStatus(inventoryItemDetail.getQCStatus());
            inventoryInfo.setXStatus(inventoryItemDetail.getXStatus());
            inventoryInfo.setIsLocked(inventoryItemDetail.getIsLocked());
            inventoryInfo.setIsSealed(inventoryItemDetail.getIsSealed());
            inventoryInfo.setIsScattered(inventoryItemDetail.getIsScattered());
            inventoryInfo.setIsExpired(inventoryItemDetail.getIsExpired());

            if (inventoryItemDetail.getExpiredTime() != null && inventoryItem.getExpiredTime().compareTo(1592409600000L) > 0) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItemDetail.getExpiredTime()), ZoneOffset.of("+8"));
                inventoryInfo.setExpiredTime(localDateTime);
            }
            inventoryInfo.setComments(inventoryItemDetail.getComments());
            inventoryInfo.setM_Str1(inventoryItemDetail.getM_Str1());
            inventoryInfo.setM_Str2(inventoryItemDetail.getM_Str2());
            inventoryInfo.setM_Str3(inventoryItemDetail.getM_Str3());
            inventoryInfo.setM_Str4(inventoryItemDetail.getM_Str4());
            inventoryInfo.setM_Str5(inventoryItemDetail.getM_Str5());
            inventoryInfo.setM_Str6(inventoryItemDetail.getM_Str6());
            inventoryInfo.setM_Str7(inventoryItemDetail.getM_Str7());
            inventoryInfo.setM_Str8(inventoryItemDetail.getM_Str8());
            inventoryInfo.setM_Str9(inventoryItemDetail.getM_Str9());
            inventoryInfo.setM_Str10(inventoryItemDetail.getM_Str10());

            inventoryInfo.setM_Str11(inventoryItemDetail.getM_Str11());
            inventoryInfo.setM_Str12(inventoryItemDetail.getM_Str12());
            inventoryInfo.setM_Str13(inventoryItemDetail.getM_Str13());
            inventoryInfo.setM_Str14(inventoryItemDetail.getM_Str14());
            inventoryInfo.setM_Str15(inventoryItemDetail.getM_Str15());
            inventoryInfo.setM_Str16(inventoryItemDetail.getM_Str16());
            inventoryInfo.setM_Str17(inventoryItemDetail.getM_Str17());
            inventoryInfo.setM_Str18(inventoryItemDetail.getM_Str18());
            inventoryInfo.setM_Str19(inventoryItemDetail.getM_Str19());
            inventoryInfo.setM_Str20(inventoryItemDetail.getM_Str20());

            inventoryInfo.setM_Str21(inventoryItemDetail.getM_Str21());
            inventoryInfo.setM_Str22(inventoryItemDetail.getM_Str22());
            inventoryInfo.setM_Str23(inventoryItemDetail.getM_Str23());
            inventoryInfo.setM_Str24(inventoryItemDetail.getM_Str24());
            inventoryInfo.setM_Str25(inventoryItemDetail.getM_Str25());
            inventoryInfo.setM_Str26(inventoryItemDetail.getM_Str26());
            inventoryInfo.setM_Str27(inventoryItemDetail.getM_Str27());
            inventoryInfo.setM_Str28(inventoryItemDetail.getM_Str28());
            inventoryInfo.setM_Str29(inventoryItemDetail.getM_Str29());
            inventoryInfo.setM_Str30(inventoryItemDetail.getM_Str30());

            inventoryInfo.setM_Str31(inventoryItemDetail.getM_Str31());
            inventoryInfo.setM_Str32(inventoryItemDetail.getM_Str32());
            inventoryInfo.setM_Str33(inventoryItemDetail.getM_Str33());
            inventoryInfo.setM_Str34(inventoryItemDetail.getM_Str34());
            inventoryInfo.setM_Str35(inventoryItemDetail.getM_Str35());
            inventoryInfo.setM_Str36(inventoryItemDetail.getM_Str36());
            inventoryInfo.setM_Str37(inventoryItemDetail.getM_Str37());
            inventoryInfo.setM_Str38(inventoryItemDetail.getM_Str38());
            inventoryInfo.setM_Str39(inventoryItemDetail.getM_Str39());
            inventoryInfo.setM_Str40(inventoryItemDetail.getM_Str40());

            inventoryInfo.setCreatorId(inventoryItemDetail.getCreatorId());
            inventoryInfo.setCreatorName(inventoryItemDetail.getCreatorName());
            inventoryInfo.setLastModifierId(inventoryItemDetail.getLastModifierId());
            inventoryInfo.setLastModifierName(inventoryItemDetail.getLastModifierName());

            if (inventoryItemDetail.getCreationTime() != null) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItemDetail.getCreationTime()), ZoneOffset.of("+8"));
                inventoryInfo.setCreationTime(localDateTime);
            }

            if (inventoryItemDetail.getLastModificationTime() != null) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItemDetail.getLastModificationTime()), ZoneOffset.of("+8"));
                inventoryInfo.setLastModificationTime(localDateTime);
            }
            if (inventoryItemDetail.getInboundTime() != null) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItemDetail.getInboundTime()), ZoneOffset.of("+8"));
                inventoryInfo.setInboundTime(localDateTime);
            }
            if (inventoryItemDetail.getProductTime() != null) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItemDetail.getProductTime()), ZoneOffset.of("+8"));
                inventoryInfo.setProductTime(localDateTime);
            }

            inventoryInfo.setPositionCode(inventoryItemDetail.getPositionCode());
            inventoryInfo.setPositionLevel(inventoryItemDetail.getPositionLevel());
            inventoryInfo.setPackageMethod(inventoryItemDetail.getPackageMethod());

            inventoryInfos.add(inventoryInfo);
        }
        elasticsearchRestTemplate.save(inventoryInfos);
        return inventoryInfos.size();
    }


    /**
     * 创建索引及映射
     *
     *
     * 在 Elasticsearch 中，当字段值为 null 或空数组 [] 时，默认情况下该字段不会被索引。
     * 这是 Elasticsearch 的一个优化行为，目的是减少不必要的索引开销。
     * @return
     */
    public <T> Boolean createIndexAndMapping(Class<T> clas) {


        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(clas);

        // 1. 构造 settings，包括 max_result_window
        Document settings = Document.create();
        Map<String, Object> map = new HashMap<>();
        map.put("max_result_window", 500000);
        settings.put("index", map);
        //创建索引
        boolean result = indexOperations.create(settings);
        if (result) {
            //生成映射
            Document mapping = indexOperations.createMapping();
            //推送映射
            return indexOperations.putMapping(mapping);
        } else {
            return result;
        }
    }

    @Override
    public PageData<InventoryInfo> getInventoryInfoDefaultList(InventoryInfoRequest request) throws Exception {
        request.setDeleted(0);
        request.setInventoryXStatus(0);
        request.setInventoryIsExpired(false);
        request.setInventoryIsLocked(false);

        request.setInventoryItemIsLocked(false);
        request.setInventoryItemXStatus(0);
        request.setInventoryItemIsExpired(false);

        request.setXStatus(0);
        request.setIsLocked(false);
        request.setIsExpired(false);

        request.setLocationXStatus(1);
        request.setForbidOutbound(false);
        request.setLocationIsLocked(false);
        request.setIsCountLocked(false);
        //平库也可以分配，默认只能立库
//        request.setLocationXType(1);

        request.setLanewayXStatus(1);

        List<String> sourceFieldList = new ArrayList<>();
        sourceFieldList.add("inventoryId");
        sourceFieldList.add("inventoryItemId");
        sourceFieldList.add("inventoryItemDetailId");
        sourceFieldList.add("packageQuantity");
        sourceFieldList.add("allocatedPackageQuantity");
        sourceFieldList.add("pallet");
        request.setSourceFieldList(sourceFieldList);


        request.setPageIndex(0);
        request.setPageSize(50);
        return getInventoryInfoList(request);

    }

    @Override
    public HashMap<Long, List<InventoryInfo>> getDefaultAllocatedInventoryInfoList(InventoryInfoRequest request) throws Exception {
        request.setDeleted(0);
        request.setInventoryXStatus(0);
        request.setInventoryIsExpired(false);
        request.setInventoryIsLocked(false);

        request.setInventoryItemIsLocked(false);
        request.setInventoryItemXStatus(0);
        request.setInventoryItemIsExpired(false);

        request.setXStatus(0);
        request.setIsLocked(false);
        request.setIsExpired(false);

        request.setLocationXStatus(1);
        request.setForbidOutbound(false);
        request.setLocationIsLocked(false);
        request.setIsCountLocked(false);
        //平库也可以分配，默认只能立库
//        request.setLocationXType(1);

        request.setLanewayXStatus(1);

        List<String> sourceFieldList = new ArrayList<>();
        sourceFieldList.add("inventoryId");
        sourceFieldList.add("inventoryItemId");
        sourceFieldList.add("inventoryItemDetailId");
        sourceFieldList.add("packageQuantity");
        sourceFieldList.add("allocatedPackageQuantity");
        sourceFieldList.add("pallet");
        request.setSourceFieldList(sourceFieldList);


        request.setPageIndex(0);
        request.setPageSize(50);
        return getAllocatedInventoryInfoList(request);

    }

    @Override
    public PageData<InventoryInfo> getInventoryInfoList(InventoryInfoRequest request) throws Exception {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (request.getZoneId() != null && request.getZoneId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("zoneId", request.getZoneId()));
        }
        if (request.getInventoryXStatus() != null && request.getInventoryXStatus() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryXStatus", request.getInventoryXStatus()));
        }
        if (request.getInventoryIsExpired() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryIsExpired", request.getInventoryIsExpired()));
        }
        if (request.getInventoryIsLocked() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryIsLocked", request.getInventoryIsLocked()));
        }

        if (request.getInventoryItemIsLocked() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemIsLocked", request.getInventoryItemIsLocked()));
        }
        if (request.getInventoryItemXStatus() != null && request.getInventoryItemXStatus() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemXStatus", request.getInventoryItemXStatus()));
        }

        if (request.getInventoryItemIsExpired() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemIsExpired", request.getInventoryItemIsExpired()));
        }

        if (request.getXStatus() != null && request.getXStatus() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("xStatus", request.getXStatus()));
        }
        if (request.getIsLocked() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("isLocked", request.getIsLocked()));
        }
        if (request.getIsExpired() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("isExpired", request.getIsExpired()));
        }

        if (request.getLocationXStatus() != null && request.getLocationXStatus() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("locationXStatus", request.getLocationXStatus()));
        }

        if (request.getForbidOutbound() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("forbidOutbound", request.getForbidOutbound()));
        }
        if (request.getLocationIsLocked() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("locationIsLocked", request.getLocationIsLocked()));
        }
        if (request.getIsCountLocked() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("isCountLocked", request.getIsCountLocked()));
        }

        if (request.getLocationXType() != null && request.getLocationXType() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("locationXType", request.getLocationXType()));
        }
        if (request.getLanewayXStatus() != null && request.getLanewayXStatus() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("lanewayXStatus", request.getLanewayXStatus()));
        }
        if (request.getInventoryItemDetailId() != null && request.getInventoryItemDetailId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemDetailId", request.getInventoryItemDetailId()));
        }
        if (request.getInventoryItemId() != null && request.getInventoryItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemId", request.getInventoryItemId()));
        }

        if (request.getInventoryId() != null && request.getInventoryId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryId", request.getInventoryId()));
        }
        if (request.getLanewayId() != null && request.getLanewayId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("lanewayId", request.getLanewayId()));
        }
        if (request.getLocationId() != null && request.getLocationId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("locationId", request.getLocationId()));
        }
        //rangeQuery gt  gte  lte

        boolQueryBuilder.must(QueryBuilders.rangeQuery("inventoryPackageQuantity").gt(0));
        boolQueryBuilder.must(QueryBuilders.termQuery("inventoryAllocatedPackageQuantity", 0));
        boolQueryBuilder.must(QueryBuilders.rangeQuery("inventoryItemPackageQuantity").gt(0));
        boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemAllocatedPackageQuantity", 0));
        boolQueryBuilder.must(QueryBuilders.rangeQuery("packageQuantity").gt(0));
        boolQueryBuilder.must(QueryBuilders.termQuery("allocatedPackageQuantity", 0));
        if (request.getMaterialId() != null && request.getMaterialId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("materialId", request.getMaterialId()));
        }
        if (request.getIsSealed() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("isSealed", request.getIsSealed()));
        }
        if (request.getInventoryItemIsSealed() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemIsSealed", request.getInventoryItemIsSealed()));
        }


        if (StringUtils.isNotEmpty(request.getApplyOrOrderCode())) {
            boolQueryBuilder.must(QueryBuilders.termQuery("applyOrOrderCode", request.getApplyOrOrderCode()));
        } else {
            // 过滤字段存在且非空
            boolQueryBuilder.mustNot(QueryBuilders.existsQuery("applyOrOrderCode"));
        }

        // in id  查询
        if (CollectionUtils.isNotEmpty(request.getMaterialIdList())) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("materialId", request.getMaterialIdList()));
        }


// 日期大于某个时间点
//        RangeQueryBuilder dateQuery = QueryBuilders.rangeQuery("timestamp")
//                .gt("2023-01-01T00:00:00")
//                .format("strict_date_optional_time");
//        boolQuery.must(dateQuery);


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
        SearchHits<InventoryInfo> search = elasticsearchRestTemplate.search(nativeSearchQuery, InventoryInfo.class);
        List<InventoryInfo> inventoryInfoList = search.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());

        long count = search.getTotalHits();
        PageData<InventoryInfo> pageData = new PageData<>();
        pageData.setCount(count);
        pageData.setData(inventoryInfoList);
//        elasticsearchRestTemplate.bulkUpdate();
//        elasticsearchRestTemplate.bulkIndex();
//        elasticsearchRestTemplate.delete()
//        elasticsearchRestTemplate.save()
        return pageData;
    }

    @Override
    public HashMap<Long, List<InventoryInfo>> getAllocatedInventoryInfoList(InventoryInfoRequest request) throws Exception {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (request.getZoneId() != null && request.getZoneId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("zoneId", request.getZoneId()));
        }
        if (request.getInventoryXStatus() != null && request.getInventoryXStatus() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryXStatus", request.getInventoryXStatus()));
        }
        if (request.getInventoryIsExpired() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryIsExpired", request.getInventoryIsExpired()));
        }
        if (request.getInventoryIsLocked() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryIsLocked", request.getInventoryIsLocked()));
        }

        if (request.getInventoryItemIsLocked() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemIsLocked", request.getInventoryItemIsLocked()));
        }
        if (request.getInventoryItemXStatus() != null && request.getInventoryItemXStatus() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemXStatus", request.getInventoryItemXStatus()));
        }

        if (request.getInventoryItemIsExpired() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemIsExpired", request.getInventoryItemIsExpired()));
        }

        if (request.getXStatus() != null && request.getXStatus() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("xStatus", request.getXStatus()));
        }
        if (request.getIsLocked() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("isLocked", request.getIsLocked()));
        }
        if (request.getIsExpired() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("isExpired", request.getIsExpired()));
        }

        if (request.getLocationXStatus() != null && request.getLocationXStatus() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("locationXStatus", request.getLocationXStatus()));
        }

        if (request.getForbidOutbound() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("forbidOutbound", request.getForbidOutbound()));
        }
        if (request.getLocationIsLocked() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("locationIsLocked", request.getLocationIsLocked()));
        }
        if (request.getIsCountLocked() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("isCountLocked", request.getIsCountLocked()));
        }

        if (request.getLocationXType() != null && request.getLocationXType() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("locationXType", request.getLocationXType()));
        }
        if (request.getLanewayXStatus() != null && request.getLanewayXStatus() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("lanewayXStatus", request.getLanewayXStatus()));
        }

        if (request.getInventoryItemDetailId() != null && request.getInventoryItemDetailId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemDetailId", request.getInventoryItemDetailId()));
        }
        if (request.getInventoryItemId() != null && request.getInventoryItemId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemId", request.getInventoryItemId()));
        }

        if (request.getInventoryId() != null && request.getInventoryId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryId", request.getInventoryId()));
        }
        if (request.getLanewayId() != null && request.getLanewayId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("lanewayId", request.getLanewayId()));
        }
        if (request.getLocationId() != null && request.getLocationId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("locationId", request.getLocationId()));
        }
        //rangeQuery gt  gte  lte

        boolQueryBuilder.must(QueryBuilders.rangeQuery("inventoryPackageQuantity").gt(0));
        boolQueryBuilder.must(QueryBuilders.termQuery("inventoryAllocatedPackageQuantity", 0));
        boolQueryBuilder.must(QueryBuilders.rangeQuery("inventoryItemPackageQuantity").gt(0));
        boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemAllocatedPackageQuantity", 0));
        boolQueryBuilder.must(QueryBuilders.rangeQuery("packageQuantity").gt(0));
        boolQueryBuilder.must(QueryBuilders.termQuery("allocatedPackageQuantity", 0));
        if (request.getMaterialId() != null && request.getMaterialId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("materialId", request.getMaterialId()));
        }
        if (request.getIsSealed() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("isSealed", request.getIsSealed()));
        }
        if (request.getInventoryItemIsSealed() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryItemIsSealed", request.getInventoryItemIsSealed()));
        }


        if (StringUtils.isNotEmpty(request.getApplyOrOrderCode())) {
            boolQueryBuilder.must(QueryBuilders.termQuery("applyOrOrderCode", request.getApplyOrOrderCode()));
        } else {
            // 过滤字段存在且非空
            boolQueryBuilder.mustNot(QueryBuilders.existsQuery("applyOrOrderCode"));
        }

        // in id  查询
        if (CollectionUtils.isNotEmpty(request.getMaterialIdList())) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("materialId", request.getMaterialIdList()));
        }


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


        //region script

        List<String> aggFields = new ArrayList<>();
        aggFields.add("materialId");


        String scriptContent = aggFields.stream().map(one -> String.format("doc['%s'].value", one))
                .collect(Collectors.joining("+'" + SEPARATOR + "'+"));


        Script script = new Script(scriptContent);
        int aggregationSize = 2;
        if (CollectionUtils.isNotEmpty(request.getMaterialIdList())) {
            aggregationSize = request.getMaterialIdList().size();
        }
        // 创建一个聚合查询对象
        TermsAggregationBuilder scriptAggregationBuilder =
                AggregationBuilders
                        .terms("aggregation_name_materialId")
                        .script(script)
                        // 返回桶数
                        .size(aggregationSize);

        //创建一个 top_hits 聚合
        TopHitsAggregationBuilder topHitsAggregation =
                AggregationBuilders.topHits("top_docs")
                        .sort("packageQuantity", SortOrder.ASC)
                        .sort("allocatedPackageQuantity", SortOrder.ASC)
                        //分页，可不要，直接指定size
                        .from(0)
                        // 设置每个桶内 返回的文档数目
                        .size(100);
        //可将 scriptAggregationBuilder 的值复制到postman 中格式化查看，就是对应的dsl 语句
        //将 top_hits 聚合添加到桶聚合中。
        scriptAggregationBuilder.subAggregation(topHitsAggregation);

        int debug = 1;
        //endregion

        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withSorts(sortBuilderList)
                .withSourceFilter(new SourceFilter() {
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
                .addAggregation(scriptAggregationBuilder)
                .build();


        SearchHits<InventoryInfo> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, InventoryInfo.class);

        AggregationsContainer<?> aggregationsContainer = searchHits.getAggregations();
        Object obj = aggregationsContainer.aggregations();
        Aggregations aggregations = (Aggregations) aggregationsContainer.aggregations();


        Map<String, Aggregation> map = aggregations.getAsMap();
        //key    ShipOrderCode91|ApplyShipOrderCode92
        HashMap<Object, Long> hashMap1 = new HashMap<>();
        HashMap<Long, List<InventoryInfo>> bucketHitsMap = new HashMap<>();
        for (Aggregation aggregation : map.values()) {
            Terms terms1 = aggregations.get(aggregation.getName());
            for (Terms.Bucket bucket : terms1.getBuckets()) {
                Object key = bucket.getKey();
                long count = bucket.getDocCount();
                hashMap1.put(key, count);

                Aggregations bucketAggregations = bucket.getAggregations();

                org.elasticsearch.search.SearchHits bucketSearchHits = ((ParsedTopHits) bucketAggregations.asList().get(0)).getHits();
                List<InventoryInfo> bucketHitList = new ArrayList<>();
                for (org.elasticsearch.search.SearchHit searchHit : bucketSearchHits.getHits()) {

//                    //字段名和对应的值
//                    Map<String, Object> smap = searchHit.getSourceAsMap();
                    String json = searchHit.getSourceAsString();
                    InventoryInfo inventoryInfo = null;
                    try {
                        inventoryInfo = objectMapper.readValue(json, InventoryInfo.class);

                    } catch (Exception ex) {
                        log.error(json);
                        throw ex;
                    }
                    bucketHitList.add(inventoryInfo);
                }
                bucketHitsMap.put(Long.valueOf(key.toString()), bucketHitList);
            }
        }


        return bucketHitsMap;
    }

    @Override
    public void updateByInventoryItemDetail(DataChangeInfo dataChangeInfo) throws JsonProcessingException, InterruptedException {
//        InventoryItemDetail detail = this.inventoryItemDetailService.getById(509955479831328L);
//        String json = objectMapper.writeValueAsString(detail);
        InventoryItemDetail changedInventoryItemDetail = null;

        changedInventoryItemDetail = upperObjectMapper.readValue(dataChangeInfo.getAfterData(), InventoryItemDetail.class);

//        if ("DELETE".equals(dataChangeInfo.getEventType())) {
//            if (changedInventoryItemDetail != null) {
//                log.info("changedInventoryItemDetail {} is not null ,dataChangeInfo.getEventType - {}, BeforeData {},AfterData {}", dataChangeInfo.getId(), dataChangeInfo.getEventType(), dataChangeInfo.getBeforeData(), dataChangeInfo.getAfterData());
//                if (changedInventoryItemDetail.getId() == null) {
//                    log.info("changedInventoryItemDetail {} id is null ,dataChangeInfo.getEventType - {}, BeforeData {},AfterData {}", dataChangeInfo.getId(), dataChangeInfo.getEventType(), dataChangeInfo.getBeforeData(), dataChangeInfo.getAfterData());
//                    changedInventoryItemDetail = upperObjectMapper.readValue(dataChangeInfo.getBeforeData(), InventoryItemDetail.class);
//
//
//                }
//            } else {
//                log.info("changedInventoryItemDetail {} is null ", dataChangeInfo.getId());
//            }
//        }

        if (changedInventoryItemDetail.getId() == null) {
            log.info("changedInventoryItemDetail {} id is null ,dataChangeInfo.getEventType - {}, BeforeData {},AfterData {}", dataChangeInfo.getId(), dataChangeInfo.getEventType(), dataChangeInfo.getBeforeData(), dataChangeInfo.getAfterData());
            changedInventoryItemDetail = upperObjectMapper.readValue(dataChangeInfo.getBeforeData(), InventoryItemDetail.class);
        }


        //更新时间
        if (InventoryInfoServiceImpl.INIT_INVENTORY_TIME == null) {

            String initInventoryTimeStr = (String) redisTemplate.opsForValue().get("InitInventoryTime");
            if (StringUtils.isNotEmpty(initInventoryTimeStr)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                INIT_INVENTORY_TIME = LocalDateTime.parse(initInventoryTimeStr, formatter);

            } else {
                INIT_INVENTORY_TIME = LocalDateTime.now();
            }

        }

        if (changedInventoryItemDetail.getLastModificationTime() != null) {
            LocalDateTime modificationTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(changedInventoryItemDetail.getLastModificationTime()), ZoneOffset.of("+8"));
            if (modificationTime.isBefore(INIT_INVENTORY_TIME)) {
                log.info("modificationTime isBefore INIT_INVENTORY_TIME - {} ", dataChangeInfo.getId());
                return;
            }
        }


        switch (dataChangeInfo.getEventType()) {
            case "CREATE":
                addByInventoryItemDetailInfo(Arrays.asList(changedInventoryItemDetail), null, null, null, null, null, null, null);
                break;
            case "UPDATE":
                updateInventoryInfoOfDetail(changedInventoryItemDetail, dataChangeInfo);
                break;
            case "DELETE":
                deletedByInventoryItemDetail(changedInventoryItemDetail);
                break;
            case "READ":
                break;
            default:
                break;
        }


    }

    @Override
    public void updateByInventoryItem(DataChangeInfo dataChangeInfo) throws JsonProcessingException, InterruptedException {

        InventoryItem changedInventoryItem = null;

        changedInventoryItem = upperObjectMapper.readValue(dataChangeInfo.getAfterData(), InventoryItem.class);
        //更新时间
        if (InventoryInfoServiceImpl.INIT_INVENTORY_TIME == null) {

            String initInventoryTimeStr = (String) redisTemplate.opsForValue().get("InitInventoryTime");
            if (StringUtils.isNotEmpty(initInventoryTimeStr)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                INIT_INVENTORY_TIME = LocalDateTime.parse(initInventoryTimeStr, formatter);

            } else {
                INIT_INVENTORY_TIME = LocalDateTime.now();
            }

        }

        if (changedInventoryItem.getLastModificationTime() != null) {
            LocalDateTime modificationTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(changedInventoryItem.getLastModificationTime()), ZoneOffset.of("+8"));

            if (modificationTime.isBefore(INIT_INVENTORY_TIME)) {
                log.info("modificationTime isBefore INIT_INVENTORY_TIME - {} ", dataChangeInfo.getId());
                return;
            }
        }


        switch (dataChangeInfo.getEventType()) {
            case "CREATE":
                break;
            case "UPDATE":
                updateInventoryInfoOfItem(changedInventoryItem, dataChangeInfo);
                break;
            case "DELETE":

                break;
            case "READ":
                break;
            default:
                break;
        }


    }

    @Override
    public void updateByInventory(DataChangeInfo dataChangeInfo) throws JsonProcessingException, InterruptedException {

        Inventory changedInventory = null;

        changedInventory = upperObjectMapper.readValue(dataChangeInfo.getAfterData(), Inventory.class);
        //更新时间
        if (InventoryInfoServiceImpl.INIT_INVENTORY_TIME == null) {

            String initInventoryTimeStr = (String) redisTemplate.opsForValue().get("InitInventoryTime");
            if (StringUtils.isNotEmpty(initInventoryTimeStr)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                INIT_INVENTORY_TIME = LocalDateTime.parse(initInventoryTimeStr, formatter);

            } else {
                INIT_INVENTORY_TIME = LocalDateTime.now();
            }

        }

        if (changedInventory.getLastModificationTime() != null) {
            LocalDateTime modificationTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(changedInventory.getLastModificationTime()), ZoneOffset.of("+8"));

            if (modificationTime.isBefore(INIT_INVENTORY_TIME)) {
                log.info("modificationTime isBefore INIT_INVENTORY_TIME - {} ", dataChangeInfo.getId());
                return;
            }
        }


        switch (dataChangeInfo.getEventType()) {
            case "CREATE":
                break;
            case "UPDATE":
                updateInventoryInfoOfInventory(changedInventory, dataChangeInfo);
                break;
            case "DELETE":
                break;
            case "READ":
                break;
            default:
                break;
        }

    }

    @Override
    public void updateByLocation(DataChangeInfo dataChangeInfo) throws JsonProcessingException, InterruptedException {
        Location changedLocation = null;

        changedLocation = upperObjectMapper.readValue(dataChangeInfo.getAfterData(), Location.class);
        //更新时间
        if (InventoryInfoServiceImpl.INIT_INVENTORY_TIME == null) {

            String initInventoryTimeStr = (String) redisTemplate.opsForValue().get("InitInventoryTime");
            if (StringUtils.isNotEmpty(initInventoryTimeStr)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                INIT_INVENTORY_TIME = LocalDateTime.parse(initInventoryTimeStr, formatter);

            } else {
                INIT_INVENTORY_TIME = LocalDateTime.now();
            }

        }

        if (changedLocation.getLastModificationTime() != null) {
            LocalDateTime modificationTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(changedLocation.getLastModificationTime()), ZoneOffset.of("+8"));
            if (modificationTime.isBefore(INIT_INVENTORY_TIME)) {
                log.info("modificationTime isBefore INIT_INVENTORY_TIME - {} ", dataChangeInfo.getId());
                return;
            }
        }


        switch (dataChangeInfo.getEventType()) {
            case "CREATE":
                break;
            case "UPDATE":
                //  updateInventoryInfoOfLocation(changedLocation, dataChangeInfo);
                updateInventoryInfoOfLocationBatch(changedLocation, dataChangeInfo);
                break;
            case "DELETE":

                break;
            case "READ":
                break;
            default:
                break;
        }

    }

    @Override
    public void updateByLaneway(DataChangeInfo dataChangeInfo) throws JsonProcessingException, InterruptedException {

        Laneway changedILaneway = null;

        changedILaneway = upperObjectMapper.readValue(dataChangeInfo.getAfterData(), Laneway.class);
        //更新时间
        if (InventoryInfoServiceImpl.INIT_INVENTORY_TIME == null) {

            String initInventoryTimeStr = (String) redisTemplate.opsForValue().get("InitInventoryTime");
            if (StringUtils.isNotEmpty(initInventoryTimeStr)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                INIT_INVENTORY_TIME = LocalDateTime.parse(initInventoryTimeStr, formatter);

            } else {
                INIT_INVENTORY_TIME = LocalDateTime.now();
            }

        }

        if (changedILaneway.getLastModificationTime() != null) {
            LocalDateTime modificationTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(changedILaneway.getLastModificationTime()), ZoneOffset.of("+8"));

            if (modificationTime.isBefore(INIT_INVENTORY_TIME)) {
                log.info("modificationTime isBefore INIT_INVENTORY_TIME - {} ", dataChangeInfo.getId());
                return;
            }
        }


        switch (dataChangeInfo.getEventType()) {
            case "CREATE":
                break;
            case "UPDATE":
                updateInventoryInfoOfLaneway(changedILaneway, dataChangeInfo);
                break;
            case "DELETE":
                break;
            case "READ":
                break;
            default:
                break;
        }

    }

    @Override
    public void test() {

    }

    @Override
    public void updateByInventoryItemDetailDb(Long id) throws InterruptedException {
        InventoryItemDetail inventoryItemDetail = this.inventoryItemDetailService.getById(id);
        DataChangeInfo dataChangeInfo = new DataChangeInfo();
        dataChangeInfo.setTableName("InventoryItemDetail");
        this.updateInventoryInfoOfDetail(inventoryItemDetail, dataChangeInfo);
    }

    @Override
    public void updateByInventoryItemDb(Long id) throws InterruptedException {
        InventoryItem inventoryItem = this.inventoryItemService.getById(id);
        DataChangeInfo dataChangeInfo = new DataChangeInfo();
        dataChangeInfo.setTableName("InventoryItem");
        this.updateInventoryInfoOfItem(inventoryItem, dataChangeInfo);
    }

    @Override
    public void updateByInventoryDb(Long id) throws InterruptedException {
        Inventory inventory = this.inventoryService.getById(id);
        DataChangeInfo dataChangeInfo = new DataChangeInfo();
        dataChangeInfo.setTableName("Inventory");
        this.updateInventoryInfoOfInventory(inventory, dataChangeInfo);
    }


    private void updateInventoryInfoOfDetail(InventoryItemDetail inventoryItemDetail, DataChangeInfo dataChangeInfo) throws InterruptedException {

        InventoryInfo inventoryInfo = elasticsearchOperations.get(inventoryItemDetail.getId().toString(), InventoryInfo.class, IndexCoordinates.of("inventory_info"));

        if (inventoryItemDetail.getLastModificationTime() != null) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItemDetail.getLastModificationTime()), ZoneOffset.of("+8"));
            if (inventoryInfo != null) {
                if (inventoryInfo.getLastModificationTime() != null && localDateTime.isAfter(inventoryInfo.getLastModificationTime())) {
                    Map<String, Object> updatedMap = prepareInventoryItemDetailUpdatedInfo(inventoryInfo, inventoryItemDetail);
                    updateInventoryInfo(inventoryItemDetail.getId().toString(), updatedMap, dataChangeInfo.getTableName());
                }
            } else {
                //新增
            }

        }

    }

    private void updateInventoryInfoOfItem(InventoryItem inventoryItem, DataChangeInfo dataChangeInfo) throws InterruptedException {

        //        CriteriaQuery 适合简单的查询场景，对于复杂的聚合查询，建议使用 NativeSearchQuery
        Criteria criteria = new Criteria("inventoryItemId").is(inventoryItem.getId());

        CriteriaQuery query = new CriteriaQuery(criteria);
        SearchHits<InventoryInfo> searchHits = elasticsearchOperations.search(query, InventoryInfo.class);
        List<InventoryInfo> inventoryInfoList = searchHits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(inventoryInfoList)) {
            log.info("can't get InventoryInfo by inventoryItemId - {} ", inventoryItem.getId());
            return;
        }
        for (InventoryInfo inventoryInfo : inventoryInfoList) {
            if (inventoryItem.getLastModificationTime() != null) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItem.getLastModificationTime()), ZoneOffset.of("+8"));
                if (inventoryInfo.getLastModificationTime() != null && localDateTime.isAfter(inventoryInfo.getLastModificationTime())) {
                    Map<String, Object> updatedMap = prepareInventoryItemUpdatedInfo(inventoryInfo, inventoryItem);
                    updateInventoryInfo(inventoryInfo.getInventoryItemDetailId().toString(), updatedMap, dataChangeInfo.getTableName());
                }
            }
        }
    }


    private void updateInventoryInfoOfInventory(Inventory inventory, DataChangeInfo dataChangeInfo) throws InterruptedException {

        Criteria criteria = new Criteria("inventoryId").is(inventory.getId());

        CriteriaQuery query = new CriteriaQuery(criteria);
        SearchHits<InventoryInfo> searchHits = elasticsearchOperations.search(query, InventoryInfo.class);
        List<InventoryInfo> inventoryInfoList = searchHits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(inventoryInfoList)) {
            log.info("can't get InventoryInfo by inventoryId - {} ", inventory.getId());
            return;
        }
        for (InventoryInfo inventoryInfo : inventoryInfoList) {
            if (inventoryInfo == null) {
                log.info("inventoryInfo is null");
                continue;
            }
            if (inventory.getLastModificationTime() != null) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventory.getLastModificationTime()), ZoneOffset.of("+8"));
                if (inventoryInfo.getLastModificationTime() != null && localDateTime.isAfter(inventoryInfo.getLastModificationTime())) {
                    Map<String, Object> updatedMap = prepareInventoryUpdatedInfo(inventoryInfo, inventory);
                    updateInventoryInfo(inventoryInfo.getInventoryItemDetailId().toString(), updatedMap, dataChangeInfo.getTableName());
                }
            }
        }

    }


    private void updateInventoryInfoOfInventoryBatch(Inventory inventory, DataChangeInfo dataChangeInfo) throws InterruptedException {


        String lockKey = LOCK_KEY;
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {
            lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
            if (!lockSuccessfully) {
                log.info("updateInventoryInfo - {} fail ,get lock fail", lockKey);
                return;
            }
            log.info("InventoryInfo - {} acquire lock  success ", lockKey);


            StopWatch stopWatch = new StopWatch("updateInventoryInfoOfInventoryBatch");
            stopWatch.start("updateInventoryInfoOfInventoryBatch");

            List<String> sourceFieldList = new ArrayList<>();
            sourceFieldList.add("inventoryItemDetailId");
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery("inventoryId", inventory.getId()));

            NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQueryBuilder)
                    .withPageable(PageRequest.of(0, 500000)) // 返回前100条（page=0, size=100）
                    .withSourceFilter(new SourceFilter() {
                        //返回的字段
                        @Override
                        public String[] getIncludes() {
                            if (CollectionUtils.isNotEmpty(sourceFieldList)) {
                                return sourceFieldList.toArray(new String[0]);
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
                    .withTrackTotalHits(true)//返回命中的总行数
                    .build();
            //withTrackTotalHits ,但是只会返回分页（withPageable）指定的productList，默认10条
            SearchHits<InventoryInfo> search = elasticsearchRestTemplate.search(nativeSearchQuery, InventoryInfo.class);
            long totalHits = search.getTotalHits();
            List<InventoryInfo> inventoryInfoList = search.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());


            long count = inventoryInfoList.size();
            int step = 1000;
            long times = count / step;
            long left = count % step;
            if (left > 0) {
                times++;
            }

            long pageIndex = 0L;
            long totalIndexSize = 0L;
            while (times > 0) {

                long skip = (++pageIndex - 1) * step;
                times--;

                List<InventoryInfo> currentInventoryInfoList = inventoryInfoList.stream().skip(skip).limit(step).collect(Collectors.toList());

                List<UpdateQuery> updateQueries = new ArrayList<>();
                for (InventoryInfo inventoryInfo : currentInventoryInfoList) {
                    Map<String, Object> updatedMap = prepareInventoryUpdatedInfo(inventoryInfo, inventory);
//                updateInventoryInfo(inventoryInfo.getInventoryItemDetailIinventoryd().toString(), updatedMap, dataChangeInfo.getTableName());
                    Document document = Document.create();
                    document.putAll(updatedMap);
                    UpdateQuery updateQuery = UpdateQuery.builder(inventoryInfo.getInventoryItemDetailId().toString())
                            .withDocument(document)
                            .withIfSeqNo(null)  // 忽略 seq_no
                            .withIfPrimaryTerm(null) // 忽略 primary_term
                            .withDocAsUpsert(true)// 如果文档不存在则创建
                            .build();
                    updateQueries.add(updateQuery);
                }
                if (updateQueries.size() > 0) {
                    // 执行批量更新
                    elasticsearchOperations.bulkUpdate(updateQueries, IndexCoordinates.of("inventory_info"));
                }

            }


            stopWatch.stop();
            long mills = stopWatch.getTotalTimeMillis();
            log.info("updateInventoryInfoOfLocationBatch complete {} ms", mills);


        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        } finally {

            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }

            log.info("InventoryInfo - {} release lock  success ", lockKey);

        }

    }

    private void updateInventoryInfoOfLocation(Location location, DataChangeInfo dataChangeInfo) throws InterruptedException {

        Criteria criteria = new Criteria("locationId").is(location.getId());
        CriteriaQuery query = new CriteriaQuery(criteria);
        SearchHits<InventoryInfo> searchHits = elasticsearchOperations.search(query, InventoryInfo.class);
        List<InventoryInfo> inventoryInfoList = searchHits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(inventoryInfoList)) {
            log.info("can't get InventoryInfo by locationId - {} ", location.getId());
            return;
        }
        for (InventoryInfo inventoryInfo : inventoryInfoList) {
            Map<String, Object> updatedMap = prepareLocationUpdatedInfo(inventoryInfo, location);
            updateInventoryInfo(inventoryInfo.getInventoryItemDetailId().toString(), updatedMap, dataChangeInfo.getTableName());


//            Document document = Document.create();
//            document.putAll(updatedMap);
//
//            UpdateQuery updateQuery = UpdateQuery.builder(inventoryInfo.getInventoryItemDetailId().toString())
//                    .withDocument(document)
//                    .build();

        }

    }

    private void updateInventoryInfoOfLocationBatch(Location location, DataChangeInfo dataChangeInfo) throws InterruptedException {
        String lockKey = LOCK_KEY;
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {
            lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
            if (!lockSuccessfully) {
                log.info("updateInventoryInfo - {} fail ,get lock fail", lockKey);
                return;
            }
            log.info("InventoryInfo - {} acquire lock  success ", lockKey);


            StopWatch stopWatch = new StopWatch("updateInventoryInfoOfLocationBatch");
            stopWatch.start("updateInventoryInfoOfLocationBatch");

            List<String> sourceFieldList = new ArrayList<>();
            sourceFieldList.add("inventoryItemDetailId");
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery("locationId", location.getId()));

            NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQueryBuilder)
                    .withPageable(PageRequest.of(0, 500000)) // 返回前100条（page=0, size=100）
                    .withSourceFilter(new SourceFilter() {
                        //返回的字段
                        @Override
                        public String[] getIncludes() {
                            if (CollectionUtils.isNotEmpty(sourceFieldList)) {
                                return sourceFieldList.toArray(new String[0]);
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
                    .withTrackTotalHits(true)//返回命中的总行数
                    .build();
            //withTrackTotalHits ,但是只会返回分页（withPageable）指定的productList，默认10条
            SearchHits<InventoryInfo> search = elasticsearchRestTemplate.search(nativeSearchQuery, InventoryInfo.class);
            long totalHits = search.getTotalHits();
            List<InventoryInfo> inventoryInfoList = search.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());


            long count = inventoryInfoList.size();
            int step = 1000;
            long times = count / step;
            long left = count % step;
            if (left > 0) {
                times++;
            }

            long pageIndex = 0L;
            long totalIndexSize = 0L;
            while (times > 0) {

                long skip = (++pageIndex - 1) * step;
                times--;

                List<InventoryInfo> currentInventoryInfoList = inventoryInfoList.stream().skip(skip).limit(step).collect(Collectors.toList());

                List<UpdateQuery> updateQueries = new ArrayList<>();
                for (InventoryInfo inventoryInfo : currentInventoryInfoList) {
                    Map<String, Object> updatedMap = prepareLocationUpdatedInfo(inventoryInfo, location);
//                updateInventoryInfo(inventoryInfo.getInventoryItemDetailId().toString(), updatedMap, dataChangeInfo.getTableName());
                    Document document = Document.create();
                    document.putAll(updatedMap);
                    UpdateQuery updateQuery = UpdateQuery.builder(inventoryInfo.getInventoryItemDetailId().toString())
                            .withDocument(document)
                            .withIfSeqNo(null)  // 忽略 seq_no
                            .withIfPrimaryTerm(null) // 忽略 primary_term
                            .withDocAsUpsert(true)// 如果文档不存在则创建
                            .build();
                    updateQueries.add(updateQuery);
                }
                if (updateQueries.size() > 0) {
                    // 执行批量更新
                    elasticsearchOperations.bulkUpdate(updateQueries, IndexCoordinates.of("inventory_info"));
                }

            }


            stopWatch.stop();
            long mills = stopWatch.getTotalTimeMillis();
            log.info("updateInventoryInfoOfLocationBatch complete {} ms", mills);


        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        } finally {

            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }

            log.info("InventoryInfo - {} release lock  success ", lockKey);

        }
    }

    private void updateInventoryInfoOfLaneway(Laneway laneway, DataChangeInfo dataChangeInfo) throws InterruptedException {

        String lockKey = LOCK_KEY;
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {
            lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
            if (!lockSuccessfully) {
                log.info("updateInventoryInfo - {} fail ,get lock fail", lockKey);
                return;
            }
            log.info("InventoryInfo - {} acquire lock  success ", lockKey);

            StopWatch stopWatch = new StopWatch("updateInventoryInfoOfLaneway");
            stopWatch.start("updateInventoryInfoOfLaneway");

            List<String> sourceFieldList = new ArrayList<>();
            sourceFieldList.add("inventoryItemDetailId");
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery("lanewayId", laneway.getId()));

            NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQueryBuilder)
                    .withPageable(PageRequest.of(0, 500000)) // 返回前100条（page=0, size=100）
                    .withSourceFilter(new SourceFilter() {
                        //返回的字段
                        @Override
                        public String[] getIncludes() {
                            if (CollectionUtils.isNotEmpty(sourceFieldList)) {
                                return sourceFieldList.toArray(new String[0]);
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
                    .withTrackTotalHits(true)//返回命中的总行数
                    .build();
            //withTrackTotalHits ,但是只会返回分页（withPageable）指定的productList，默认10条
            SearchHits<InventoryInfo> search = elasticsearchRestTemplate.search(nativeSearchQuery, InventoryInfo.class);
            long totalHits = search.getTotalHits();
            List<InventoryInfo> inventoryInfoList = search.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());


            //有10000限制
//        Criteria criteria = new Criteria("lanewayId").is(laneway.getId());
//        CriteriaQuery query = new CriteriaQuery(criteria);
//        SearchHits<InventoryInfo> searchHits = elasticsearchOperations.search(query, InventoryInfo.class);
//        List<InventoryInfo> inventoryInfoList1 = searchHits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());


            long count = inventoryInfoList.size();
            int step = 1000;
            long times = count / step;
            long left = count / step;
            if (left > 0) {
                times++;
            }

            long pageIndex = 0L;
            long totalIndexSize = 0L;
            while (times > 0) {

                long skip = (++pageIndex - 1) * step;
                times--;

                List<InventoryInfo> currentInventoryInfoList = inventoryInfoList.stream().skip(skip).limit(step).collect(Collectors.toList());

                List<UpdateQuery> updateQueries = new ArrayList<>();
                for (InventoryInfo inventoryInfo : currentInventoryInfoList) {
                    Map<String, Object> updatedMap = prepareLanewayUpdatedInfo(inventoryInfo, laneway);
//                updateInventoryInfo(inventoryInfo.getInventoryItemDetailId().toString(), updatedMap, dataChangeInfo.getTableName());
                    Document document = Document.create();
                    document.putAll(updatedMap);
                    UpdateQuery updateQuery = UpdateQuery.builder(inventoryInfo.getInventoryItemDetailId().toString())
                            .withDocument(document)
                            .withIfSeqNo(null)  // 忽略 seq_no
                            .withIfPrimaryTerm(null) // 忽略 primary_term
                            .withDocAsUpsert(true)// 如果文档不存在则创建
                            .build();
                    updateQueries.add(updateQuery);
                }
                if (updateQueries.size() > 0) {
                    // 执行批量更新
                    elasticsearchOperations.bulkUpdate(updateQueries, IndexCoordinates.of("inventory_info"));
                }

            }


            stopWatch.stop();
            long mills = stopWatch.getTotalTimeMillis();
            log.info("updateInventoryInfoOfLaneway complete {} ms", mills);


        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        } finally {

            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }

            log.info("InventoryInfo - {} release lock  success ", lockKey);

        }
    }


    private void deletedByInventoryItemDetail(InventoryItemDetail inventoryItemDetail) throws InterruptedException {
        Map<String, Object> updatedMap = new HashMap<>();
        updatedMap.put("deleted", 1);
        updateInventoryInfo(inventoryItemDetail.getId().toString(), updatedMap, "InventoryItemDetail");
    }

    //region 根据实体更新
    public void updateFullDocument(InventoryInfo inventoryInfo) {
        // 直接使用 index 方法会替换整个文档
        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(inventoryInfo.getInventoryItemDetailId().toString())
                .withObject(inventoryInfo)
                .build();

        elasticsearchOperations.index(indexQuery, IndexCoordinates.of("inventory_info"));
    }
//endregion

    // 更新方法2：使用字段映射
    private void updateInventoryInfo(String id, Map<String, Object> fieldsMap, String table) throws InterruptedException {

        String lockKey = LOCK_KEY;// "redisson:updateInventoryInfo:" + id;
        //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {
            //boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException
            lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
            if (!lockSuccessfully) {
//                log.info("Thread - {} 获得锁 {}失败！锁被占用！", Thread.currentThread().getId(), lockKey);

                log.info("updateInventoryInfo - {} fail ,get lock fail", id);
                //获取不到锁，抛异常处理 服务器繁忙，稍后重试
//                    throw new Exception("服务器繁忙，稍后重试");
                return;
            }
            log.info("InventoryInfo - {} acquire lock - {} success ", id, lockKey);

            //            Elasticsearch 7+ 使用 seq_no 和 primary_term 替代了旧版的 _version 来跟踪文档变更
//         withAbortOnVersionConflict(false) 无效：
//            该参数仅影响传统的 _version 冲突处理，对 seq_no 和 primary_term 冲突可能无效。
//            .withIfSeqNo(null)  // 忽略 seq_no
//                    .withIfPrimaryTerm(null) // 忽略 primary_term

            Document document = Document.create();
            document.putAll(fieldsMap);
// .retryOnConflict(3) // 冲突时重试3次
//                    .setIfSeqNo(seqNo)  // 使用正确的序列号
//                    .setIfPrimaryTerm(primaryTerm);
            UpdateQuery updateQuery = UpdateQuery.builder(id)
                    .withDocument(document)
//                    .withRetryOnConflict(3) // 冲突时重试3次
                    .withIfSeqNo(null)  // 忽略 seq_no
                    .withIfPrimaryTerm(null) // 忽略 primary_term
                    .withDocAsUpsert(true)// 如果文档不存在则创建
                    .build();
            //InventoryInfo
            UpdateResponse response = elasticsearchOperations.update(updateQuery, IndexCoordinates.of("inventory_info"));
            log.info("updateInventoryInfo complete  table - {} id - {} result - {}", table, id, response.getResult().toString());


        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        } finally {
            //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
            //unlock 删除key
            //如果锁因超时（leaseTime）会抛异常

//            lock.unlock(); // 执行Redisson操作

            // 清除中断状态
//            boolean wasInterrupted = Thread.interrupted();
//            try {
//                // 只有当前线程持有锁，才释放
//                if (lockSuccessfully && lock.isHeldByCurrentThread()) {
//                    lock.unlock();
//                }
//            } finally {
//                if (wasInterrupted) {
//                    // 恢复中断状态
//                    Thread.currentThread().interrupt();
//                }
//            }

//            if (lock.isHeldByCurrentThread()) {
//                try {
//                    lock.unlock();
//                } catch (Exception e) {
//                    // 记录日志，但不要吞没异常
//                    log.error("解锁失败", e);
//                }
//            }

            // 只有当前线程持有锁，才释放
            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            log.info("InventoryInfo - {} release lock - {} success ", id, lockKey);

        }


    }

//    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
//    public void updateDocument() {
//        elasticsearchOperations.update(updateQuery, IndexCoordinates.of("inventory_info"));
//    }

    public void updateInventoryInfoBatch(String id, Map<String, Object> fields, String table) {
        Document document = Document.create();
        document.putAll(fields);

        UpdateQuery updateQuery = UpdateQuery.builder(id)
                .withDocument(document)
                .build();
        //InventoryInfo
        UpdateResponse response = elasticsearchOperations.update(updateQuery, IndexCoordinates.of("inventory_info"));
        log.info("updateInventoryInfo complete  table - {} id - {} result - {}", table, id, response.getResult().toString());

        int n = 0;
    }

    private Map<String, Object> prepareLocationUpdatedInfo(InventoryInfo inventoryInfo, Location location) {

        Map<String, Object> updatedMap = new HashMap<>();
        updatedMap.put("locationCode", location.getXCode());
        updatedMap.put("locationXStatus", location.getXStatus());
        updatedMap.put("locationIsLocked", location.getIsLocked());
        updatedMap.put("forbidOutbound", location.getForbidOutbound());
        updatedMap.put("isCountLocked", location.getIsCountLocked());
        updatedMap.put("locationXType", location.getXType());
        return updatedMap;
    }


    private Map<String, Object> prepareLanewayUpdatedInfo(InventoryInfo inventoryInfo, Laneway laneway) {
        Map<String, Object> updatedMap = new HashMap<>();
        updatedMap.put("lanewayCode", laneway.getXCode());
        updatedMap.put("lanewayXStatus", laneway.getXStatus());
        return updatedMap;
    }


    private Map<String, Object> prepareInventoryUpdatedInfo(InventoryInfo inventoryInfo, Inventory inventory) {
        //inventory
        Map<String, Object> updatedMap = new HashMap<>();
        updatedMap.put("pallet", inventory.getPallet());
        updatedMap.put("inventoryAllocatedSmallUnitQuantity", inventory.getAllocatedSmallUnitQuantity());
        updatedMap.put("inventoryAllocatedPackageQuantity", inventory.getAllocatedPackageQuantity());
        updatedMap.put("inventoryQCStatus", inventory.getQCStatus());
        updatedMap.put("inventoryXStatus", inventory.getXStatus());
        updatedMap.put("inventoryIsLocked", inventory.getIsLocked());
        updatedMap.put("inventoryIsSealed", inventory.getIsSealed());
        updatedMap.put("inventoryIsScattered", inventory.getIsScattered());
        updatedMap.put("inventoryIsExpired", inventory.getIsExpired());
        updatedMap.put("inventoryComments", inventory.getComments());

        updatedMap.put("weight", inventory.getWeight());
        updatedMap.put("length", inventory.getLength());
        updatedMap.put("width", inventory.getWidth());
        updatedMap.put("height", inventory.getHeight());
        updatedMap.put("inventoryStr1", inventory.getStr1());
        updatedMap.put("inventoryStr2", inventory.getStr2());
        updatedMap.put("inventoryStr3", inventory.getStr3());
        updatedMap.put("inventoryStr4", inventory.getStr4());
        updatedMap.put("inventoryStr5", inventory.getStr5());

        updatedMap.put("inventoryPackageQuantity", inventory.getPackageQuantity());
        updatedMap.put("inventorySmallUnitQuantity", inventory.getSmallUnitQuantity());
        updatedMap.put("levelCount", inventory.getLevelCount());
        updatedMap.put("conveyorCode", inventory.getConveyorCode());
        updatedMap.put("applyOrOrderCode", inventory.getApplyOrOrderCode());
        updatedMap.put("orginAGVID", inventory.getOrginAGVID());
        updatedMap.put("orginLocationCode", inventory.getOrginLocationCode());
        updatedMap.put("palletType", inventory.getPalletType());
        updatedMap.put("volume", inventory.getVolume());

        return updatedMap;
    }

    private Map<String, Object> prepareInventoryItemUpdatedInfo(InventoryInfo inventoryInfo, InventoryItem inventoryItem) {
        //inventoryItem
        Map<String, Object> updatedMap = new HashMap<>();
        if (inventoryItem.getExpiredTime() != null && inventoryItem.getExpiredTime().compareTo(1592409600000L) > 0) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItem.getExpiredTime()), ZoneOffset.of("+8"));
            updatedMap.put("inventoryItemExpiredTime", inventoryItem.getExpiredTime());
        }
        updatedMap.put("inventoryItemAllocatedPackageQuantity", inventoryItem.getAllocatedPackageQuantity());
        updatedMap.put("inventoryItemPackageQuantity", inventoryItem.getPackageQuantity());
        updatedMap.put("inventoryItemIsLocked", inventoryItem.getIsLocked());
        updatedMap.put("inventoryItemXStatus", inventoryItem.getXStatus());
        updatedMap.put("inventoryItemIsExpired", inventoryItem.getIsExpired());
        updatedMap.put("inventoryItemComments", inventoryItem.getComments());
        updatedMap.put("inventoryItemStr1", inventoryItem.getStr1());
        updatedMap.put("inventoryItemStr2", inventoryItem.getStr2());
        updatedMap.put("inventoryItemStr3", inventoryItem.getStr3());
        updatedMap.put("inventoryItemStr4", inventoryItem.getStr4());
        updatedMap.put("inventoryItemStr5", inventoryItem.getStr5());
        return updatedMap;
    }

    private Map<String, Object> prepareInventoryItemDetailUpdatedInfo(InventoryInfo inventoryInfo, InventoryItemDetail inventoryItemDetail) {

        Map<String, Object> updatedMap = new HashMap<>();
        updatedMap.put("carton", inventoryItemDetail.getCarton());
        updatedMap.put("serialNo", inventoryItemDetail.getSerialNo());
        updatedMap.put("batchNo", inventoryItemDetail.getBatchNo());
        updatedMap.put("batchNo2", inventoryItemDetail.getBatchNo2());
        updatedMap.put("batchNo3", inventoryItemDetail.getBatchNo3());
        updatedMap.put("smallUnitQuantity", inventoryItemDetail.getSmallUnitQuantity());
        updatedMap.put("packageQuantity", inventoryItemDetail.getPackageQuantity());
        updatedMap.put("allocatedSmallUnitQuantity", inventoryItemDetail.getAllocatedSmallUnitQuantity());
        updatedMap.put("allocatedPackageQuantity", inventoryItemDetail.getAllocatedPackageQuantity());
        updatedMap.put("qcStatus", inventoryItemDetail.getQCStatus());
        updatedMap.put("xStatus", inventoryItemDetail.getXStatus());
        updatedMap.put("isLocked", inventoryItemDetail.getIsLocked());
        updatedMap.put("isSealed", inventoryItemDetail.getIsSealed());

        updatedMap.put("isScattered", inventoryItemDetail.getIsScattered());
        updatedMap.put("isExpired", inventoryItemDetail.getIsExpired());

        if (inventoryItemDetail.getExpiredTime() != null && inventoryItemDetail.getExpiredTime().compareTo(1592409600000L) > 0) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItemDetail.getExpiredTime()), ZoneOffset.of("+8"));
            updatedMap.put("expiredTime", localDateTime);
        }
        updatedMap.put("comments", inventoryItemDetail.getComments());
        updatedMap.put("m_Str1", inventoryItemDetail.getM_Str1());
        updatedMap.put("m_Str2", inventoryItemDetail.getM_Str2());
        updatedMap.put("m_Str3", inventoryItemDetail.getM_Str3());
        updatedMap.put("m_Str4", inventoryItemDetail.getM_Str4());
        updatedMap.put("m_Str5", inventoryItemDetail.getM_Str5());
        updatedMap.put("m_Str6", inventoryItemDetail.getM_Str6());
        updatedMap.put("m_Str7", inventoryItemDetail.getM_Str7());
        updatedMap.put("m_Str8", inventoryItemDetail.getM_Str8());
        updatedMap.put("m_Str9", inventoryItemDetail.getM_Str9());
        updatedMap.put("m_Str10", inventoryItemDetail.getM_Str10());
        updatedMap.put("m_Str11", inventoryItemDetail.getM_Str11());
        updatedMap.put("m_Str12", inventoryItemDetail.getM_Str12());
        updatedMap.put("m_Str13", inventoryItemDetail.getM_Str13());
        updatedMap.put("m_Str14", inventoryItemDetail.getM_Str14());

        updatedMap.put("m_Str15", inventoryItemDetail.getM_Str15());
        updatedMap.put("m_Str16", inventoryItemDetail.getM_Str16());
        updatedMap.put("m_Str17", inventoryItemDetail.getM_Str17());
        updatedMap.put("m_Str18", inventoryItemDetail.getM_Str18());
        updatedMap.put("m_Str19", inventoryItemDetail.getM_Str19());
        updatedMap.put("m_Str20", inventoryItemDetail.getM_Str20());
        updatedMap.put("m_Str21", inventoryItemDetail.getM_Str21());
        updatedMap.put("m_Str22", inventoryItemDetail.getM_Str22());
        updatedMap.put("m_Str23", inventoryItemDetail.getM_Str23());
        updatedMap.put("m_Str24", inventoryItemDetail.getM_Str24());
        updatedMap.put("m_Str25", inventoryItemDetail.getM_Str25());
        updatedMap.put("m_Str26", inventoryItemDetail.getM_Str26());
        updatedMap.put("m_Str27", inventoryItemDetail.getM_Str27());
        updatedMap.put("m_Str28", inventoryItemDetail.getM_Str28());
        updatedMap.put("m_Str29", inventoryItemDetail.getM_Str29());
        updatedMap.put("m_Str30", inventoryItemDetail.getM_Str30());
        updatedMap.put("m_Str31", inventoryItemDetail.getM_Str31());
        updatedMap.put("m_Str32", inventoryItemDetail.getM_Str32());
        updatedMap.put("m_Str33", inventoryItemDetail.getM_Str33());
        updatedMap.put("m_Str34", inventoryItemDetail.getM_Str34());
        updatedMap.put("m_Str35", inventoryItemDetail.getM_Str35());
        updatedMap.put("m_Str36", inventoryItemDetail.getM_Str36());
        updatedMap.put("m_Str37", inventoryItemDetail.getM_Str37());
        updatedMap.put("m_Str38", inventoryItemDetail.getM_Str38());
        updatedMap.put("m_Str39", inventoryItemDetail.getM_Str39());
        updatedMap.put("m_Str40", inventoryItemDetail.getM_Str40());

        updatedMap.put("lastModifierId", inventoryItemDetail.getLastModifierId());
        updatedMap.put("lastModifierName", inventoryItemDetail.getLastModifierName());


        if (inventoryItemDetail.getLastModificationTime() != null) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItemDetail.getLastModificationTime()), ZoneOffset.of("+8"));
            updatedMap.put("lastModificationTime", localDateTime);
        }

        if (inventoryItemDetail.getProductTime() != null) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItemDetail.getProductTime()), ZoneOffset.of("+8"));
            updatedMap.put("productTime", localDateTime);
        }
        if (inventoryItemDetail.getInboundTime() != null) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItemDetail.getInboundTime()), ZoneOffset.of("+8"));
            updatedMap.put("inboundTime", localDateTime);
        }
        updatedMap.put("positionCode", inventoryItemDetail.getPositionCode());
        updatedMap.put("positionLevel", inventoryItemDetail.getPositionLevel());
        updatedMap.put("packageMethod", inventoryItemDetail.getPackageMethod());
        return updatedMap;
    }


}
