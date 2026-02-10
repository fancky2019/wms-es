package gs.com.gses.service.impl;

import gs.com.gses.model.request.authority.CheckPermissionRequest;
import gs.com.gses.model.request.authority.LoginUserTokenDto;
import gs.com.gses.model.response.wms.WmsResponse;
import gs.com.gses.service.AuthenticationService;
import gs.com.gses.service.api.AuthorityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private AuthorityService authorityService;

    @Override
    public LoginUserTokenDto resolveUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        CheckPermissionRequest checkPermissionRequest = new CheckPermissionRequest();
        checkPermissionRequest.setCode("ShipOrder/ComplexQueryDemo");
        checkPermissionRequest.setUrl("ShipOrder/ComplexQueryDemo");

        WmsResponse dto = authorityService.checkPermissionRet(checkPermissionRequest, token);
        if (!dto.getResult()) {
            String msg = "checkPermission fail";
            log.info(msg);
            return null;
        }

        Map<String, String> userInfoMap = (Map) dto.getData();
        LoginUserTokenDto user = new LoginUserTokenDto();
        BeanWrapper wrapper = new BeanWrapperImpl(user);
        wrapper.setPropertyValues(userInfoMap);
        return user;
    }
}
