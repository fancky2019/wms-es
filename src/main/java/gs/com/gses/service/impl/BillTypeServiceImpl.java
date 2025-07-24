package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.mapper.BillTypeMapper;
import gs.com.gses.model.entity.BillType;
import gs.com.gses.service.BillTypeService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lirui
 * @description 针对表【BillType】的数据库操作Service实现
 * @createDate 2025-07-24 14:01:16
 */
@Service
public class BillTypeServiceImpl extends ServiceImpl<BillTypeMapper, BillType>
        implements BillTypeService {

    @Override
    public BillType getByCode(String code) throws Exception {
        if (StringUtils.isEmpty(code)) {
            throw new Exception("Code is empty ");
        }
        LambdaQueryWrapper<BillType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BillType::getXCode, code);
        List<BillType> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new Exception("Can't not get BillType by code " + code);
        }
        if (list.size() > 1) {
            throw new Exception("Get multiple\n BillType by code " + code);
        }
        return list.get(0);
    }
}




