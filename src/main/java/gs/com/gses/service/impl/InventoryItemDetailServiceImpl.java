package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.*;
import gs.com.gses.mapper.InventoryItemDetailMapper;
import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import gs.com.gses.model.request.wms.ShipOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.InventoryItemDetailResponse;
import gs.com.gses.model.response.wms.ShipOrderItemResponse;
import gs.com.gses.service.InventoryItemDetailService;
import gs.com.gses.service.InventoryItemService;
import gs.com.gses.service.InventoryService;
import gs.com.gses.service.MaterialService;
import gs.com.gses.utility.LambdaFunctionHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【InventoryItemDetail】的数据库操作Service实现
 * @createDate 2024-08-08 13:44:55
 */
@Service
public class InventoryItemDetailServiceImpl extends ServiceImpl<InventoryItemDetailMapper, InventoryItemDetail>
        implements InventoryItemDetailService {


    @Autowired
    private MaterialService materialService;

    @Autowired
    private InventoryItemService inventoryItemService;

    @Autowired
    private InventoryService inventoryService;

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




