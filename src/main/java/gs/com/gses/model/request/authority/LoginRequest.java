package gs.com.gses.model.request.authority;

import lombok.Data;

@Data
public class LoginRequest {

    //00128   0012800  cf00d984be00cf8e77fb559ae44c5aa5
    //   "accountName": "admin",  // "password":"228cd269b5f3ca6d07440f278ae27836"
    private String accountName;
    private String password;
}
