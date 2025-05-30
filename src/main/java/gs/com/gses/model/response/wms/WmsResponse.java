package gs.com.gses.model.response.wms;

import lombok.Data;

import java.io.Serializable;

@Data
public class WmsResponse<T> implements Serializable {
    private T data;
    private Boolean result;
    private String explain;
    private String err;
    private String traceId;
    private static final long serialVersionUID = 1L;
}
