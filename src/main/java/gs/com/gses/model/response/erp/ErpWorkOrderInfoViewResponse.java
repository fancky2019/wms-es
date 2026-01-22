package gs.com.gses.model.response.erp;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

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
    @ExcelProperty(value = "WMS需求数量")
    private BigDecimal totalRequiredQuantity;
    @ExcelProperty(value = "WMS拣货数量")
    private BigDecimal totalPickedQuantity;

    private static final long serialVersionUID = 1L;
}
