package com.gs.gses.controller;

import com.gs.gses.model.entity.Laneway;
import com.gs.gses.service.BasicInfoCacheService;
import com.gs.gses.service.LanewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/laneway")
public class LanewayController {

    @Autowired
    private LanewayService lanewayService;

    @GetMapping("/getById/{id}")
    public Laneway getById(@PathVariable Long id) throws InterruptedException {
        return lanewayService.getById(id);
    }
}
