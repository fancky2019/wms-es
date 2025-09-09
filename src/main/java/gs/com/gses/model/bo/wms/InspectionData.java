package gs.com.gses.model.bo.wms;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InspectionData {
    private Integer rowIndex;
    private String actualValue;
    private String standardValue;
    private String upperLimit;
    private String lowerLimit;
    private String inspectionResult;
}
