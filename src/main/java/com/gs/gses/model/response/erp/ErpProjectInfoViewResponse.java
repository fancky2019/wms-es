package com.gs.gses.model.response.erp;

import lombok.Data;

import java.io.Serializable;

@Data
public class ErpProjectInfoViewResponse implements Serializable {
    private String projectCode;
    /**
     *
     */
    private String projectName;

    /**
     *
     */
    private String projectAddress;

    private static final long serialVersionUID = 1L;
}
