package gs.com.gses.model.response.mqtt;

import lombok.Data;

@Data
public class OrderMaterial {
    private  String materialCode;
    private  String batchNo;
    private  String quantity;
}
