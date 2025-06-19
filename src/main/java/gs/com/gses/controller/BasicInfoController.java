package gs.com.gses.controller;

import gs.com.gses.model.response.MessageResult;
import gs.com.gses.service.BasicInfoCacheService;
import gs.com.gses.service.InventoryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//public class BasicInfoController {
@RestController
@RequestMapping("/basicInfo")
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


}
