package gs.com.gses.service;

import gs.com.gses.model.request.authority.LoginUserTokenDto;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    LoginUserTokenDto resolveUser(HttpServletRequest request);
}
