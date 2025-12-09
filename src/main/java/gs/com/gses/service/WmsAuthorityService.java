package gs.com.gses.service;

import gs.com.gses.model.request.authority.LoginRequest;
import gs.com.gses.model.request.authority.LoginUserTokenDto;

public interface WmsAuthorityService {
    LoginUserTokenDto wmsUserInfo(String token);

    LoginUserTokenDto login(LoginRequest request) throws Exception;
}
