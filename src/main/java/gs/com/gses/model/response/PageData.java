package gs.com.gses.model.response;

import lombok.Data;

import java.util.List;

@Data
public class PageData<T> {
//    private static final long serialVersionUID = 1L;
    private long count;
    private List<T> data;
}
