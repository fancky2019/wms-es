package gs.com.gses.filter;

import gs.com.gses.model.request.authority.LoginUserTokenDto;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserInfoHolder {

    /**
     * 使用ThreadLocal存储数据源
     */
    private static final ThreadLocal<LoginUserTokenDto> CONTEXT_HOLDER = new ThreadLocal<>();
    private static Map<String, LoginUserTokenDto> userInfoMap = new ConcurrentHashMap<>();

    /**
     *
     *
     * @param dbType
     */
    public static void setUser(LoginUserTokenDto dbType) {
        CONTEXT_HOLDER.set(dbType);
    }

    /**
     *
     *
     * @return
     */
    public static LoginUserTokenDto getUser() {
        return CONTEXT_HOLDER.get();
    }

    /**
     *
     */
    public static synchronized void removeUser() {
        CONTEXT_HOLDER.remove();
    }


    /**
     *
     *
     * @return
     */
    public static synchronized void setUser(String userId, LoginUserTokenDto dto) {
        userInfoMap.put(userId, dto);
    }

    /**
     *
     *
     * @return
     */
    public static LoginUserTokenDto getUser(String userId) {
        return userInfoMap.get(userId);
    }

    public static synchronized void removeUser(String userId) {
        userInfoMap.remove(userId);
    }

    public static synchronized void clearUser() {
        userInfoMap.clear();
    }

}
