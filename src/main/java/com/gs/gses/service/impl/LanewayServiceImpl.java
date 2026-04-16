package com.gs.gses.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gs.gses.model.entity.Laneway;
import com.gs.gses.service.BasicInfoCacheService;
import com.gs.gses.service.LanewayService;
import com.gs.gses.mapper.wms.LanewayMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author lirui
* @description 针对表【Laneway】的数据库操作Service实现
* @createDate 2024-08-08 13:26:25
*/
@Service
public class LanewayServiceImpl extends ServiceImpl<LanewayMapper, Laneway>
    implements LanewayService{
    @Autowired
    private BasicInfoCacheService basicInfoCacheService;
    @Override
    public Laneway getById(Long id) throws InterruptedException {
        return basicInfoCacheService.loadFromDbLaneway(id);
    }
}




