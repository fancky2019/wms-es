package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.elasticsearch.ShipOrderInfoRepository;
import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.elasticsearch.ShipOrderInfo;
import gs.com.gses.model.entity.*;
import gs.com.gses.model.request.InventoryInfoRequest;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.response.PageData;
import gs.com.gses.service.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InventoryInfoServiceImpl implements InventoryInfoService {


    @Autowired
    private ShipOrderInfoRepository shipOrderInfoRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

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

    private static String SEPARATOR = "|";


    @Override
    public void initInventoryInfoFromDb() {
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
//        Map<String, Material> materialMap=   redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.materialPrefix);
        Map<String, Warehouse> warehouseMap = redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.warehousePrefix);
        Map<String, Orgnization> orgnizationMap = redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.orgnizationPrefix);
//        Map<String, PackageUnit> packageUnitMap=   redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.packageUnitPrefix);

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
            List<InventoryInfo> inventoryInfos = new ArrayList<>();

            //page 内部调用selectPage
            IPage<InventoryItemDetail> inventoryItemDetailPage = this.inventoryItemDetailService.page(page);
            List<InventoryItemDetail> inventoryItemDetailList = inventoryItemDetailPage.getRecords();
            List<Long> inventoryItemIdList = inventoryItemDetailList.stream().map(p -> p.getInventoryItemId()).distinct().collect(Collectors.toList());
            List<InventoryItem> inventoryItemList = this.inventoryItemService.listByIds(inventoryItemIdList);
            List<Long> inventoryIdList = inventoryItemList.stream().map(p -> p.getInventoryId()).distinct().collect(Collectors.toList());
            List<Inventory> inventoryList = this.inventoryService.listByIds(inventoryIdList);

            InventoryInfo inventoryInfo = null;
            for (InventoryItemDetail inventoryItemDetail : inventoryItemDetailList) {
                if (inventoryItemDetail.getInventoryItemId().equals(509955479831320L)) {
                    int nn = 0;
                }
                inventoryInfo = new InventoryInfo();
                InventoryItem inventoryItem = inventoryItemList.stream().filter(p -> p.getId().equals(inventoryItemDetail.getInventoryItemId())).findFirst().orElse(null);
                Inventory inventory = inventoryList.stream().filter(p -> p.getId().equals(inventoryItem.getInventoryId())).findFirst().orElse(null);
                Location location = locationMap.get(inventory.getLocationId().toString());
                Laneway laneway = lanewayMap.get(location.getLanewayId().toString());
                Zone zone = zoneMap.get(laneway.getZoneId().toString());
                // Material material = materialMap.get(inventoryItemDetail.getMaterialId().toString());

                Material material = (Material) redisTemplate.opsForHash().get(BasicInfoCacheServiceImpl.materialPrefix, inventoryItemDetail.getMaterialId().toString());


                Warehouse warehouse = warehouseMap.get(inventory.getWhid().toString());

                if (warehouse != null) {
                    inventoryInfo.setWhid(warehouse.getId());
                    inventoryInfo.setWhCode(warehouse.getXCode());
                }

                if (location == null) {
                    log.info("location is null " + inventory.getLocationId().toString());
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
                if (inventoryItem.getExpiredTime() != null) {
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(inventoryItem.getExpiredTime()), ZoneOffset.of("+8"));
                    inventoryInfo.setInventoryItemExpiredTime(localDateTime);
                }
                inventoryInfo.setInventoryItemAllocatedPackageQuantity(inventoryItem.getAllocatedPackageQuantity());
                inventoryInfo.setInventoryItemPackageQuantity(inventoryItem.getPackageQuantity());
                inventoryInfo.setInventoryItemIsLocked(inventoryItem.getIsLocked());
                inventoryInfo.setInventoryItemXStatus(inventoryItem.getXStatus());
                inventoryInfo.setInventoryItemIsExpired(inventoryItem.getIsExpired());
                inventoryInfo.setInventoryItemStr5(inventoryItem.getStr5());
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
                    log.info("material is null " + inventoryItemDetail.getMaterialId().toString());
                    continue;
                }

                inventoryInfo.setMaterialId(inventoryItemDetail.getMaterialId());
                inventoryInfo.setMaterialCode(material.getXCode());
                inventoryInfo.setBatchNo(inventoryItemDetail.getBatchNo());
                inventoryInfo.setBatchNo2(inventoryItemDetail.getBatchNo2());
                inventoryInfo.setBatchNo3(inventoryItemDetail.getBatchNo3());

                if (inventoryItemDetail.getPackageUnitId() != null) {
//                    PackageUnit packageUnit = packageUnitMap.get(inventoryItemDetail.getPackageUnitId());
                    PackageUnit packageUnit = (PackageUnit) redisTemplate.opsForHash().get(BasicInfoCacheServiceImpl.packageUnitPrefix, inventoryItemDetail.getPackageUnitId().toString());
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

                if (inventoryItemDetail.getExpiredTime() != null) {
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
            totalIndexSize += inventoryInfos.size();
            log.info("inventory_info size:" + inventoryInfos.size());
        }

        stopWatch.stop();
//        stopWatch.start("BatchInsert_Trace2");
        long mills = stopWatch.getTotalTimeMillis();
        log.info("initInventoryInfoFromDb complete {} ms", mills);

        log.info("inventory_info total:" + totalIndexSize);
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
        //创建索引
        boolean result = indexOperations.create();
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
    public HashMap<Object, List<InventoryInfo>> getDefaultAllocatedInventoryInfoList(InventoryInfoRequest request) throws Exception {
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
    public HashMap<Object, List<InventoryInfo>> getAllocatedInventoryInfoList(InventoryInfoRequest request) throws Exception {

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
        HashMap<Object, List<InventoryInfo>> bucketHitsMap = new HashMap<>();
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
                    InventoryInfo inventoryInfo = objectMapper.readValue(json, InventoryInfo.class);
                    bucketHitList.add(inventoryInfo);
                }
                bucketHitsMap.put(key, bucketHitList);
            }
        }


        return bucketHitsMap;
    }

}
