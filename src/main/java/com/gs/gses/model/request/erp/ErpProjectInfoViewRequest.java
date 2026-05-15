package com.gs.gses.model.request.erp;

import com.gs.gses.model.request.RequestPage;
import lombok.Data;

@Data
public class ErpProjectInfoViewRequest extends RequestPage {
    private String projectCode;
    /**
     *
     */
    private String projectName;

    /**
     *
     */
    private String projectAddress;
}
