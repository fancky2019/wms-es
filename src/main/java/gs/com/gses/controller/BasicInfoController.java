package gs.com.gses.controller;

import gs.com.gses.model.response.MessageResult;
import gs.com.gses.service.BasicInfoCacheService;
import gs.com.gses.service.InventoryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

//public class BasicInfoController {
@RestController
@RequestMapping("/api/basicInfo")
public class BasicInfoController {


    @Autowired
    private BasicInfoCacheService basicInfoCacheService;

    /**
     * 初始化缓存信息
     * @return
     * @throws Exception
     */
    @GetMapping("/initBasicInfoCache")
    public MessageResult<Void> initBasicInfoCache() throws Exception {
        basicInfoCacheService.initBasicInfoCache();
        return MessageResult.success();
    }

    /**
     * 初始化缓存信息
     * @return
     * @throws Exception
     */
    @GetMapping("/getBasicInfoCache")
    public MessageResult<Void> getBasicInfoCache() throws Exception {
        basicInfoCacheService.getBasicInfoCache();
        return MessageResult.success();
    }

    @PostMapping("/setSbpEnable")
    public MessageResult<Void> setSbpEnable() throws Exception {
        basicInfoCacheService.setSbpEnable();
        return MessageResult.success();
    }

    @PostMapping("/setKeyVal/{key}/{val}")
    public MessageResult<Void> setKeyVal(@PathVariable("key") String key,@PathVariable("val") String val) throws Exception {
        basicInfoCacheService.setKeyVal(key,val);
        return MessageResult.success();
    }

    @PostMapping("/setKeyValExpire/{key}/{val}/{timeout}")
    public MessageResult<Void> setKeyValExpire(@PathVariable("key") String key,@PathVariable("val") String val,@PathVariable("timeout") long timeout) throws Exception {
        basicInfoCacheService.setKeyValExpire(key,val,timeout, TimeUnit.SECONDS);
        return MessageResult.success();
    }

}
