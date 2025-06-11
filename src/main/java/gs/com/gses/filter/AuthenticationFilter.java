package gs.com.gses.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import gs.com.gses.model.request.authority.CheckPermissionRequest;
import gs.com.gses.model.request.authority.LoginUserTokenDto;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.wms.WmsResponse;
import gs.com.gses.service.api.AuthorityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Configuration
public class AuthenticationFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        //路径变量，根据 / 分割，获得数组，去除最有一个数组元素

        try {

            MessageResult<Void> messageResult = new MessageResult<>();
            messageResult.setSuccess(false);


            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            // 转换为HttpServletRequest
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            log.info("RequestURI:{}", httpServletRequest.getRequestURI());

            CheckPermissionRequest checkPermissionRequest = new CheckPermissionRequest();
            //ShipOrder/OneClickContinuous
            String code = "ShipOrder/ComplexQueryDemo";
            checkPermissionRequest.setCode(code);
            checkPermissionRequest.setUrl(code);
            //  LoginUserTokenDto dto = authorityService.checkPermission(request, token);

            String token = httpServletRequest.getHeader("Authorization");
            try {
                log.info("Start checkPermission");
                WmsResponse dto = authorityService.checkPermissionRet(checkPermissionRequest, token);
                LoginUserTokenDto userInfo = null;
                if (dto.getResult()) {
                    log.info("Start checkPermission success");
                    Map<String, String> userInfoMap = (Map) dto.getData();
                    userInfo = new LoginUserTokenDto();
                    BeanWrapper wrapper = new BeanWrapperImpl(userInfo);
                    wrapper.setPropertyValues(userInfoMap);
//           String jsonStr= objectMapper.writeValueAsString( dto.getData());
//            LoginUserTokenDto pojoJacksonPojo = objectMapper.readValue(jsonStr, LoginUserTokenDto.class);
                    UserInfoHolder.setUser(userInfo);
                    int m = 0;
                }

            } catch (FeignException ex) {
                log.info("CheckPermission fail");
                if (httpServletResponse.getStatus() == HttpStatus.UNAUTHORIZED.value()) {

                    // 401处理逻辑
                    //  return new UnauthorizedException("服务调用未授权，请检查认证信息");
                }
                messageResult.setMessage(ex.getMessage());
                messageResult.setCode(HttpServletResponse.SC_UNAUTHORIZED);
                if (ex instanceof FeignException.Unauthorized) {
                    // 401特殊处理
                    httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
                if (ex instanceof FeignException.Forbidden) {
                    // 401特殊处理
                    httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }

                String msg = objectMapper.writeValueAsString(messageResult);

                try {
                    returnJson(httpServletResponse, msg);
                } catch (Exception e) {
                    log.error("returnJson ", e);
                }
            } catch (Exception ex) {
                try {
                    messageResult.setMessage(ex.getMessage());
                    messageResult.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    String msg = objectMapper.writeValueAsString(messageResult);
                    returnJson(httpServletResponse, msg);
                } catch (Exception e) {
                    log.error("returnJson ", e);
                }
            }

            chain.doFilter(request, response);
            log.info("doFilter completed");
        } catch (Exception ex) {
            log.error("", ex);
        } finally {
            UserInfoHolder.removeUser();
        }
//        int statusCode = responseWrapper.getStatus();
//        System.out.println("Final status code: " + statusCode);
    }

    private void returnJson(HttpServletResponse response, String json) throws Exception {

//        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Cache-Control","no-cache");
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {


            writer = response.getWriter();
            writer.print(json);

        } catch (IOException e) {
            // logger.error("response error",e);
        } finally {
            if (writer != null) {
                writer.close();
            }

        }
    }
}
