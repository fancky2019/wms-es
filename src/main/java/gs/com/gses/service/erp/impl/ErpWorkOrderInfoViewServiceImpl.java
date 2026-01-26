package gs.com.gses.service.erp.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import gs.com.gses.easyecel.DropDownSetField;
import gs.com.gses.easyecel.ExcelStyleConfig;
import gs.com.gses.easyecel.ResoveDropAnnotationUtil;
import gs.com.gses.easyecel.handler.DropDownCellWriteHandler;
import gs.com.gses.model.entity.*;
import gs.com.gses.model.entity.erp.ErpWorkOrderInfoView;
import gs.com.gses.mapper.erp.ErpWorkOrderInfoViewMapper;
import gs.com.gses.model.request.erp.ErpWorkOrderInfoViewRequest;
import gs.com.gses.model.request.wms.ApplyShipOrderRequest;
import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.erp.ErpWorkOrderInfoViewResponse;
import gs.com.gses.model.response.wms.ApplyShipOrderResponse;
import gs.com.gses.model.response.wms.InventoryItemDetailResponse;
import gs.com.gses.model.response.wms.TruckOrderItemResponse;
import gs.com.gses.multipledatasource.DataSource;
import gs.com.gses.multipledatasource.DataSourceType;
import gs.com.gses.service.ApplyShipOrderItemService;
import gs.com.gses.service.ApplyShipOrderService;
import gs.com.gses.service.MaterialService;
import gs.com.gses.service.erp.ErpWorkOrderInfoViewService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【ERP_WORKORDERINFO】的数据库操作Service实现
 * @createDate 2026-01-19 14:31:07
 */
@Slf4j
@Service
public class ErpWorkOrderInfoViewServiceImpl extends ServiceImpl<ErpWorkOrderInfoViewMapper, ErpWorkOrderInfoView> implements ErpWorkOrderInfoViewService {


    @Autowired
    private MaterialService materialService;


    @Autowired
    private ApplyShipOrderService applyShipOrderService;

    @Autowired
    private ApplyShipOrderItemService applyShipOrderItemService;


    @DataSource(DataSourceType.THIRD)
    @Override
    public PageData<ErpWorkOrderInfoViewResponse> getErpWorkOrderInfoViewPage(ErpWorkOrderInfoViewRequest request) throws Exception {
//        String workOrderCode = "GS-AS03-2401240103";
//        LambdaQueryWrapper<ErpWorkOrderInfoView> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(ErpWorkOrderInfoView::getWorkOrderCode, workOrderCode);
//        List<ErpWorkOrderInfoView> workOrderCodeDataList = this.list(queryWrapper);
//        int n = 0;
        LambdaQueryWrapper<ErpWorkOrderInfoView> workOrderInfoViewQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(request.getWorkOrderCode())) {
            // StringUtils.split(request.getWorkOrderCode(), "|")
            List<String> workOrderCodeList = Arrays.asList(request.getWorkOrderCode().split("\\|"));
            workOrderInfoViewQueryWrapper.in(ErpWorkOrderInfoView::getWorkOrderCode, workOrderCodeList);
        }
        if (StringUtils.isNotEmpty(request.getApplyCode())) {
            workOrderInfoViewQueryWrapper.like(ErpWorkOrderInfoView::getApplyCode, request.getApplyCode());
        }

        if (StringUtils.isNotEmpty(request.getMaterialCode())) {
            workOrderInfoViewQueryWrapper.like(ErpWorkOrderInfoView::getMaterialCode, request.getMaterialCode());
        }

        if (StringUtils.isNotEmpty(request.getBatchNo())) {
            workOrderInfoViewQueryWrapper.like(ErpWorkOrderInfoView::getBatchNo, request.getBatchNo());
        }

        if (StringUtils.isNotEmpty(request.getProjectNo())) {
            workOrderInfoViewQueryWrapper.like(ErpWorkOrderInfoView::getProjectNo, request.getProjectNo());
        }

        if (StringUtils.isNotEmpty(request.getProjectName())) {
            workOrderInfoViewQueryWrapper.like(ErpWorkOrderInfoView::getProjectName, request.getProjectName());
        }
        if (StringUtils.isNotEmpty(request.getInBoundCode())) {
            workOrderInfoViewQueryWrapper.like(ErpWorkOrderInfoView::getInBoundCode, request.getInBoundCode());
        }

