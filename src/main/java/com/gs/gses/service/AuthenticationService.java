package com.gs.gses.service;

import com.gs.gses.model.request.authority.LoginUserTokenDto;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    LoginUserTokenDto resolveUser(HttpServletRequest request);
}
