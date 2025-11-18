package gs.com.gses.service;

import gs.com.gses.model.entity.WmsTask;
import com.baomidou.mybatisplus.extension.service.IService;
import org.elasticsearch.client.license.LicensesStatus;

import java.util.List;

/**
* @author lirui
* @description 针对表【WmsTask】的数据库操作Service
* @createDate 2024-08-11 10:42:56
*/
public interface WmsTaskService extends IService<WmsTask> {

    void cancelTaskByIdList(List<Long> idList, String token);
}
