package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.Location;
import gs.com.gses.service.LocationService;
import gs.com.gses.mapper.LocationMapper;
import org.springframework.stereotype.Service;

/**
* @author lirui
* @description 针对表【Location】的数据库操作Service实现
* @createDate 2024-08-08 13:26:25
*/
@Service
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location>
    implements LocationService{

}




