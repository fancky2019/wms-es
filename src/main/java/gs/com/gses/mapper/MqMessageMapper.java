package gs.com.gses.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gs.com.gses.model.entity.MqMessage;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lirui
* @description 针对表【MqMessage】的数据库操作Mapper
* @createDate 2024-08-12 14:23:55
* @Entity gs.com.gses.model.entity.MqMessage
*/
@Mapper
public interface MqMessageMapper extends BaseMapper<MqMessage> {

}




