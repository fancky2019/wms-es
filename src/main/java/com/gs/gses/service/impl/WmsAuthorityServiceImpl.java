package com.gs.gses.service.impl;

import com.gs.gses.filter.UserInfoHolder;
import com.gs.gses.model.request.authority.CheckPermissionRequest;
import com.gs.gses.model.request.authority.LoginRequest;
import com.gs.gses.model.request.authority.LoginUserTokenDto;
import com.gs.gses.model.response.wms.WmsResponse;
import com.gs.gses.service.WmsAuthorityService;
import com.gs.gses.service.api.AuthorityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class WmsAuthorityServiceImpl implements WmsAuthorityService {

    @Autowired
    private AuthorityService authorityService;


    @Override
    public LoginUserTokenDto wmsUserInfo(String token) {
        CheckPermissionRequest request = new CheckPermissionRequest();
        //ShipOrder/OneClickContinuous
        String code = "ShipOrder/OneClickContinuous";
        request.setCode(code);
        request.setUrl(code);
        //  LoginUserTokenDto dto = authorityService.checkPermission(request, token);

        WmsResponse dto = authorityService.checkPermissionRet(request, token);
        LoginUserTokenDto userInfo = null;
        if (dto.getResult()) {
            Map<String, String> userInfoMap = (Map) dto.getData();
            userInfo = new LoginUserTokenDto();
            BeanWrapper wrapper = new BeanWrapperImpl(userInfo);
            wrapper.setPropertyValues(userInfoMap);
//           String jsonStr= objectMapper.writeValueAsString( dto.getData());
//            LoginUserTokenDto pojoJacksonPojo = objectMapper.readValue(jsonStr, LoginUserTokenDto.class);

            int m = 0;
        }
        return userInfo;
    }

    @Override
    public LoginUserTokenDto login(LoginRequest request) throws Exception {
        WmsResponse dto = authorityService.login(request);
        LoginUserTokenDto userInfo = null;
        if (dto.getResult()) {
            Map<String, String> userInfoMap = (Map) dto.getData();
            userInfo = new LoginUserTokenDto();
            // 不存在的字段会报错
//            BeanWrapper wrapper = new BeanWrapperImpl(userInfo);
//            wrapper.setPropertyValues(userInfoMap);
            //会自动忽略不存在的字段。
            BeanUtils.copyProperties(userInfo, userInfoMap);
//           String jsonStr= objectMapper.writeValueAsString( dto.getData());
//            LoginUserTokenDto pojoJacksonPojo = objectMapper.readValue(jsonStr, LoginUserTokenDto.class);
            UserInfoHolder.setUser(userInfo.getId(), userInfo);
            int m = 0;
        }
        return userInfo;
    }
}
