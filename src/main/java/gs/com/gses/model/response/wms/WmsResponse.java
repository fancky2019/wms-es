package gs.com.gses.model.response.wms;

import lombok.Data;

@Data
public class WmsResponse<T> {
    private T data;
    private Boolean result;
    private String explain;
    private String err;

}
