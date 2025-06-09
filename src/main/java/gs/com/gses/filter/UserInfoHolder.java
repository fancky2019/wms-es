package gs.com.gses.filter;

import gs.com.gses.model.request.authority.LoginUserTokenDto;

public class UserInfoHolder {

    /**
     * 使用ThreadLocal存储数据源
     */
    private static final ThreadLocal<LoginUserTokenDto> CONTEXT_HOLDER = new ThreadLocal<>();

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
    public static void removeUser() {
        CONTEXT_HOLDER.remove();
    }
}
