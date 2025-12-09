package gs.com.gses.service.api;

import gs.com.gses.model.request.authority.CheckPermissionRequest;
import gs.com.gses.model.request.authority.LoginRequest;
import gs.com.gses.model.request.authority.LoginUserTokenDto;
import gs.com.gses.model.response.wms.WmsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
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
