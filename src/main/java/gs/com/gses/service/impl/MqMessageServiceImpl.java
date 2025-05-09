package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.service.MqMessageService;
import gs.com.gses.mapper.MqMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author lirui
* @description 针对表【MqMessage】的数据库操作Service实现
* @createDate 2024-08-12 14:23:55
*/
@Service
public class MqMessageServiceImpl extends ServiceImpl<MqMessageMapper, MqMessage>
    implements MqMessageService{

}




