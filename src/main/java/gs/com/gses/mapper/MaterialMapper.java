package gs.com.gses.mapper;

import gs.com.gses.model.entity.Material;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lirui
* @description 针对表【Material】的数据库操作Mapper
* @createDate 2024-08-11 10:16:00
* @Entity gs.com.gses.model.entity.Material
*/
@Mapper
public interface MaterialMapper extends BaseMapper<Material> {

}




