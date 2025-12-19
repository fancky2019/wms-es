package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.WaveShipOrderItemRelation;
import gs.com.gses.service.WaveShipOrderItemRelationService;
import gs.com.gses.mapper.wms.WaveShipOrderItemRelationMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【WaveShipOrderItemRelation】的数据库操作Service实现
 * @createDate 2024-08-11 10:42:56
 */
@Service
public class WaveShipOrderItemRelationServiceImpl extends ServiceImpl<WaveShipOrderItemRelationMapper, WaveShipOrderItemRelation>
        implements WaveShipOrderItemRelationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindingNewRelation(HashMap<Long, Long> cloneRelation) throws Exception {
        List<Long> oldShipOrderItemIdList = cloneRelation.keySet().stream().collect(Collectors.toList());
        LambdaQueryWrapper<WaveShipOrderItemRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(WaveShipOrderItemRelation::getShipOrderItemId, cloneRelation.keySet());
        List<WaveShipOrderItemRelation> relationList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(relationList)) {
            throw new Exception("Get WaveShipOrderItemRelation fail");
        }

        LambdaUpdateWrapper<WaveShipOrderItemRelation> updateWrapper = null;
        for (WaveShipOrderItemRelation relation : relationList) {
            long newShipOrderItemId = cloneRelation.get(relation.getShipOrderItemId());
            updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(WaveShipOrderItemRelation::getId, relation.getId());
            updateWrapper.set(WaveShipOrderItemRelation::getShipOrderItemId, newShipOrderItemId);
            this.update(updateWrapper);
        }
    }


}