        if (StringUtils.isNotEmpty(request.getInBoundStatus())) {
            workOrderInfoViewQueryWrapper.like(ErpWorkOrderInfoView::getInBoundStatus, request.getInBoundStatus());
        }


        // 创建分页对象 (当前页, 每页大小)
        Page<ErpWorkOrderInfoView> page = new Page<>(request.getPageIndex(), request.getPageSize());

        if (request.getSearchCount() != null) {
            // 关键设置：不执行 COUNT 查询
            page.setSearchCount(request.getSearchCount());
        }

        // 执行分页查询, sqlserver 使用通用表达式 WITH selectTemp AS
        IPage<ErpWorkOrderInfoView> erpWorkOrderInfoViewPage = this.baseMapper.selectPage(page, workOrderInfoViewQueryWrapper);

        // 获取当前页数据
        List<ErpWorkOrderInfoView> erpWorkOrderInfoViewList = erpWorkOrderInfoViewPage.getRecords();
        long total = erpWorkOrderInfoViewPage.getTotal();
        if (CollectionUtils.isEmpty(erpWorkOrderInfoViewPage.getRecords())) {
            log.info("No records found");
            return PageData.getDefault();
        }

        List<ErpWorkOrderInfoViewResponse> erpWorkOrderInfoViewResponseList = erpWorkOrderInfoViewList.stream().map(p -> {
            ErpWorkOrderInfoViewResponse response = new ErpWorkOrderInfoViewResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        List<String> applyShipOrderList = erpWorkOrderInfoViewResponseList.stream().map(p -> p.getApplyCode()).distinct().collect(Collectors.toList());
        ApplyShipOrderRequest applyShipOrderRequest = new ApplyShipOrderRequest();
        applyShipOrderRequest.setPageIndex(1);
        applyShipOrderRequest.setPageSize(Integer.MAX_VALUE);
        applyShipOrderRequest.setSearchCount(false);
        applyShipOrderRequest.setXCodeList(applyShipOrderList);
        PageData<ApplyShipOrderResponse> applyShipOrderPage = this.applyShipOrderService.getApplyShipOrderPage(applyShipOrderRequest);
        if (CollectionUtils.isEmpty(applyShipOrderPage.getData())) {
            throw new Exception("Can not get applyShipOrder data");
        }

        List<ApplyShipOrderResponse> applyShipOrderResponseList = applyShipOrderPage.getData();

        List<String> materialCodeList = erpWorkOrderInfoViewResponseList.stream().map(p -> p.getMaterialCode()).collect(Collectors.toList());
        List<Material> materialList = this.materialService.getByCodeList(materialCodeList);
        Map<String, Material> materialCodeMap = materialList.stream().collect(Collectors.toMap(p -> p.getXCode(), p -> p));
//        LambdaQueryWrapper<ApplyShipOrderItem> applyShipOrderItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Map<String, List<Long>> workOrderApplyCodeMap = new HashMap<>();
//        applyShipOrderItemLambdaQueryWrapper.and(qw -> {
//            for (ErpWorkOrderInfoView query : erpWorkOrderInfoViewList) {
//                Material material = materialCodeMap.get(query.getMaterialCode());
//                List<Long> currentApplyShipOrderIdList = applyShipOrderResponseList.stream().filter(p -> p.getXCode().contains(query.getApplyCode())).map(p -> p.getId()).collect(Collectors.toList());
//                if (CollectionUtils.isEmpty(currentApplyShipOrderIdList)) {
//                    //Exception 抛不出来
//                    //因为 Lambda 表达式中的代码块只能抛出 RuntimeException 或其子类，不能抛出受检异常（checked exception）。
//                    throw new RuntimeException("Can't get ApplyShipOrder by ApplyCode " + query.getApplyCode());
//                }
//                workOrderApplyCodeMap.put(query.getApplyCode(), currentApplyShipOrderIdList);
//                for (Long applyShipOrderId : currentApplyShipOrderIdList) {
//                    qw.or(w -> {
//                        w.eq(ApplyShipOrderItem::getApplyShipOrderId, applyShipOrderId);
//                        w.eq(ApplyShipOrderItem::getMaterialId, material.getId());
//                    });
//                }
//            }
//        });
//
//
        List<ApplyShipOrderItem> applyShipOrderItemList = this.applyShipOrderItemService.getByApplyMaterialIdBatch(erpWorkOrderInfoViewResponseList, applyShipOrderResponseList, materialCodeMap, workOrderApplyCodeMap);


        for (ErpWorkOrderInfoViewResponse workOrderInfoView : erpWorkOrderInfoViewResponseList) {
            Material material = materialCodeMap.get(workOrderInfoView.getMaterialCode());
            List<Long> applyShipOrderIdList = workOrderApplyCodeMap.get(workOrderInfoView.getApplyCode());
            List<ApplyShipOrderItem> currentApplyShipOrderItemList = applyShipOrderItemList.stream().filter(p -> applyShipOrderIdList.contains(p.getApplyShipOrderId()) && p.getMaterialId().equals(material.getId())).collect(Collectors.toList());
            BigDecimal totalRequiredQuantity = currentApplyShipOrderItemList.stream().map(ApplyShipOrderItem::getRequiredNumber).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPickedQuantity = currentApplyShipOrderItemList.stream().map(p -> Optional.ofNullable(p.getPickedNumber()).orElse(BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);
            workOrderInfoView.setTotalRequiredQuantity(totalRequiredQuantity);
            workOrderInfoView.setTotalPickedQuantity(totalPickedQuantity);
        }
        PageData<ErpWorkOrderInfoViewResponse> pageData = new PageData<>();
        pageData.setData(erpWorkOrderInfoViewResponseList);
        pageData.setCount(total);
        return pageData;
    }

    @DataSource(DataSourceType.THIRD)
    @Override
    public void export(ErpWorkOrderInfoViewRequest request, HttpServletResponse httpServletResponse) throws Exception {
        PageData<ErpWorkOrderInfoViewResponse> data = getErpWorkOrderInfoViewPage(request);
        exportExcel(httpServletResponse, data.getData(), ErpWorkOrderInfoViewResponse.class, "");
    }

    //region 泛型导出

    /**
     * 指定数据源导出excel
     *
     * @param response
     * @param data     导出模板，1 导出错误信息，2 导出数据
     * @throws IOException
     */
    private <T> void exportExcel(HttpServletResponse response, List<T> data, Class cla, String fileName) throws Exception {
        if (StringUtils.isEmpty(fileName)) {
            fileName = cla.getSimpleName();
        }
        prepareResponds(fileName, response);
        ServletOutputStream outputStream = response.getOutputStream();
        // 获取改类声明的所有字段
        Field[] fields = cla.getDeclaredFields();
        // 响应字段对应的下拉集合
        Map<Integer, String[]> map = new HashMap<>();
        Field field = null;
        // 循环判断哪些字段有下拉数据集，并获取
        for (int i = 0; i < fields.length; i++) {
            field = fields[i];
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
//        if (flag == 0 || flag == 2) {
        builder.registerWriteHandler(new ExcelStyleConfig(Lists.newArrayList(20), null, null));
        builder.head(cla);
//        } else {
//            builder.registerWriteHandler(new ExcelStyleConfig(null,null,null));
//            builder.head(GXDetailListLogVO.class);
//        }
        String sheetName = cla.getSimpleName();
        WriteSheet sheet1 = EasyExcel.writerSheet(0, sheetName).build();
        builder.registerWriteHandler(new DropDownCellWriteHandler(map));
        builder.file(outputStream);

        //不能重命名，重命名就没有XLSX格式后缀
        builder.excelType(ExcelTypeEnum.XLSX);
        ExcelWriter writer = builder.build();
        writer.write(data, sheet1);
        writer.finish();

        //ExcelWriter实现Closeable 接口，内部close 调用finish, finish 内会执行关闭操作
//        outputStream.flush();
//        outputStream.close();
    }

    /**
     * 将文件输出到浏览器(导出)
     */
    private void prepareResponds(String fileName, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8'zh_cn'" + fileName + ExcelTypeEnum.XLSX.getValue());
    }
    //endregion
}




