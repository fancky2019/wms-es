package gs.com.gses.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.ftp.FtpConfig;
import gs.com.gses.ftp.FtpService;
import gs.com.gses.model.bo.wms.ExcelInspectionData;
import gs.com.gses.model.bo.wms.InspectionData;
import gs.com.gses.model.entity.ApplyReceiptOrderItem;
import gs.com.gses.model.request.wms.ApplyReceiptOrderItemRequest;
import gs.com.gses.model.request.wms.MaterialRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.MaterialResponse;
import gs.com.gses.service.ApplyReceiptOrderItemService;
import gs.com.gses.mapper.ApplyReceiptOrderItemMapper;
import gs.com.gses.service.MaterialService;
import gs.com.gses.utility.ExcelUpdater;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lirui
 * @description 针对表【ApplyReceiptOrderItem】的数据库操作Service实现
 * @createDate 2025-09-03 16:25:44
 */
@Slf4j
@Service
public class ApplyReceiptOrderItemServiceImpl extends ServiceImpl<ApplyReceiptOrderItemMapper, ApplyReceiptOrderItem>
        implements ApplyReceiptOrderItemService {


    @Autowired
    private MaterialService materialService;

    @Autowired
    private FtpService ftpService;

    @Autowired
    private FtpConfig ftpConfig;


    @Autowired
    private Executor threadPoolExecutor;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String inspection(MultipartFile[] files, ApplyReceiptOrderItemRequest applyReceiptOrderItemRequest) throws Exception {
        //        List<String> fileNames = saveFiles(files);
        //获取body中的参数
//            String value = (String)request.getAttribute("paramName");
        String name = applyReceiptOrderItemRequest.getComments();
        StopWatch stopWatch = new StopWatch("inspection");
        stopWatch.start("AnalysisAndSave");

        if (files == null || files.length == 0) {
            throw new Exception("files is null");
        }
        String currentWorkingDir = System.getProperty("user.dir");
        String saveBasePath = MessageFormat.format("{0}/{1}/{2}", currentWorkingDir, "tmp", buildDateBasedPath());
        List<ExcelInspectionData> excelInspectionDataList = new ArrayList<>();
        for (MultipartFile file : files) {
            try (InputStream inputStream = file.getInputStream()) {
                ExcelInspectionData excelInspectionData = parseSpecificCell(inputStream);
                List<InspectionData> dataList = excelInspectionData.getInspectionDataList();
                HashMap<String, Object> inspectionResultMap = new HashMap<>();
                for (InspectionData data : dataList) {
                    boolean number = NumberUtils.isCreatable(data.getStandardValue());
                    String inspectionResult = "N";

                    if (!number) {
                        inspectionResult = data.getActualValue().equals(data.getStandardValue()) ? "Y" : "N";
                    } else {
                        BigDecimal actualValue = NumberUtils.createBigDecimal(data.getActualValue());
                        BigDecimal standardValue = NumberUtils.createBigDecimal(data.getStandardValue());
                        BigDecimal upperLimit = standardValue.add(NumberUtils.createBigDecimal(data.getUpperLimit()));
                        BigDecimal lowerLimit = standardValue.add(NumberUtils.createBigDecimal(data.getLowerLimit()));

                        inspectionResult = actualValue.compareTo(upperLimit) <= 0 &&
                                actualValue.compareTo(lowerLimit) >= 0 ? "Y" : "N";
                    }
                    data.setInspectionResult(inspectionResult);
                    inspectionResultMap.put("D" + data.getRowIndex(), inspectionResult);
                }
                boolean unqualified = inspectionResultMap.values().stream().anyMatch("N"::equals);
                excelInspectionData.setUnqualified(unqualified);
                inspectionResultMap.put("L1", unqualified ? "N" : "Y");
                String materialPath = MessageFormat.format("{0}/{1}/", saveBasePath, excelInspectionData.getMaterialCode());
                Path path = Paths.get(materialPath);
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                }
                String outputPath = MessageFormat.format("{0}/{1}", materialPath, file.getOriginalFilename());
                byte[] bytes = file.getBytes();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ExcelUpdater.updateCells(byteArrayInputStream, outputPath, inspectionResultMap);
                excelInspectionData.setOutputPath(outputPath);
                excelInspectionData.setOriginalFilename(file.getOriginalFilename());
                excelInspectionDataList.add(excelInspectionData);
            } catch (Exception e) {
                throw e;
            }
        }
        stopWatch.stop();
        log.info("AnalysisAndSave: {}ms", stopWatch.getLastTaskTimeMillis());
        stopWatch.start("FtpUpload");
        String rootPath = ftpConfig.getBasePath();
        String basePath = rootPath + buildDateBasedPath();
        for (ExcelInspectionData excelInspectionData : excelInspectionDataList) {
            String materialPath = MessageFormat.format("{0}{1}/{2}", basePath, excelInspectionData.getMaterialCode(), excelInspectionData.getOriginalFilename());
            this.ftpService.uploadFile(excelInspectionData.getOutputPath(), materialPath);
        }
        log.info("ftp upload complete");
        stopWatch.stop();
        log.info("FtpUpload: {}ms", stopWatch.getLastTaskTimeMillis());
        stopWatch.start("UpdateInspectionQuantity");
        updateInspectionQuantity(excelInspectionDataList);
        stopWatch.stop();
        log.info("UpdateInspectionQuantity: {}ms", stopWatch.getLastTaskTimeMillis());
        log.info("Inspection: {}ms", stopWatch.getTotalTimeMillis());
//        List<String> filePathList = new ArrayList<>();
//        String filePath = "";
//        for (MultipartFile file : files) {
//            filePath = basePath + file.getOriginalFilename();
//            Boolean success = ftpService.uploadFile(file.getBytes(), filePath);
//            filePathList.add(filePath);
//        }

        //保存到本地
//        List<String> fileNames = saveFiles(files);

        return "";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String inspectionOptimization(MultipartFile[] files, ApplyReceiptOrderItemRequest applyReceiptOrderItemRequest) throws Exception {
//        List<String> fileNames = saveFiles(files);
        //获取body中的参数
//            String value = (String)request.getAttribute("paramName");
        String name = applyReceiptOrderItemRequest.getComments();
        if (files == null || files.length == 0) {
            throw new Exception("files is null");
        }
        String currentWorkingDir = System.getProperty("user.dir");
        String saveBasePath = MessageFormat.format("{0}/{1}/{2}", currentWorkingDir, "tmp", buildDateBasedPath());
        List<ExcelInspectionData> excelInspectionDataList = new ArrayList<>();
        List<CompletableFuture<ExcelInspectionData>> futuresList = new ArrayList<>();
        for (MultipartFile file : files) {
            CompletableFuture<ExcelInspectionData> future = CompletableFuture.supplyAsync(() -> {


                ExcelInspectionData excelInspectionData = new ExcelInspectionData();
                try (InputStream inputStream = file.getInputStream()) {
//                    Integer.parseInt("123t");
                    excelInspectionData = parseSpecificCell(inputStream);
                    List<InspectionData> dataList = excelInspectionData.getInspectionDataList();
                    HashMap<String, Object> inspectionResultMap = new HashMap<>();
                    for (InspectionData data : dataList) {
                        boolean number = NumberUtils.isCreatable(data.getStandardValue());
                        String inspectionResult = "N";

                        if (!number) {
                            inspectionResult = data.getActualValue().equals(data.getStandardValue()) ? "Y" : "N";
                        } else {
                            BigDecimal actualValue = NumberUtils.createBigDecimal(data.getActualValue());
                            BigDecimal standardValue = NumberUtils.createBigDecimal(data.getStandardValue());
                            BigDecimal upperLimit = standardValue.add(NumberUtils.createBigDecimal(data.getUpperLimit()));
                            BigDecimal lowerLimit = standardValue.add(NumberUtils.createBigDecimal(data.getLowerLimit()));

                            inspectionResult = actualValue.compareTo(upperLimit) <= 0 &&
                                    actualValue.compareTo(lowerLimit) >= 0 ? "Y" : "N";
                        }
                        data.setInspectionResult(inspectionResult);
                        inspectionResultMap.put("D" + data.getRowIndex(), inspectionResult);
                    }
                    boolean unqualified = inspectionResultMap.values().stream().anyMatch("N"::equals);
                    excelInspectionData.setUnqualified(unqualified);
                    inspectionResultMap.put("L1", unqualified ? "N" : "Y");
                    String materialPath = MessageFormat.format("{0}/{1}/", saveBasePath, excelInspectionData.getMaterialCode());
                    Path path = Paths.get(materialPath);
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                    }
                    String outputPath = MessageFormat.format("{0}/{1}", materialPath, file.getOriginalFilename());
                    byte[] bytes = file.getBytes();
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                    ExcelUpdater.updateCells(byteArrayInputStream, outputPath, inspectionResultMap);
                    excelInspectionData.setOutputPath(outputPath);
                    excelInspectionData.setOriginalFilename(file.getOriginalFilename());
//                    excelInspectionDataList.add(excelInspectionData);
                } catch (Exception e) {
                    log.error("", e);
                    excelInspectionData.setErrMsg(e.getMessage());
                }
                return excelInspectionData;
            }, threadPoolExecutor);


            futuresList.add(future);
        }
        CompletableFuture<Void> allDone = CompletableFuture.allOf(
                futuresList.toArray(new CompletableFuture[0])
        );

        CompletableFuture<List<ExcelInspectionData>> resultsFuture1 = allDone.thenApply(v -> {
            // 这里的join()不会阻塞，因为所有任务都已经完成
            return futuresList.stream()
                    .map(CompletableFuture::join)  //get() join()  立即获取结果 // 从每个future中提取结果值
                    .collect(Collectors.toList());
        });
        excelInspectionDataList = resultsFuture1.join();

        if (CollectionUtils.isEmpty(excelInspectionDataList)) {
            throw new RuntimeException("解析excel 异常");
        }


        String rootPath = ftpConfig.getBasePath();
        String basePath = rootPath + buildDateBasedPath();

        for (ExcelInspectionData excelInspectionData : excelInspectionDataList) {
            String materialPath = MessageFormat.format("{0}{1}/{2}", basePath, excelInspectionData.getMaterialCode(), excelInspectionData.getOriginalFilename());
            this.ftpService.uploadFile(excelInspectionData.getOutputPath(), materialPath);
        }
        log.info("ftp upload complete");

        updateInspectionQuantity(excelInspectionDataList);

        return "";
    }


    /**
     * 解析指定单元格的值
     * @param inputStream 文件输入流
     *  targetRow 目标行号（从0开始计数）
     *  targetColumn 目标列号（从0开始计数，E列是第4列）
     */
    public ExcelInspectionData parseSpecificCell(InputStream inputStream) {
        int infoRow = 1;
        int dataRow = 3;
        List<InspectionData> dataList = new ArrayList<>();
        ExcelInspectionData excelInspectionData = new ExcelInspectionData();
        excelInspectionData.setInspectionDataList(dataList);

        // 创建读取监听器
        ReadListener<Object> listener = new ReadListener<Object>() {
            @Override
            public void invoke(Object data, AnalysisContext context) {
                // 获取当前行号（从0开始）
                int currentRowIndex = context.readRowHolder().getRowIndex();
                if (infoRow == currentRowIndex) {
                    if (data instanceof Map) {
                        Map dataMap = (Map) data;
                        excelInspectionData.setMaterialCode(dataMap.get(4).toString());
                        excelInspectionData.setProjectNo(dataMap.get(5).toString());
                        excelInspectionData.setBatchNo(dataMap.get(6).toString());
                        excelInspectionData.setDeviceNo(dataMap.get(7).toString());
                    }
                }
                // 检查是否是目标行（第二行，索引为1） 索引从0开始  A1
                else if (currentRowIndex >= dataRow) {
                    if (data instanceof Map) {

                        InspectionData inspectionData = new InspectionData();
                        Map dataMap = (Map) data;
//                        Object value = dataMap.get(2);
//                        String targetCellValue1 = value != null ? value.toString() : null;
//                        log.info("parseSpecificCell [" + currentRowIndex + ", " + targetColumn + "] 的值: " + targetCellValue1);
//                        if (StringUtils.isEmpty(targetCellValue1)) {
//                            String msg = MessageFormat.format("Can't find cell[{0},{1}] value", currentRowIndex, targetColumn);
//                            return;
//                        }

                        Object value = dataMap.get(2);
                        if (value == null) {
                            return;
                        }
                        inspectionData.setRowIndex(currentRowIndex + 1);
                        inspectionData.setActualValue(dataMap.get(2).toString());
                        inspectionData.setStandardValue(dataMap.get(5).toString());
                        inspectionData.setUpperLimit(dataMap.get(6).toString());
                        inspectionData.setLowerLimit(dataMap.get(7).toString());
                        dataList.add(inspectionData);
                    }


                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                if (CollectionUtils.isEmpty(dataList)) {
                    System.out.println("未找到指定单元格的值");
                }
            }
        };


        EasyExcel.read(inputStream, listener)
                .sheet(0) // 读取第一个sheet（Sheet1）
                .headRowNumber(0) // 第一行是表头，从第0行开始读数据
                .doRead();
        return excelInspectionData;
    }

    /**
     * 构建基于日期的相对路径
     */
    public String buildDateBasedPath() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/");
        return today.format(formatter);
    }

    private BigDecimal initCellValue(Object value, int currentRowIndex, int targetColumn) {
        String targetCellValue1 = value != null ? value.toString() : null;
        log.info("parseSpecificCell [" + currentRowIndex + ", " + targetColumn + "] 的值: " + targetCellValue1);
        if (StringUtils.isEmpty(targetCellValue1)) {
            String msg = MessageFormat.format("Can't find cell[{0},{1}] value", currentRowIndex, targetColumn);
            return null;
        }
        return new BigDecimal(targetCellValue1);
    }

    /**
     * 模板确定
     * 模版写入 map
     * @param filePath
     * @param data1
     */
    private void writeExcelWithTemplate(String filePath, List<List<String>> data1) {
        String templateFileName = "template.xlsx";
        String outputFileName = "filled_template.xlsx";

        // 准备填充数据
        Map<String, Object> data = new HashMap<>();
        data.put("name", "张三");
        data.put("age", 25);
        data.put("department", "技术部");
        data.put("salary", 15000.0);
        data.put("remark", "优秀员工"); // 这个占位符在模板中不存在，会忽略

        // 使用模板填充
        EasyExcel.write(outputFileName)
                .withTemplate(templateFileName)
                .sheet()
                .doFill(data);

        System.out.println("模板填充完成：" + outputFileName);
    }

    @Override
    public void specificCellWriteExample() throws Exception {

        String currentWorkingDir = System.getProperty("user.dir");
        File folder = new File(currentWorkingDir + File.separator + "tmp");
        // 写入Excel文件
        String fileName = MessageFormat.format("{0}/{1}.xlsx", folder, "P0002051555检验表");
        String outputPath = MessageFormat.format("{0}/{1}.xlsx", folder, "P0002051555检验表导出");
        Map<String, Object> updates = new HashMap<>();
//        行索引从1开始
        updates.put("L1", "N");
        for (int i = 4; i <= 19; i++) {
            updates.put("D" + i, "N");
        }
        ExcelUpdater.updateCells(fileName, outputPath, updates);

    }


    private void updateInspectionQuantity(List<ExcelInspectionData> excelInspectionDataList) throws Exception {

        if (CollectionUtils.isEmpty(excelInspectionDataList)) {
            throw new Exception("excelInspectionDataList is empty");
        }
        List<String> materialCodeList = excelInspectionDataList.stream().map(p -> p.getMaterialCode()).distinct().collect(Collectors.toList());
        MaterialRequest materialRequest = new MaterialRequest();
        materialRequest.setSearchCount(false);
        materialRequest.setMaterialCodeList(materialCodeList);
        materialRequest.setPageSize(Integer.MAX_VALUE);
        PageData<MaterialResponse> materialPageData = materialService.getMaterialPage(materialRequest);
        List<MaterialResponse> materialResponseList = materialPageData.getData();
        if (CollectionUtils.isEmpty(materialResponseList)) {
            String msg = MessageFormat.format("Can not get material info by codes {0}", String.join(",", materialCodeList));
            throw new Exception(msg);
        }

        Map<String, MaterialResponse> materialCodeMap = materialResponseList.stream().collect(Collectors.toMap(gs.com.gses.model.response.wms.MaterialResponse::getXCode, p -> p));
        for (String materialCode : materialCodeList) {
            if (!materialCodeMap.containsKey(materialCode)) {
                String msg = MessageFormat.format("Can not get material info by code {0}", materialCode);
                throw new Exception(msg);
            }
        }

        Map<Long, MaterialResponse> materialMap = materialResponseList.stream().collect(Collectors.toMap(MaterialResponse::getId, p -> p));
        LambdaQueryWrapper<ApplyReceiptOrderItem> wrapper = new LambdaQueryWrapper<>();

        // 构建 OR 条件
        for (ExcelInspectionData data : excelInspectionDataList) {

            Long materialId = materialCodeMap.get(data.getMaterialCode()).getId();
            wrapper.or(w -> w
                    .eq(StringUtils.isNotEmpty(data.getMaterialCode()), ApplyReceiptOrderItem::getMaterialId, materialId)
                    .eq(StringUtils.isNotEmpty(data.getProjectNo()), ApplyReceiptOrderItem::getM_Str7, data.getProjectNo())
                    .like(StringUtils.isNotEmpty(data.getDeviceNo()), ApplyReceiptOrderItem::getM_Str12, data.getDeviceNo())
            );
        }

        List<ApplyReceiptOrderItem> itemList = baseMapper.selectList(wrapper);
        //多个字段分组
        MultiKeyMap<MultiKey, List<ApplyReceiptOrderItem>> multiKeyMap = new MultiKeyMap<>();
        for (ApplyReceiptOrderItem p : itemList) {
            MultiKey key = new MultiKey<>(p.getMaterialId(), p.getM_Str7(), p.getM_Str12());
            List<ApplyReceiptOrderItem> group = multiKeyMap.get(key);
            if (group == null) {
                group = new ArrayList<>();
                multiKeyMap.put(key, group);
            }
            group.add(p);
        }

        for (MultiKey multiKey : multiKeyMap.keySet()) {
            List<ApplyReceiptOrderItem> list = multiKeyMap.get(multiKey);
            if (list.size() > 1) {
                throw new Exception("multiple ApplyReceiptOrderItem - " + StringUtils.join(multiKey.getKeys(), ","));
            }
        }

        List<ApplyReceiptOrderItem> updatedItemList = new ArrayList<>();
        for (ApplyReceiptOrderItem item : itemList) {
            MaterialResponse materialResponse = materialMap.get(item.getMaterialId());
            String materialCode = materialResponse.getXCode();
            String projectNo = item.getM_Str7();
            String deviceNo = item.getM_Str12();
            String batchNo = item.getBatchNo();

            Stream<ExcelInspectionData> stream = excelInspectionDataList.stream().filter(p -> p.getMaterialCode().equals(materialCode));
            if (StringUtils.isNotEmpty(projectNo)) {
                stream = stream.filter(p -> projectNo.equals(p.getProjectNo()));
            }
            if (StringUtils.isNotEmpty(deviceNo)) {
                stream = stream.filter(p -> deviceNo.contains(p.getDeviceNo()));
            }
            if (StringUtils.isNotEmpty(batchNo)) {
                stream = stream.filter(p -> batchNo.equals(p.getBatchNo()));
            }

            List<ExcelInspectionData> currentDataList = stream.collect(Collectors.toList());
            if (CollectionUtils.isEmpty(currentDataList)) {
                throw new Exception("Can not find excel data by materialCode:" + materialCode + ",projectNo:" + projectNo + ",deviceNo:" + deviceNo);
            }

            //合格的
            currentDataList = currentDataList.stream().filter(p -> !p.getUnqualified()).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(currentDataList)) {
                log.info("not qualified excel data by materialCode:" + materialCode + ",projectNo:" + projectNo + ",deviceNo:" + deviceNo);
                continue;
            }
            int qualifiedCount = currentDataList.size();
            if ("N".equals(item.getM_Str20())) {
                String msg = MessageFormat.format("ApplyReceiptOrderItem - {0} is exempt from inspection ", item.getId());
                throw new Exception(msg);

            }
            BigDecimal inspectionItemQuantity = BigDecimal.ZERO;
            if (StringUtils.isNotEmpty(item.getM_Str21())) {
                boolean number = NumberUtils.isCreatable(item.getM_Str21());
                if (number) {
                    inspectionItemQuantity = NumberUtils.createBigDecimal(item.getM_Str21());
                }
            }
            BigDecimal totalQuantity = inspectionItemQuantity.add(BigDecimal.valueOf(qualifiedCount));
            BigDecimal allNeed = item.getAllocatedNumber().add(item.getWaitAllocateNumber());
            if (totalQuantity.compareTo(allNeed) > 0) {

                String msg = MessageFormat.format("ApplyReceiptOrderItem - {0}  exceed AllWaitAllocateNumber ", item.getId());
                throw new Exception(msg);

            }
            item.setM_Str21(totalQuantity.toString());
            updatedItemList.add(item);
        }
        if (CollectionUtils.isEmpty(updatedItemList)) {
            log.info("updatedItemList is empty");
            return;
        }

        for (ApplyReceiptOrderItem item : updatedItemList) {
            LambdaUpdateWrapper<ApplyReceiptOrderItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ApplyReceiptOrderItem::getId, item.getId())
                    .set(ApplyReceiptOrderItem::getM_Str21, item.getM_Str21())
            ;
            boolean result = this.update(null, updateWrapper);
            if (!result) {
                throw new Exception("update inspectionItemQuantity fail");
            }
        }
    }


}




