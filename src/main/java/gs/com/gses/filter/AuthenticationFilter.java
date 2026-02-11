package gs.com.gses.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import gs.com.gses.config.CorsProperties;
import gs.com.gses.model.request.authority.CheckPermissionRequest;
import gs.com.gses.model.request.authority.LoginUserTokenDto;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.wms.WmsResponse;
import gs.com.gses.service.api.AuthorityService;
import gs.com.gses.sse.SseEmitterServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.exception.ExtIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;


@Slf4j
//过配置文件中的开关控制 AuthenticationFilter 是否生效（即是否被注册为 Bean 并参与过滤器链）。
@ConditionalOnProperty(value = "sbp.checkpermission", havingValue = "true")
@Configuration
public class AuthenticationFilter implements Filter {
    @Autowired
    private CorsProperties corsProperties;
    //    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //路径变量，根据 / 分割，获得数组，去除最有一个数组元素
        MessageResult<Void> messageResult = new MessageResult<>();
        messageResult.setSuccess(false);

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // 转换为HttpServletRequest
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String requestURI = httpServletRequest.getRequestURI();
        // 获取客户端IP地址
        String clientIp = getClientIpAddress(httpServletRequest);
        log.info("Request from IP: {}, URI: {}", clientIp, requestURI);

        String method = httpServletRequest.getMethod();
        // 放行 OPTIONS 请求（由 CorsFilter 处理）
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }
        // 放行 Swagger 相关路径,不校验
        if (requestURI.contains("/swagger-ui") ||
                requestURI.contains("/v3/api-docs") ||
                requestURI.contains("/swagger-resources")) {
//            不调用 chain.doFilter() → 过滤器链断开 → 请求不会进入 Controller。
            chain.doFilter(request, response);
            return;
        }

//        if (requestURI.contains("/sseConnect")) {
//            chain.doFilter(request, response);
//            return;
//        }

        //OPTIONS 请求不会到此过滤器，应该到cors 中

        CheckPermissionRequest checkPermissionRequest = new CheckPermissionRequest();
        //ShipOrder/OneClickContinuous
        String code = "ShipOrder/ComplexQueryDemo";
