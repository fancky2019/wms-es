package gs.com.gses.service;

import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.entity.BillType;


/**
 * @author lirui
 * @description 针对表【BillType】的数据库操作Service
 * @createDate 2025-07-24 14:01:16
 */
public interface BillTypeService extends IService<BillType> {
    BillType getByCode(String code) throws Exception;
}
