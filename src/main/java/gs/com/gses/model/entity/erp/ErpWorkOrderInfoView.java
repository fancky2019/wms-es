package gs.com.gses.model.entity.erp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 
 * @author lirui
 * @TableName ERP_WORKORDERINFO
 */
@TableName(value ="ERP_WORKORDERINFO")
@Data
public class ErpWorkOrderInfoView {
    /**
     * 
     */
    @TableField(value = "工单号")
    private String workOrderCode;
    /**
     *
     */
    @TableField(value = "料号")
    private String materialCode;

    /**
     * 
     */
    @TableField(value = "需求数量")
    private BigDecimal requiredQuantity;

    /**
     *
     */
    @TableField(value = "工单项次")
    private Integer rowNo;

    /**
     *
     */
    @TableField(value = "未齐套数量")
    private BigDecimal lackQuantity;

    /**
     *
     */
    @TableField(value = "是否为辅材")
    private String auxiliaryMaterial;

    /**
     * 
     */
    @TableField(value = "申请单")
    private String applyCode;
    /**
     *
     */
    @TableField(value = "批次")
    private String batchNo;
    /**
     * 
     */
    @TableField(value = "ERP申请数量")
    private BigDecimal erpApplyQuantity;

    /**
     * 
     */
    @TableField(value = "入库单")
    private String inBoundCode;

    /**
     * 
     */
    @TableField(value = "入库状态")
    private String inBoundStatus;

    /**
     * 
     */
    @TableField(value = "工单日期")
    private String workOrderDate;

    /**
     *
     */
    @TableField(value = "物料名称")
    private String materialName;
    /**
     *
     */
    @TableField(value = "项目编号")
    private String projectNo;
    /**
     *
     */
    @TableField(value = "项目名称")
    private String projectName;

}