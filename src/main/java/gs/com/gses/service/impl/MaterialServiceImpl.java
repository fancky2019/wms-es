package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.Material;
import gs.com.gses.model.entity.ShipOrder;
import gs.com.gses.service.MaterialService;
import gs.com.gses.mapper.MaterialMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

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
            return null;
        }
        LambdaQueryWrapper<Material> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Material::getXCode, materialCode);
        List<Material> list = this.list(queryWrapper);
        if (list.size() > 1) {
            throw new Exception("find more than one material info  by " + materialCode);
        }
        return list.get(0);
    }
}




