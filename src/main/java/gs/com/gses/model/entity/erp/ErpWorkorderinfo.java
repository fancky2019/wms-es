package gs.com.gses.model.entity.erp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 
 * @TableName ERP_WORKORDERINFO
 */
@TableName(value ="ERP_WORKORDERINFO")
@Data
public class ErpWorkorderinfo {
    /**
     * 
     */
    @TableField(value = "工单号")
    private String workOrderCode;

    /**
     * 
     */
    @TableField(value = "项序")
    private Integer rowNo;

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
    @TableField(value = "未齐套")
    private BigDecimal percent;

    /**
     * 
     */
    @TableField(value = "申请单")
    private String applyCode;

    /**
     * 
     */
    @TableField(value = "申请数量")
    private BigDecimal applyQuantity;

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
}