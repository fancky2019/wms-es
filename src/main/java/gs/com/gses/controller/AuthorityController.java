package gs.com.gses.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.request.authority.LoginUserTokenDto;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.service.WmsAuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/authority")
public class AuthorityController {

//    @Autowired
//    private WmsService wmsService;

    @Autowired
    private WmsAuthorityService wmsAuthorityService;

    @Autowired
    private ObjectMapper objectMapper;



    /**
     *测试权限校验@ignore
     * @param id
     * @param token
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/checkPermissionRet/{id}")
    public MessageResult<LoginUserTokenDto> checkPermissionRet(@PathVariable Long id, @RequestHeader("Authorization") String token) throws InterruptedException {
        return MessageResult.success(wmsAuthorityService.wmsUserInfo(token));
    }
}
