package gs.com.gses.service;

import gs.com.gses.model.request.authority.LoginUserTokenDto;

public interface WmsAuthorityService {
    LoginUserTokenDto wmsUserInfo(String token);
}
