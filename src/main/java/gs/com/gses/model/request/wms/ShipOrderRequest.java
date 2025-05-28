package gs.com.gses.model.request.wms;

import gs.com.gses.model.request.RequestPage;
import lombok.Data;

import java.util.List;

@Data
public class ShipOrderRequest extends RequestPage {
    private static final long serialVersionUID = 1L;
    private List<Long> shipOrderIdList;
}
