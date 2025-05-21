package gs.com.gses.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class Page implements Serializable {
    private Integer pageSize=10;
    private Integer pageIndex=1;
}
