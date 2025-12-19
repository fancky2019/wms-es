package gs.com.gses.mapper.wms;

import gs.com.gses.model.entity.InventoryItemDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author lirui
 * @description 针对表【InventoryItemDetail】的数据库操作Mapper
 * @createDate 2024-08-08 13:44:55
 * @Entity gs.com.gses.model.entity.InventoryItemDetail
 */
@Mapper
public interface InventoryItemDetailMapper extends BaseMapper<InventoryItemDetail> {
    List<Long> getAllIdList();
}




