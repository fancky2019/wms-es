package com.gs.gses.service;

import com.gs.gses.model.entity.WmsTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author lirui
* @description 针对表【WmsTask】的数据库操作Service
* @createDate 2024-08-11 10:42:56
*/
public interface WmsTaskService extends IService<WmsTask> {

    void cancelTaskByIdList(List<Long> idList, String token);
}
