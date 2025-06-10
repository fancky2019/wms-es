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
import gs.com.gses.easyecel.DropDownSetField;
import gs.com.gses.easyecel.ResoveDropAnnotationUtil;
import gs.com.gses.easyecel.handler.DropDownCellWriteHandler;
import gs.com.gses.model.bo.ModifyMStr12BoGroupKey;
import gs.com.gses.model.entity.*;
import gs.com.gses.mapper.InventoryItemDetailMapper;
import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import gs.com.gses.model.bo.ModifyMStr12Bo;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.InventoryItemDetailResponse;
import gs.com.gses.service.InventoryItemDetailService;
import gs.com.gses.service.InventoryItemService;
import gs.com.gses.service.InventoryService;
import gs.com.gses.service.MaterialService;
import gs.com.gses.utility.LambdaFunctionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    public Boolean checkDetailExist(InventoryItemDetailRequest request) throws Exception {

        if (StringUtils.isEmpty(request.getM_Str7())) {
            throw new Exception("m_Str7 is null");
        }
        if (StringUtils.isEmpty(request.getM_Str12())) {
            throw new Exception("m_Str12 is null");
        }
        if (StringUtils.isEmpty(request.getMaterialCode())) {
            throw new Exception("materialCode is null");
        }
        request.setSearchCount(false);

        PageData<InventoryItemDetailResponse> page = getInventoryItemDetailPage(request);
        int size = page.getData().size();
        if (size == 0) {
            throw new Exception("Can't get inventoryItemDetail info by m_Str7 ,m_Str12,materialCode");
        }
        if (size > 1) {
            throw new Exception("Get more than one inventoryItemDetail info by  m_Str7 ,m_Str12,materialCode");
        }
        InventoryItemDetailResponse inventoryItemDetailResponse = page.getData().get(0);
        InventoryItem inventoryItem = this.inventoryItemService.getById(inventoryItemDetailResponse.getInventoryItemId());
        if (inventoryItem == null) {
            String str = MessageFormat.format("inventoryItem - {0} lost", inventoryItemDetailResponse.getInventoryItemId().toString());
            throw new Exception(str);
        }
        Inventory inventory = this.inventoryService.getById(inventoryItem.getInventoryId());
        if (inventory == null) {
            String str = MessageFormat.format("Inventory - {0} lost", inventoryItem.getInventoryId().toString());
            throw new Exception(str);
        }
        request.setId(inventoryItemDetailResponse.getId());
        request.setMaterialId(inventoryItemDetailResponse.getMaterialId());
        request.setPallet(inventory.getPallet());
        return true;
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
        }

        modifyMStr12(dataList, errorDataSet);

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

            List<InventoryItemDetailResponse> allDetailList = new ArrayList<>();
            for (ModifyMStr12Bo bo : dataList) {
                Material material = this.materialService.getByCode(bo.getMaterialCode());
                if (material == null) {
                    bo.setRemark("物料不存在");
                    errorDataSet.add(bo);
                    return;
                }
                bo.setMaterialId(material.getId());
                InventoryItemDetailRequest request = new InventoryItemDetailRequest();
                request.setM_Str7(bo.getMStr7());
                request.setMaterialId(material.getId());
                request.setSearchCount(false);
                PageData<InventoryItemDetailResponse> pageData = this.getInventoryItemDetailPage(request);
                List<InventoryItemDetailResponse> detailList = pageData.getData();
                allDetailList.addAll(detailList);
            }

            if (allDetailList.size() != dataList.size()) {
                log.info("Detail's count don't equal bo's count  ");
                String remark = MessageFormat.format("库存条数 {0} excel 条数 {1}", allDetailList.size(), dataList.size());
                dataList.forEach(p ->
                {
                    p.setRemark(remark);
                });
                errorDataSet.addAll(dataList);
                return;
            }

            for (ModifyMStr12Bo bo : dataList) {
                List<InventoryItemDetailResponse> currentetailList = allDetailList.stream().filter(p -> bo.getMStr7().equals(p.getM_Str7()) && bo.getMaterialId().equals(p.getMaterialId())).collect(Collectors.toList());
                if (currentetailList.size() != 1) {
                    String msg = MessageFormat.format("MStr7 - {0} MaterialCode - {1} 有多条库存", bo.getMaterialCode(), bo.getMStr7());
                    bo.setRemark(msg);
                    errorDataSet.add(bo);
                    continue;
                }

                InventoryItemDetailResponse detail = currentetailList.get(0);
                if (StringUtils.isEmpty(detail.getM_Str12())) {
                    LambdaUpdateWrapper<InventoryItemDetail> updateWrapper = new LambdaUpdateWrapper<InventoryItemDetail>().eq(InventoryItemDetail::getId, detail.getId()).set(InventoryItemDetail::getM_Str12, bo.getMStr12());
                    //数据没有做修改，影响的行数是0，不然返回1
                    boolean re = this.update(null, updateWrapper);
                } else {
                    bo.setRemark("没有M_Str12为空的库存");
                    errorDataSet.add(bo);
                }
            }
        } catch (Exception ex) {
            log.error("", ex);
        }
    }


    @Override
    public <T> void exportExcelModifyMStrTemplate(HttpServletResponse response, Class<T> cla) throws IOException {
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

        prepareResponds("ModifyMStr12_error",response);
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
    public PageData<InventoryItemDetailResponse> getInventoryItemDetailPage(InventoryItemDetailRequest request) throws Exception {
        LambdaQueryWrapper<InventoryItemDetail> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotEmpty(request.getM_Str7())) {
            queryWrapper.eq(InventoryItemDetail::getM_Str7, request.getM_Str7());
        }
        if (StringUtils.isNotEmpty(request.getM_Str12())) {
            queryWrapper.like(InventoryItemDetail::getM_Str12, request.getM_Str12());
        }
        if (StringUtils.isNotEmpty(request.getMaterialCode())) {
            Material material = materialService.getByCode(request.getMaterialCode());
            if (material != null) {
                queryWrapper.eq(InventoryItemDetail::getMaterialId, material.getId());

            }
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




