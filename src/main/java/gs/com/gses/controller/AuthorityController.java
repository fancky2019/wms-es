package gs.com.gses.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.request.authority.CheckPermissionRequest;
import gs.com.gses.model.request.authority.LoginUserTokenDto;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.wms.WmsResponse;
import gs.com.gses.service.api.AuthorityService;
import gs.com.gses.service.api.WmsService;
import gs.com.gses.service.impl.WmsAuthorityService;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/authority")
public class AuthorityController {

//    @Autowired
//    private WmsService wmsService;

    @Autowired
    private WmsAuthorityService wmsAuthorityService;

    @Autowired
    private ObjectMapper objectMapper;


    @GetMapping("/checkPermissionRet/{id}")
    public MessageResult<LoginUserTokenDto> checkPermissionRet(@PathVariable Long id, @RequestHeader("Authorization") String token) throws InterruptedException {
        return MessageResult.success(wmsAuthorityService.wmsUserInfo(token));
    }
}
