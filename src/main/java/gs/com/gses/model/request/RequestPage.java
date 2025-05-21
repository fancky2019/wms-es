package gs.com.gses.model.request;

import lombok.Data;

@Data
public class RequestPage extends EsRequestPage {
    private Boolean searchCount=true;
}
