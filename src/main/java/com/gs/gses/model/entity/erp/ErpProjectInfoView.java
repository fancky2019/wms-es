package com.gs.gses.model.entity.erp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName(value ="WMS_ERP_PJAB_VIEW")
@Data
public class ErpProjectInfoView {
    //select PJAB001,PJABL003,PJABUD010  from WMS_ERP_PJAB_VIEW
    /**
     *
     */
    @TableField(value = "PJAB001")
    private String projectCode;
    /**
     *
     */
    @TableField(value = "PJABL003")
    private String projectName;

    /**
     *
     */
    @TableField(value = "PJABUD010")
    private String projectAddress;

}
