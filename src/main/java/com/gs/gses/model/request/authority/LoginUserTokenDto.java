package com.gs.gses.model.request.authority;

import lombok.Data;

@Data
public class LoginUserTokenDto {

    private String id ;

    /// <summary>
    /// 用户名
    /// </summary>
    private String userName ;

    /// <summary>
    /// 登录账号
    /// </summary>
    private String accountName ;

    /// <summary>
    /// 访问令牌
    /// </summary>
    private String accessToken ;

    /// <summary>
    /// 过期时间
    /// </summary>

    private String expirationTime;

    /// <summary>
    /// 是否启用
    /// </summary>
    private Boolean isEnable;
    /// <summary>
    /// 部门
    /// </summary>
    private String department;

//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss[.SSSSSSSSS]")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String lastModificationTime;

    /// <inheritdoc />
    private String lastModifierId ;
    /// <inheritdoc />
    private String creationTime;

    /// <inheritdoc />
    private String creatorId;
}
