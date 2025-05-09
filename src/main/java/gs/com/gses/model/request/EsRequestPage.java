package gs.com.gses.model.request;

import lombok.Data;

import java.util.List;

@Data
public class EsRequestPage extends Page {
    private List<String> sourceFieldList;
    /**
     *bucket  相当于group by
     */
    private List<String> bucketFieldList;
    /**
     * 统计字段
     */
    private List<String> aggregationFieldList;

    private List<Sort> sortFieldList;
}
