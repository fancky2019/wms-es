package gs.com.gses.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.easyecel.DropDownSetField;
import gs.com.gses.easyecel.ResoveDropAnnotationUtil;
import gs.com.gses.easyecel.handler.DropDownCellWriteHandler;
import gs.com.gses.model.bo.ModifyMStr12BoGroupKey;
import gs.com.gses.model.bo.wms.AllocateModel;
import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.entity.*;
import gs.com.gses.mapper.InventoryItemDetailMapper;
import gs.com.gses.model.request.EsRequestPage;
import gs.com.gses.model.request.wms.InventoryInfoRequest;
import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import gs.com.gses.model.bo.ModifyMStr12Bo;
import gs.com.gses.model.request.wms.ShipOrderItemRequest;
import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.InventoryItemDetailResponse;
import gs.com.gses.model.response.wms.ShipOrderItemResponse;
import gs.com.gses.service.*;
import gs.com.gses.utility.BarcodeUtil;
import gs.com.gses.utility.LambdaFunctionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【InventoryItemDetail】的数据库操作Service实现
 * @createDate 2024-08-08 13:44:55
 */
@Slf4j
@Service
public class InventoryItemDetailServiceImpl extends ServiceImpl<InventoryItemDetailMapper, InventoryItemDetail> implements InventoryItemDetailService {


    @Autowired
    private MaterialService materialService;

    @Autowired
    private InventoryItemService inventoryItemService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryInfoService inventoryInfoService;
//
//    @Override
//    public List<InventoryItemDetail> getInventoryItemDetailPage(InventoryItemDetailRequest request) {
//        LambdaQueryWrapper<InventoryItemDetail> queryWrapper = new LambdaQueryWrapper<>();
//        // 创建分页对象 (当前页, 每页大小)
//        Page<InventoryItemDetail> page = new Page<>(request.getPageIndex(), request.getPageSize());
//        // 关键设置：不执行 COUNT 查询
//        page.setSearchCount(false);
//        // 执行分页查询, sqlserver 使用通用表达式 WITH selectTemp AS
//        IPage<InventoryItemDetail> shipOrderPage = this.baseMapper.selectPage(page, queryWrapper);
//
//        // 获取结果   // 当前页数据
//        List<InventoryItemDetail> records = shipOrderPage.getRecords();
//
////        List<InventoryItemDetail> records1=this.list();
//
//

