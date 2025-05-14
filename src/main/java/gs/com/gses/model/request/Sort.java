package gs.com.gses.model.request;

import lombok.Data;

/**
 * @author lirui
 */
@Data
public class Sort {
    private  String sortField;
    /**
     * asc desc
     */
    private  String sortType;
}
