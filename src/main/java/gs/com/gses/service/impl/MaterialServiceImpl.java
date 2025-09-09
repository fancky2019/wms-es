package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.Material;
import gs.com.gses.model.entity.ReceiptOrderItem;
import gs.com.gses.model.entity.ShipOrder;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.MaterialRequest;
import gs.com.gses.model.request.wms.ReceiptOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.MaterialResponse;
import gs.com.gses.model.response.wms.ReceiptOrderItemResponse;
import gs.com.gses.service.MaterialService;
import gs.com.gses.mapper.MaterialMapper;
import gs.com.gses.utility.LambdaFunctionHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【Material】的数据库操作Service实现
 * @createDate 2024-08-11 10:16:00
 */
@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material>
        implements MaterialService {

    @Override
    public Material getByCode(String materialCode) throws Exception {
        if (StringUtils.isEmpty(materialCode)) {
            throw new Exception("materialCode is null ");
        }
        LambdaQueryWrapper<Material> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Material::getXCode, materialCode);
        List<Material> list = this.list(queryWrapper);
        if (list.size() > 1) {
            throw new Exception("find more than one material info  by " + materialCode);
        }
        if (list.isEmpty()) {
            throw new Exception("can't get material info  by " + materialCode);
        }
        return list.get(0);
    }

    @Override
    public PageData<MaterialResponse> getMaterialPage(MaterialRequest request) {
        LambdaQueryWrapper<Material> queryWrapper = new LambdaQueryWrapper<>();

        if (request.getId()!=null&&request.getId()>0){
            queryWrapper.eq(Material::getId, request.getId());
        }

        if (CollectionUtils.isNotEmpty(request.getMaterialCodeList())){
            queryWrapper.in(Material::getXCode, request.getMaterialCodeList());
        }

        if (StringUtils.isNotEmpty(request.getXCode())){
            queryWrapper.in(Material::getXCode, request.getXCode());
        }

        // 创建分页对象 (当前页, 每页大小)
        Page<Material> page = new Page<>(request.getPageIndex(), request.getPageSize());

        if (CollectionUtils.isEmpty(request.getSortFieldList())) {
            List<Sort> sortFieldList = new ArrayList<>();
            Sort sort = new Sort();
            sort.setSortField("id");
            sort.setSortType("desc");
            sortFieldList.add(sort);
            request.setSortFieldList(sortFieldList);
        }
        if (CollectionUtils.isNotEmpty(request.getSortFieldList())) {
            List<OrderItem> orderItems = LambdaFunctionHelper.getWithDynamicSort(request.getSortFieldList());
            page.setOrders(orderItems);
        }

        if (request.getSearchCount() != null) {
            // 关键设置：不执行 COUNT 查询
            page.setSearchCount(request.getSearchCount());
        }

        // 执行分页查询, sqlserver 使用通用表达式 WITH selectTemp AS
        IPage<Material> receiptOrderPage = this.baseMapper.selectPage(page, queryWrapper);

        // 获取当前页数据
        List<Material> records = receiptOrderPage.getRecords();
        long total = receiptOrderPage.getTotal();

        List<MaterialResponse> materialResponseList = records.stream().map(p -> {
            MaterialResponse response = new MaterialResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<MaterialResponse> pageData = new PageData<>();
        pageData.setData(materialResponseList);
        pageData.setCount(total);
        return pageData;
    }
}




