package gs.com.gses.service.impl;

import gs.com.gses.model.request.authority.LoginUserTokenDto;

public interface WmsAuthorityService {
    LoginUserTokenDto wmsUserInfo(String token);
}