    ////        // 测试单个字段查询,selectOne 内部调用list
    ////        Object entity = this.baseMapper.selectOne(new QueryWrapper<InventoryItemDetail>()
    ////                .select("M_Str1")
    ////                .eq("Id", 509955479831320L));
    ////
    ////// 使用简单查询测试
    ////        InventoryItemDetail entity11 = this.baseMapper.selectById(509955479831320L);
//
//        return records;
//    }
    @Override
    public Boolean checkDetailExist(InventoryItemDetailRequest request, List<ShipOrderItemResponse> matchedShipOrderItemResponseList, List<AllocateModel> allocateModelList) throws Exception {

        if (StringUtils.isEmpty(request.getM_Str7())) {
            throw new Exception("m_Str7 is null");
        }
//        if (StringUtils.isEmpty(request.getM_Str12())) {
//            throw new Exception("m_Str12 is null");
//        }
        if (StringUtils.isEmpty(request.getMaterialCode())) {
            throw new Exception("materialCode is null");
        }
        request.setSearchCount(false);

        PageData<InventoryItemDetailResponse> page = getInventoryItemDetailPage(request);
        List<InventoryItemDetailResponse> detailResponseList = page.getData();
        int size = detailResponseList.size();
        if (size == 0) {
//            throw new Exception("Can't get inventoryItemDetail info by m_Str7 ,m_Str12,materialCode");
            throw new Exception("库存不存在");
        }

        InventoryItemDetailResponse inventoryItemDetailResponse = null;
        if (size > 1) {

            int count = 0;
            if (StringUtils.isNotEmpty(request.getM_Str12())) {
                for (InventoryItemDetailResponse detailResponse : detailResponseList) {
                    List<String> mStr12List = Arrays.stream(detailResponse.getM_Str12().split(",")).collect(Collectors.toList());
                    if (mStr12List.contains(request.getM_Str12())) {
                        count++;
                        inventoryItemDetailResponse = detailResponse;
                    }
                }

                if (count > 1) {
                    throw new Exception("匹配多个库存");
                }
                request.setId(inventoryItemDetailResponse.getId());
                checkPackageQuantity(inventoryItemDetailResponse.getPackageQuantity(), request.getPackageQuantity());
                List<AllocateModel> currentAllocateModelList = allocate(matchedShipOrderItemResponseList, detailResponseList,null);
                allocateModelList.addAll(currentAllocateModelList);

            } else {
                //求和
                BigDecimal sum = detailResponseList.stream()
                        .map(InventoryItemDetailResponse::getPackageQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                checkPackageQuantity(sum, request.getPackageQuantity());
                List<AllocateModel> currentAllocateModelList = allocate(matchedShipOrderItemResponseList, detailResponseList,null);
                allocateModelList.addAll(currentAllocateModelList);
                inventoryItemDetailResponse = detailResponseList.get(0);
            }

        } else {
            inventoryItemDetailResponse = detailResponseList.get(0);
            checkPackageQuantity(inventoryItemDetailResponse.getPackageQuantity(), request.getPackageQuantity());
            List<AllocateModel> currentAllocateModelList = allocate(matchedShipOrderItemResponseList, detailResponseList,null);
            allocateModelList.addAll(currentAllocateModelList);
        }
        request.setMaterialId(inventoryItemDetailResponse.getMaterialId());
        return true;
    }

    @Override
    public Boolean checkDetailExistBatch(List<InventoryItemDetailRequest> requestList, List<ShipOrderItemResponse> matchedShipOrderItemResponseList, List<AllocateModel> allocateModelList) throws Exception {

        String currentTaskName = "getInventoryItemDetail";
        StopWatch stopWatch = new StopWatch("checkDetailExistBatch");
        stopWatch.start(currentTaskName);
        LambdaQueryWrapper<InventoryItemDetail> wrapper = new LambdaQueryWrapper<>();

        wrapper.and(qw -> {
            for (InventoryItemDetailRequest query : requestList) {
                qw.or(w -> {
                    w.eq(InventoryItemDetail::getM_Str7, query.getM_Str7());
                    if (StringUtils.isNotEmpty(query.getM_Str12())) {
                        w.eq(InventoryItemDetail::getM_Str12, query.getM_Str12());
                    }
                    if (query.getMaterialId() != null) {
                        w.eq(InventoryItemDetail::getMaterialId, query.getMaterialId());
                    }
                });
            }
        });

        List<InventoryItemDetail> inventoryItemDetailList = baseMapper.selectList(wrapper);
        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        currentTaskName = "getPalletInfo";
        stopWatch.start(currentTaskName);
        List<Long> detailIdList = inventoryItemDetailList.stream().map(m -> m.getId()).collect(Collectors.toList());
        Map<Long, String> palletMap = getPalletInfo(detailIdList);
        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        currentTaskName = "allocate";
        stopWatch.start(currentTaskName);
        for (InventoryItemDetailRequest request : requestList) {

            List<InventoryItemDetail> currentInventoryItemDetailList = inventoryItemDetailList.stream().filter(p ->
                    request.getMaterialId().equals(p.getMaterialId()) && request.getM_Str7().equals(p.getM_Str7())
            ).collect(Collectors.toList());
            if (StringUtils.isNotEmpty(request.getM_Str12())) {
                currentInventoryItemDetailList = inventoryItemDetailList.stream().filter(p ->
                        request.getM_Str12().equals(p.getM_Str12())
                ).collect(Collectors.toList());
            }

            List<ShipOrderItemResponse> currentShipOrderItemResponseList = matchedShipOrderItemResponseList.stream().filter(p ->
                    request.getMaterialId().equals(p.getMaterialId()) && request.getM_Str7().equals(p.getM_Str7())
            ).collect(Collectors.toList());
            if (StringUtils.isNotEmpty(request.getM_Str12())) {
                currentShipOrderItemResponseList = currentShipOrderItemResponseList.stream().filter(p ->
                        request.getM_Str12().equals(p.getM_Str12())
                ).collect(Collectors.toList());
            }

            int size = currentInventoryItemDetailList.size();
            if (size == 0) {
                String msg = MessageFormat.format("Can't Match InventoryItemDetail by ProjectNo {0} MaterialCode {1} DeviceNo {2}", request.getM_Str7(), request.getMaterialCode(), request.getM_Str12());
                throw new Exception(msg);
            }

            List<InventoryItemDetailResponse> detailResponseList = currentInventoryItemDetailList.stream().map(p -> {
                InventoryItemDetailResponse response = new InventoryItemDetailResponse();
                BeanUtils.copyProperties(p, response);
                return response;
            }).collect(Collectors.toList());

            InventoryItemDetailResponse inventoryItemDetailResponse = null;
            if (size > 1) {

                int count = 0;
                if (StringUtils.isNotEmpty(request.getM_Str12())) {
                    for (InventoryItemDetailResponse detailResponse : detailResponseList) {
                        List<String> mStr12List = Arrays.stream(detailResponse.getM_Str12().split(",")).collect(Collectors.toList());
                        if (mStr12List.contains(request.getM_Str12())) {
                            count++;
                            inventoryItemDetailResponse = detailResponse;
                        }
                    }

                    if (count > 1) {
                        String msg = MessageFormat.format("Match multiple InventoryItemDetails by ProjectNo {0} MaterialCode {1} DeviceNo {2}", request.getM_Str7(), request.getMaterialCode(), request.getM_Str12());
                        throw new Exception(msg);
                    }
                    request.setId(inventoryItemDetailResponse.getId());
                    checkPackageQuantity(inventoryItemDetailResponse.getPackageQuantity(), request.getPackageQuantity());
                    List<AllocateModel> currentAllocateModelList = allocate(currentShipOrderItemResponseList, detailResponseList,palletMap);
                    allocateModelList.addAll(currentAllocateModelList);

                } else {
                    //求和
                    BigDecimal sum = detailResponseList.stream()
                            .map(InventoryItemDetailResponse::getPackageQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    checkPackageQuantity(sum, request.getPackageQuantity());
                    List<AllocateModel> currentAllocateModelList = allocate(currentShipOrderItemResponseList, detailResponseList,palletMap);
                    allocateModelList.addAll(currentAllocateModelList);
                    inventoryItemDetailResponse = detailResponseList.get(0);
                }

            } else {
                inventoryItemDetailResponse = detailResponseList.get(0);
                checkPackageQuantity(inventoryItemDetailResponse.getPackageQuantity(), request.getPackageQuantity());
                List<AllocateModel> currentAllocateModelList = allocate(currentShipOrderItemResponseList, detailResponseList,palletMap);
                allocateModelList.addAll(currentAllocateModelList);
            }
            request.setMaterialId(inventoryItemDetailResponse.getMaterialId());
        }

        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        log.info("currentTaskName stopWatch {} cost {}", stopWatch.getId(), stopWatch.getTotalTimeMillis());
        return true;
    }

    @Override
    public List<AllocateModel> allocate(List<ShipOrderItemResponse> shipOrderItemList,
                                        List<InventoryItemDetailResponse> detailList, Map<Long, String> palletMap) throws Exception {
        List<AllocateModel> allocateModelList = new ArrayList<>();
        if (CollectionUtils.isEmpty(shipOrderItemList)) {
            log.info("shipOrderItemList is empty");
            return allocateModelList;
        }
        shipOrderItemList = shipOrderItemList.stream().sorted(Comparator.comparingLong(ShipOrderItemResponse::getId)).collect(Collectors.toList());
        HashMap<Long, BigDecimal> usedDetailDic = new HashMap<Long, BigDecimal>();
        if (palletMap == null) {
            List<Long> detailIdList = detailList.stream().map(m -> m.getId()).collect(Collectors.toList());
            palletMap = getPalletInfo(detailIdList);
        }
        for (int i = 0; i < shipOrderItemList.size(); i++) {
            ShipOrderItemResponse shipOrderItem = shipOrderItemList.get(i);
            BigDecimal itemNeedPackageQuantity = shipOrderItem.getRequiredPkgQuantity().subtract(shipOrderItem.getAlloactedPkgQuantity());
            if (itemNeedPackageQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            for (InventoryItemDetailResponse detailResponse : detailList) {
                if (detailResponse.getIsLocked()) {
                    String msg = MessageFormat.format("InventoryItemDetail - {0} is locked", detailResponse.getInventoryItemId());
                    throw new Exception(msg);
                }

                BigDecimal detailLeftPackageQuantity = detailResponse.getPackageQuantity();
                if (usedDetailDic.containsKey(detailResponse.getId())) {
                    detailLeftPackageQuantity = usedDetailDic.get(detailResponse.getId());
                    if (detailLeftPackageQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                }

                BigDecimal detailPickedPkgQuantity = BigDecimal.ZERO;
                if (itemNeedPackageQuantity.compareTo(detailLeftPackageQuantity) >= 0) {
                    itemNeedPackageQuantity = itemNeedPackageQuantity.subtract(detailLeftPackageQuantity);
                    detailPickedPkgQuantity = detailLeftPackageQuantity;
                    detailLeftPackageQuantity = BigDecimal.ZERO;
                } else {

                    detailLeftPackageQuantity = detailLeftPackageQuantity.subtract(itemNeedPackageQuantity);
                    detailPickedPkgQuantity = itemNeedPackageQuantity;
                    itemNeedPackageQuantity = BigDecimal.ZERO;
                }
                usedDetailDic.put(detailResponse.getId(), detailLeftPackageQuantity);
                AllocateModel allocateModel = new AllocateModel();
                allocateModel.setPallet(palletMap.get(detailResponse.getId()));
                allocateModel.setInventoryItemDetailId(detailResponse.getId());
                allocateModel.setAllocateQuantity(detailPickedPkgQuantity);
                allocateModel.setShipOrderItemId(shipOrderItem.getId());
                allocateModelList.add(allocateModel);
                if (itemNeedPackageQuantity.compareTo(BigDecimal.ZERO) == 0) {
                    break;
                }

            }
        }
        return allocateModelList;
    }


    @Override
    public List<Map<String, String>> trunkBarCodePreview(long id) throws Exception {
        InventoryItemDetail detail = this.getById(id);
        if (detail == null) {
            throw new Exception("InventoryItemDetail " + id + " doesn't exist");
        }
        if (StringUtils.isEmpty(detail.getM_Str7())) {
            throw new Exception("InventoryItemDetail " + id + " projectNo is empty");
        }
        if (StringUtils.isEmpty(detail.getM_Str12())) {
            throw new Exception("InventoryItemDetail " + id + " deviceNo is empty");
        }
        Material material = this.materialService.getById(detail.getMaterialId());
        if (material == null) {
            throw new Exception("Material " + detail.getMaterialId() + " doesn't exist");
        }
        List<String> barCodeList = new ArrayList<>();

        String[] projectNoArray = detail.getM_Str12().split(",");
        for (String projectNo : projectNoArray) {
            //XM0801,DYH001,P0002043508
            String barCode = "";
            barCode = MessageFormat.format("{0},{1},{2}", detail.getM_Str7(), projectNo, material.getXCode());
            barCodeList.add(barCode);
        }
        List<Map<String, String>> result = BarcodeUtil.getMultipleBarcodes(barCodeList, 0, 0, null);
//        return Collections.emptyList();
        return result;
    }

    @Override
    public List<Long> getAllIdList() {
        List<Long> idList = this.baseMapper.getAllIdList();
        return idList;
    }


    private void checkPackageQuantity(BigDecimal inventoryPackageQuantity, BigDecimal needPackageQuantity) throws Exception {
        if (inventoryPackageQuantity.compareTo(needPackageQuantity) < 0) {
            DecimalFormat df = new DecimalFormat("#0.00");
            String inventoryFormatted = df.format(inventoryPackageQuantity);
            String needFormatted = df.format(needPackageQuantity);
            throw new Exception("库存不足 ,库存数量 - " + inventoryFormatted + " 需求数量 - " + needFormatted);

        }
    }

    private Map<Long, String> getPalletInfo(List<Long> detailIdList) throws Exception {
        InventoryInfoRequest inventoryInfoRequest = new InventoryInfoRequest();
        List<String> sourceFieldList = new ArrayList<>();
        sourceFieldList.add("pallet");
        sourceFieldList.add("inventoryItemDetailId");
        inventoryInfoRequest.setFieldMap(EsRequestPage.setFieldMapByField(sourceFieldList));
        inventoryInfoRequest.setPageIndex(0);
        inventoryInfoRequest.setPageSize(10000);
        inventoryInfoRequest.setDeleted(0);
        inventoryInfoRequest.setInventoryItemDetailIdList(detailIdList);
        PageData<InventoryInfo> pageData = inventoryInfoService.getInventoryInfoPage(inventoryInfoRequest);
        List<InventoryInfo> inventoryInfoList = pageData.getData();
        if (inventoryInfoList == null || inventoryInfoList.size() == 0) {
            throw new Exception("es库存数据异常");
        }

        List<String> palletList = inventoryInfoList.stream().map(p -> p.getPallet()).distinct().collect(Collectors.toList());

        if (CollectionUtils.isEmpty(palletList)) {
            throw new Exception("es库存托盘数据异常");
        }

        //转map
        Map<Long, String> map = inventoryInfoList.stream()
                .collect(Collectors.toMap(
                        InventoryInfo::getInventoryItemDetailId, // Key Mapper
                        item -> item.getPallet()               // Value Mapper (对象本身)
                ));
        return map;
    }


    @Override
    public void importExcelModifyMStr12(HttpServletResponse httpServletResponse, MultipartFile file) throws IOException {
        List<ModifyMStr12Bo> dataList = new ArrayList<ModifyMStr12Bo>();
        HashSet<ModifyMStr12Bo> errorDataSet = new HashSet<ModifyMStr12Bo>();
        final int SAVE_DB_SIZE = 20;
        EasyExcel.read(file.getInputStream(), ModifyMStr12Bo.class, new ReadListener<ModifyMStr12Bo>() {

            /**
             * 这个每一条数据解析都会来调用
             * @param o
             * @param analysisContext
             */
            @Override
            public void invoke(ModifyMStr12Bo o, AnalysisContext analysisContext) {
//                       注意://实体对象设置 lombok 设置    @Accessors(chain = false) 禁用链式调用，否则easyexcel读取时候无法生成实体对象的值
                //跳过空白行,
                if (StringUtils.isNotEmpty(o.getMaterialCode()) && StringUtils.isNotEmpty(o.getMStr7()) && StringUtils.isNotEmpty(o.getMStr12())) {
                    try {
                        if (o.getQuantity().compareTo(BigDecimal.ONE) == 0) {
                            dataList.add(o);
                        } else {
                            o.setRemark("数量大于1");
                            errorDataSet.add(o);
                        }

                    } catch (Exception e) {
                        o.setRemark("更新异常");
                        errorDataSet.add(o);
                    }
                } else {
                    o.setRemark("信息有空");
                    errorDataSet.add(o);
                }


            }

            /**
             *所有的都读取完 回调 ，
             * @param analysisContext
             */
            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {

            }

            @Override
            public void onException(Exception exception, AnalysisContext context) throws Exception {
                int m = 0;
//                        CellDataTypeEnum
                throw exception;
            }
        }).sheet().doRead();

        if (CollectionUtils.isNotEmpty(dataList)) {
            Map<ModifyMStr12BoGroupKey, List<ModifyMStr12Bo>> groupedByCustomKey = dataList.stream().collect(Collectors.groupingBy(ModifyMStr12BoGroupKey::new));

//            Map<ModifyMStr12BoGroupKey, List<ModifyMStr12Bo>> grouped = dataList.stream()
//                    .collect(Collectors.groupingBy(
//                            item -> new ModifyMStr12BoGroupKey(item.getMStr7(), item.getMaterialCode())
//                    ));


            for (ModifyMStr12BoGroupKey key : groupedByCustomKey.keySet()) {
                List<ModifyMStr12Bo> list = groupedByCustomKey.get(key);
                if (CollectionUtils.isNotEmpty(list)) {
                    List<ModifyMStr12Bo> errorlist = errorDataSet.stream().filter(o -> o.getMaterialCode().equals(key.getMaterialCode()) && o.getMStr7().equals(key.getMStr7())).collect(Collectors.toList());

                    if (CollectionUtils.isNotEmpty(errorlist)) {
                        log.info("MStr7 - {} MaterialCode - {} exist incorrect data", key.getMaterialCode(), key.getMStr7());
                        errorDataSet.addAll(list);
                        continue;
                    }


                }
            }
        } else {
            throw new RuntimeException("excel data is empty");
        }

        if (errorDataSet.isEmpty()) {
            modifyMStr12(dataList, errorDataSet);
        }


        saveExcel("ModifyMStr12_error", errorDataSet, ModifyMStr12Bo.class);
//        CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2, future3);
        String fileName = "ModifyMStr12_error";
        prepareResponds(fileName, httpServletResponse);
        EasyExcel.write(httpServletResponse.getOutputStream(), ModifyMStr12Bo.class).sheet("表名称").doWrite(errorDataSet);

    }

    private <T> void saveExcel(String fileName, Collection<T> data, Class<T> cla) {
        // 获取当前工作目录
        String currentWorkingDir = System.getProperty("user.dir");

        // 创建文件夹路径
        File folder = new File(currentWorkingDir + File.separator + "tmp");

        if (!folder.exists()) {
            boolean created = folder.mkdir();
            if (created) {
                log.info("文件夹创建成功: " + folder.getAbsolutePath());
            } else {
                log.info("文件夹创建失败");
            }
        }
        // 写入Excel文件
        String fullName = MessageFormat.format("{0}/{1}.xlsx", folder, fileName);//"C:/temp/example.xlsx"; // 指定保存路径
        EasyExcel.write(fullName, cla)
                .sheet("工作表1") // 设置sheet名称
                .doWrite(data);  // 写入数据
    }

    /**
     * 将文件输出到浏览器(导出)
     */
    private void prepareResponds(String fileName, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8'zh_cn'" + fileName + ExcelTypeEnum.XLSX.getValue());

//        fileName = URLEncoder.encode(fileName, "UTF-8");
//        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setCharacterEncoding("utf-8");
//        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
//        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");


    }

    public void modifyMStr12(List<ModifyMStr12Bo> dataList, HashSet<ModifyMStr12Bo> errorDataSet) {

        try {

            HashSet<ModifyMStr12Bo> currentErrorDataSet = new HashSet<>();
            HashSet<InventoryItemDetailResponse> allDetailSet = new HashSet<>();
            for (ModifyMStr12Bo bo : dataList) {
                Material material = this.materialService.getByCode(bo.getMaterialCode());
                if (material == null) {
                    bo.setRemark("物料不存在");
                    currentErrorDataSet.add(bo);
                    continue;
                }
                bo.setMaterialId(material.getId());
                InventoryItemDetailRequest request = new InventoryItemDetailRequest();
                request.setM_Str7(bo.getMStr7());
                request.setMaterialId(material.getId());
                request.setSearchCount(false);
                request.setPageSize(10000);
                PageData<InventoryItemDetailResponse> pageData = this.getInventoryItemDetailPage(request);
                List<InventoryItemDetailResponse> detailList = pageData.getData();
                allDetailSet.addAll(detailList);
                if (detailList.size() == 0) {
                    bo.setRemark("库存不存在");
                    currentErrorDataSet.add(bo);
                    continue;
                }

                if (detailList.size() > 1) {
                    long count = dataList.stream().filter(p -> bo.getMStr7().equals(p.getMStr7()) && bo.getMaterialCode().equals(p.getMaterialCode())).count();
                    if (detailList.size() != count) {
                        bo.setRemark("找到" + detailList.size() + "条库存,excel " + count + "条");
                        currentErrorDataSet.add(bo);
                    }
                }
            }

            if (!currentErrorDataSet.isEmpty()) {
                errorDataSet.addAll(currentErrorDataSet);
                return;
            }


            for (ModifyMStr12Bo bo : dataList) {
                List<InventoryItemDetailResponse> currentDetailList = allDetailSet.stream().filter(p ->
                        bo.getMStr7().equals(p.getM_Str7())
                                && bo.getMaterialId().equals(p.getMaterialId())
                                && StringUtils.isEmpty(p.getM_Str12())).collect(Collectors.toList());

                //没有空M_Str12()
                if (currentDetailList.isEmpty()) {

                    List<InventoryItemDetailResponse> mStr12DetailList = allDetailSet.stream().filter(p ->
                            bo.getMStr7().equals(p.getM_Str7())
                                    && bo.getMaterialId().equals(p.getMaterialId())
                                    && bo.getMStr12().equals(p.getM_Str12())).collect(Collectors.toList());
                    if (mStr12DetailList.size() == 1) {
                        continue;
                    } else {
                        bo.setRemark("值为M_Str12的库存，有多个");
                        errorDataSet.add(bo);
                        return;
                    }

                }
                InventoryItemDetailResponse detail = currentDetailList.get(0);
                detail.setM_Str12(bo.getMStr12());

                LambdaUpdateWrapper<InventoryItemDetail> updateWrapper = new LambdaUpdateWrapper<InventoryItemDetail>().
                        eq(InventoryItemDetail::getId, detail.getId())
                        .set(InventoryItemDetail::getM_Str12, detail.getM_Str12());
                log.info("InventoryItemDetailId - {} update MStr12 - {}", detail.getId(), detail.getM_Str12());
                //数据没有做修改，影响的行数是0，不然返回1
                boolean re = this.update(null, updateWrapper);

            }
        } catch (Exception ex) {
            log.error("", ex);
        }
    }


    @Override
    public <T> void exportExcelModifyMStrTemplate(HttpServletResponse response, Class<T> cla) throws
            IOException {
        String fileName = cla.getSimpleName();
        prepareResponds(fileName, response);
        // EasyExcel.write(response.getOutputStream(), ProductTest.class).sheet("表名称").doWrite(new ArrayList<ProductTest>());
        //细化设置
        ServletOutputStream outputStream = response.getOutputStream();
        // 获取改类声明的所有字段
        Field[] fields = cla.getDeclaredFields();
        // 响应字段对应的下拉集合
        Map<Integer, String[]> map = new HashMap<>();
        Field field = null;
        List<Field> fieldList = new ArrayList<>();
        // 过滤掉ExcelIgnore的列
        for (int i = 0; i < fields.length; i++) {
            field = fields[i];
            int modifiers = field.getModifiers();
            if (!Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers)) {
                // 解析注解信息
                ExcelIgnore excelIgnore = field.getAnnotation(ExcelIgnore.class);
                if (null == excelIgnore) {
                    fieldList.add(field);
                }
            }

        }

        // 循环判断哪些字段有下拉数据集，并获取
        for (int i = 0; i < fieldList.size(); i++) {
            field = fieldList.get(i);
            // 解析注解信息
            DropDownSetField dropDownSetField = field.getAnnotation(DropDownSetField.class);
            if (null != dropDownSetField) {
                String[] sources = ResoveDropAnnotationUtil.resove(dropDownSetField);
                if (null != sources && sources.length > 0) {
                    map.put(i, sources);
                }
            }
        }


        //多个sheet页写入
        ExcelWriterBuilder builder = new ExcelWriterBuilder();
        builder.autoCloseStream(true);
//        builder.head()
        builder.head(cla);


//        builder.excludeColumnFieldNames()

////        if (flag == 0 || flag == 2) {
//        builder.registerWriteHandler(new ExcelStyleConfig(Lists.newArrayList(20), null, null));
//        builder.head(ProductTest.class);
////        } else {
////            builder.registerWriteHandler(new ExcelStyleConfig(null,null,null));
////            builder.head(GXDetailListLogVO.class);
////        }
        //下拉框
        builder.registerWriteHandler(new DropDownCellWriteHandler(map));
        builder.file(outputStream);

        //不能重命名，重命名就没有XLSX格式后缀
        builder.excelType(ExcelTypeEnum.XLSX);
        ExcelWriter writer = builder.build();
        WriteSheet sheet = EasyExcel.writerSheet(0, cla.getSimpleName()).build();
        List<T> list = new ArrayList<>();
        writer.write(list, sheet);
        writer.finish();
    }

    @Override
    public void downloadErrorData(HttpServletResponse response) throws IOException {
        String currentWorkingDir = System.getProperty("user.dir");
        File folder = new File(currentWorkingDir + File.separator + "tmp");
        // 写入Excel文件
        String fullName = MessageFormat.format("{0}/{1}.xlsx", folder, "ModifyMStr12_error");
        // Excel 文件路径（确保存在）
        File file = new File(fullName);

        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        prepareResponds("ModifyMStr12_error", response);
//        // 设置响应头
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setHeader("Content-Disposition",
//                "attachment; filename=" + URLEncoder.encode("模板.xlsx", StandardCharsets.UTF_8));

        // 写入响应流
        try (InputStream inputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        }
    }


    @Override
    public PageData<InventoryItemDetailResponse> getInventoryItemDetailPage(InventoryItemDetailRequest request) throws
            Exception {
        log.info("getInventoryItemDetailPage - {}", objectMapper.writeValueAsString(request));
        LambdaQueryWrapper<InventoryItemDetail> queryWrapper = new LambdaQueryWrapper<>();
        /**
         * gt: Greater than（大于）
         * ge: Greater than or equal to（大于等于）
         * lt: Less than（小于）
         * le: Less than or equal to（小于等于）
         */
        queryWrapper.gt(InventoryItemDetail::getPackageQuantity, 0);
        if (StringUtils.isNotEmpty(request.getM_Str7())) {
            queryWrapper.eq(InventoryItemDetail::getM_Str7, request.getM_Str7());
        }

        if (!request.getIgnoreDeviceNo()) {
            if (StringUtils.isNotEmpty(request.getM_Str12())) {
                queryWrapper.like(InventoryItemDetail::getM_Str12, request.getM_Str12());
            } else {
                //空过滤  AND (M_Str12 IS NULL OR M_Str12 = ?)
                queryWrapper.and(wrapper -> wrapper
                        .isNull(InventoryItemDetail::getM_Str12)
                        .or()
                        .eq(InventoryItemDetail::getM_Str12, "")
                );
//                queryWrapper.apply("( m_str12 IS NULL OR m_str12 = '')");
            }
        }
        if (StringUtils.isNotEmpty(request.getMaterialCode())) {
            Material material = materialService.getByCode(request.getMaterialCode());
            if (material != null) {
                queryWrapper.eq(InventoryItemDetail::getMaterialId, material.getId());
            }
        }

        if (request.getMaterialId() != null && request.getMaterialId() > 0) {
            queryWrapper.eq(InventoryItemDetail::getMaterialId, request.getMaterialId());
        }


        // 创建分页对象 (当前页, 每页大小)
        Page<InventoryItemDetail> page = new Page<>(request.getPageIndex(), request.getPageSize());

        if (CollectionUtils.isNotEmpty(request.getSortFieldList())) {
            List<OrderItem> orderItems = LambdaFunctionHelper.getWithDynamicSort(request.getSortFieldList());
            page.setOrders(orderItems);
        }

        if (request.getSearchCount() != null) {
            // 关键设置：不执行 COUNT 查询
            page.setSearchCount(request.getSearchCount());
        }

        // 执行分页查询, sqlserver 使用通用表达式 WITH selectTemp AS
        IPage<InventoryItemDetail> inventoryItemDetailPage = this.baseMapper.selectPage(page, queryWrapper);

        // 获取当前页数据
        List<InventoryItemDetail> records = inventoryItemDetailPage.getRecords();
        long total = inventoryItemDetailPage.getTotal();

        List<InventoryItemDetailResponse> inventoryItemDetailResponseList = records.stream().map(p -> {
            InventoryItemDetailResponse response = new InventoryItemDetailResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<InventoryItemDetailResponse> pageData = new PageData<>();
        pageData.setData(inventoryItemDetailResponseList);
        pageData.setCount(total);
        return pageData;
    }

}




