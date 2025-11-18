package gs.com.gses.controller;

import gs.com.gses.model.response.MessageResult;
import gs.com.gses.service.WmsTaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "WmsTask", description = "WmsTask")
@RequestMapping("/api/wmsTask")
public class WmsTaskController {

    @Autowired
    private WmsTaskService wmsTaskService;

    /**
     *  js可能造成精度丢失，后台最好设计成string
     *
     *  参数数组：[ 739728636198982,   739728618586192]
     * @param idList
     * @param token
     * @return
     */
    @PostMapping("/cancelTaskByIdList")
    public MessageResult<Void> cancelTaskByIdList(@RequestBody List<Long> idList, @RequestHeader("Authorization") String token) {
        wmsTaskService.cancelTaskByIdList(idList, token);
        return MessageResult.success();
    }

}