//            String code = "ShipOrder/SubAssignPalletsByShipOrderBatch";
        checkPermissionRequest.setCode(code);
        checkPermissionRequest.setUrl(code);
        //  LoginUserTokenDto dto = authorityService.checkPermission(request, token);

        String token = httpServletRequest.getHeader("Authorization");
        log.info("token {}", token);
        LoginUserTokenDto userInfo = null;
        try {
            log.info("Start checkPermission");
            WmsResponse dto = authorityService.checkPermissionRet(checkPermissionRequest, token);
            log.info("WmsResponse {}", dto);

            if (dto.getResult()) {
                log.info("Complete checkPermission success");
                Map<String, String> userInfoMap = (Map) dto.getData();
                userInfo = new LoginUserTokenDto();
                BeanWrapper wrapper = new BeanWrapperImpl(userInfo);
                wrapper.setPropertyValues(userInfoMap);
//           String jsonStr= objectMapper.writeValueAsString( dto.getData());
//            LoginUserTokenDto pojoJacksonPojo = objectMapper.readValue(jsonStr, LoginUserTokenDto.class);
                UserInfoHolder.setUser(userInfo);
                UserInfoHolder.setUser(userInfo.getId(), userInfo);
                log.info("UserInfoHolder set complete {}", dto);

                if (requestURI.contains("/sseConnect") && SseEmitterServiceImpl.sseCache.containsKey(userInfo.getId())) {
                    String msg = MessageFormat.format("userInfo {0} has been connected", userInfo.getId());
                    log.info(msg);
                    authenticationFail(httpServletRequest, httpServletResponse, messageResult, msg);
                    return;
                }
            } else {
                String msg = "checkPermission fail";
                log.info(msg);
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                authenticationFail(httpServletRequest, httpServletResponse, messageResult, msg);
//               请求被拦截 不再往下走过滤器链 不会进入 Controller 直接由 Filter 自己返回响应
                return;
            }

            log.info("Authentication complete");
            chain.doFilter(request, response);
            log.info("doFilter completed");

        } catch (FeignException ex) {
            log.error("CheckPermission fail", ex);
            if (ex instanceof FeignException.Unauthorized) {
                // 401特殊处理
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            if (ex instanceof FeignException.Forbidden) {
                // 401特殊处理
                httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
            try {
                authenticationFail(httpServletRequest, httpServletResponse, messageResult, ex.getMessage());
                //自己返回(returnJson)加return
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception ex) {
            try {
                messageResult.setMessage(ex.getMessage());
                messageResult.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                String msg = objectMapper.writeValueAsString(messageResult);
                returnJson(httpServletRequest, httpServletResponse, msg);
                return;
            } catch (Exception e) {
                log.error("returnJson ", e);
            }
        } finally {
            UserInfoHolder.removeUser();
            if (userInfo != null) {
                UserInfoHolder.removeUser(userInfo.getId());
            }
        }
    }

    private void authenticationFail(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, MessageResult<Void> messageResult, String msg) throws Exception {
        messageResult.setMessage(msg);
        messageResult.setCode(httpServletResponse.getStatus());
        String json = objectMapper.writeValueAsString(messageResult);
        returnJson(httpServletRequest, httpServletResponse, json);
    }

    private void returnJson(HttpServletRequest httpServletRequest, HttpServletResponse response, String json) {

        response.setCharacterEncoding("UTF-8");
        String requestURI = httpServletRequest.getRequestURI();
        if (requestURI.contains("/sseConnect")) {
//            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
//            // SSE需要返回200
//            response.setStatus(HttpServletResponse.SC_OK);
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            sendSseError(response, HttpServletResponse.SC_UNAUTHORIZED, json);
            return;
        } else {
            response.setContentType("application/json; charset=utf-8");
        }

        String origin = httpServletRequest.getHeader("Origin");
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        }

        try (PrintWriter writer = response.getWriter()) {
            writer.print(json);
            writer.flush();
        } catch (IOException e) {
            log.error("returnJson error", e);
        }

    }

    //region ip

    /**
     * 获取客户端真实IP地址
     * 考虑了代理、负载均衡等情况
     */
    public String getClientIpAddress(HttpServletRequest request) {
        String ip = null;

        // 1. 尝试从X-Forwarded-For获取
        ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // 如果有多个IP，取第一个（客户端真实IP）
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }

        // 2. 尝试从Proxy-Client-IP获取
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 3. 尝试从WL-Proxy-Client-IP获取
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 4. 尝试从HTTP_CLIENT_IP获取
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 5. 尝试从HTTP_X_FORWARDED_FOR获取
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }

        // 6. 最后使用request.getRemoteAddr()
        ip = request.getRemoteAddr();

        // 处理IPv6本地地址
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }

    /**
     * 验证IP地址是否有效
     */
    private boolean isValidIp(String ip) {
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }
        return true;
    }


    //endregion


    /**
     * 发送SSE格式的错误响应
     *
     * 401 + application/json
     * 200 + text/event-stream
     * 发送SSE格式的错误响应
     *
     * @param response HttpServletResponse
     * @param httpStatusCode HTTP状态码（401/403等）
     * @param errorMessage 错误消息
     */
    private void sendSseError(HttpServletResponse response, int httpStatusCode, String errorMessage) {
        try {
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK); // SSE必须返回200

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("httpStatusCode", httpStatusCode); // 原始HTTP状态码
            errorData.put("code", httpStatusCode); // 兼容旧格式
            errorData.put("message", errorMessage);
            errorData.put("type", "auth_error");
            errorData.put("timestamp", System.currentTimeMillis());
            errorData.put("success", false);

            PrintWriter writer = response.getWriter();
            writer.write("event: auth_error\n"); // 明确的事件名称
            writer.write("data: " + objectMapper.writeValueAsString(errorData) + "\n");
            writer.write("retry: 0\n\n"); // 设置为0表示不要自动重连
            writer.flush();
        } catch (IOException e) {
            log.error("发送SSE错误响应失败", e);
        }
    }
}
