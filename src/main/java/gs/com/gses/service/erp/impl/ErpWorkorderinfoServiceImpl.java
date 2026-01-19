package gs.com.gses.service.erp.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.erp.ErpWorkorderinfo;
import gs.com.gses.mapper.erp.ErpWorkorderinfoMapper;
import gs.com.gses.multipledatasource.DataSource;
import gs.com.gses.multipledatasource.DataSourceType;
import gs.com.gses.service.erp.ErpWorkorderinfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lirui
 * @description 针对表【ERP_WORKORDERINFO】的数据库操作Service实现
 * @createDate 2026-01-19 14:31:07
 */
@Service
public class ErpWorkorderinfoServiceImpl extends ServiceImpl<ErpWorkorderinfoMapper, ErpWorkorderinfo>
        implements ErpWorkorderinfoService {


    @DataSource(DataSourceType.THIRD)
    @Override
    public void oracleQuery() {
        String workOrderCode = "GS-AS03-2401240103";
        LambdaQueryWrapper<ErpWorkorderinfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ErpWorkorderinfo::getWorkOrderCode, workOrderCode);
        List<ErpWorkorderinfo> workOrderCodeDataList = this.list(queryWrapper);
        int n = 0;
    }
}




