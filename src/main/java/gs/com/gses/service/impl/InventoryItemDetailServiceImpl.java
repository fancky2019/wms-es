package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.InventoryItemDetail;
import gs.com.gses.mapper.InventoryItemDetailMapper;
import gs.com.gses.model.entity.ShipOrder;
import gs.com.gses.model.request.InventoryItemDetailRequest;
import gs.com.gses.model.request.ShipOrderRequest;
import gs.com.gses.model.response.ShipOrderResponse;
import gs.com.gses.service.InventoryItemDetailService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

    @Override
    public List<InventoryItemDetail> getInventoryItemDetailPage(InventoryItemDetailRequest request) {
        LambdaQueryWrapper<InventoryItemDetail> queryWrapper = new LambdaQueryWrapper<>();
        // 创建分页对象 (当前页, 每页大小)
        Page<InventoryItemDetail> page = new Page<>(request.getPageIndex(), request.getPageSize());
        // 关键设置：不执行 COUNT 查询
        page.setSearchCount(false);
        // 执行分页查询, sqlserver 使用通用表达式 WITH selectTemp AS
        IPage<InventoryItemDetail> shipOrderPage = this.baseMapper.selectPage(page, queryWrapper);

        // 获取结果   // 当前页数据
        List<InventoryItemDetail> records = shipOrderPage.getRecords();

//        List<InventoryItemDetail> records1=this.list();


//        // 测试单个字段查询,selectOne 内部调用list
//        Object entity = this.baseMapper.selectOne(new QueryWrapper<InventoryItemDetail>()
//                .select("M_Str1")
//                .eq("Id", 509955479831320L));
//
//// 使用简单查询测试
//        InventoryItemDetail entity11 = this.baseMapper.selectById(509955479831320L);

        return records;
    }


}




