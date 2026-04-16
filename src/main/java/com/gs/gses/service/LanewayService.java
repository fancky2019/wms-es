package com.gs.gses.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gses.model.entity.Laneway;
import org.springframework.web.bind.annotation.PathVariable;

/**
* @author lirui
* @description 针对表【Laneway】的数据库操作Service
* @createDate 2024-08-08 13:26:25
*/
public interface LanewayService extends IService<Laneway> {
    Laneway getById(@PathVariable Long id) throws InterruptedException;
}
