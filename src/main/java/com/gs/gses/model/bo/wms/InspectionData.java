package com.gs.gses.model.bo.wms;

import lombok.Data;

@Data
public class InspectionData {
    private Integer rowIndex;
    private String actualValue;
    private String standardValue;
    private String upperLimit;
    private String lowerLimit;
    private String inspectionResult;
}
