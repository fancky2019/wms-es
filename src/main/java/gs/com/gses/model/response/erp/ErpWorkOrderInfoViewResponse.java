package gs.com.gses.model.response.erp;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import gs.com.gses.model.enums.TruckOrderStausEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ErpWorkOrderInfoViewResponse implements Serializable {
    /**
     *
     */
    @ExcelProperty(value = "工单号")
    private String workOrderCode;
    /**
     *
     */
    @ExcelProperty(value = "料号")
    private String materialCode;

    /**
     *
     */
    @ExcelProperty(value = "需求数量")
    private BigDecimal requiredQuantity;

    /**
     *
     */
    @ExcelProperty(value = "工单项次")
    private Integer rowNo;


    /**
     *
     */
    @ExcelProperty(value = "未齐套数量")
    private BigDecimal lackQuantity;

    /**
     *
     */
    @ExcelProperty(value = "是否为辅材")
    private String auxiliaryMaterial;
    /**
     *
     */
    @ExcelProperty(value = "申请单")
    private String applyCode;
    /**
     *
     */
    @ExcelProperty(value = "批次")
    private String batchNo;
    /**
     *
     */
    @ExcelProperty(value = "保单号")
    private BigDecimal erpApplyQuantity;

    /**
     *
     */
    @ExcelProperty(value = "ERP申请数量")
    private String inBoundCode;

    /**
     *
     */
    @ExcelProperty(value = "入库单")
    private String inBoundStatus;

    /**
     *
     */
    @ExcelProperty(value = "工单日期")
    private String workOrderDate;

    /**
     *
     */
    @ExcelProperty(value = "物料名称")
    private String materialName;
    @ExcelProperty(value = "项目编号")
    private String projectNo;
    /**
     *
     */
    @ExcelProperty(value = "项目名称")
    private String projectName;
    @ExcelProperty(value = "WMS需求数量")
    private BigDecimal totalRequiredQuantity;
    @ExcelProperty(value = "WMS拣货数量")
    private BigDecimal totalPickedQuantity;
    @ExcelProperty(value = "是否齐套")
//    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Boolean completeSet;
    //    @JsonGetter("statusStr")  // 专门用于 Jackson 序列化的注解
    public Boolean getCompleteSet() {
        return totalRequiredQuantity != null && totalPickedQuantity != null && totalRequiredQuantity.compareTo(totalPickedQuantity) == 0;
    }

    private static final long serialVersionUID = 1L;
}
