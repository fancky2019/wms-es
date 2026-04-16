package com.gs.gses.service;

import com.gs.gses.model.request.authority.LoginRequest;
import com.gs.gses.model.request.authority.LoginUserTokenDto;

public interface WmsAuthorityService {
    LoginUserTokenDto wmsUserInfo(String token);

    LoginUserTokenDto login(LoginRequest request) throws Exception;
}
