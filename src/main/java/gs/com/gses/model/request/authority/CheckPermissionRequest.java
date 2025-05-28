package gs.com.gses.model.request.authority;

import lombok.Data;

import java.security.PrivateKey;

@Data
public class CheckPermissionRequest {
    /**
     *
     */
    private String code;
    /**
     *
     */
    private String url;
}
