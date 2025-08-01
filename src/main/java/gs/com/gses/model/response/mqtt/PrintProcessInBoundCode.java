package gs.com.gses.model.response.mqtt;

import lombok.Data;

@Data
public class PrintProcessInBoundCode {
    private String msgId;
    private String str3;
    private String str4;
    private String receiptOrderCode;
    private String materialCode;

}
