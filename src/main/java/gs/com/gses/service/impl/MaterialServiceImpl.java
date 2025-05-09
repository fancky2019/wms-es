package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.Material;
import gs.com.gses.service.MaterialService;
import gs.com.gses.mapper.MaterialMapper;
import org.springframework.stereotype.Service;

/**
* @author lirui
* @description 针对表【Material】的数据库操作Service实现
* @createDate 2024-08-11 10:16:00
*/
@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material>
    implements MaterialService{

}




