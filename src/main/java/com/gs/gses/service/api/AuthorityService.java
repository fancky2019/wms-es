package com.gs.gses.service.api;

import com.gs.gses.model.request.authority.CheckPermissionRequest;
import com.gs.gses.model.request.authority.LoginRequest;
import com.gs.gses.model.response.wms.WmsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "AuthorityService", url = "${sbp.authority}")
public interface AuthorityService {
//    @GetMapping("/GSUser/CheckPermission")
//    LoginUserTokenDto checkPermission(@SpringQueryMap CheckPermissionRequest request , @RequestHeader("Authorization") String token);


    @PostMapping("/GSUser/checkPermissionRet")
    WmsResponse checkPermissionRet(@RequestBody CheckPermissionRequest request , @RequestHeader("Authorization") String token);

    @PostMapping("/GSUser/Login")
    WmsResponse login(@RequestBody LoginRequest request);

}
