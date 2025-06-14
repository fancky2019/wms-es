package gs.com.gses.model.bo.trunkorderexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruckOrderItemBo {
//    private Integer 序号;
//    private String 项目编号;
//    private String 项目名称;
//    private String 出库申请单号;
//    private String 物料编号;
//    private String 设备名称;
//    private String 设备编号;
//    private Integer 数量;
//    private String 发货批次;
//    private String 备注;

    @ExcelProperty(value = "工单号")
    private Integer seqNo;
    /**
     *
     */
    @ExcelProperty(value = "项目编号")
    private String projectNo;

    /**
     *
     */
    @ExcelProperty(value = "项目名称")
    private String projectName;

    /**
     *
     */
    @ExcelProperty(value = "出库申请单号")
    private String applyShipOrderCode;

    /**
     *
     */
    @ExcelProperty(value = "物料编号")
    private String materialCode;

    /**
     *
     */
    @ExcelProperty(value = "设备名称")
    private String deviceName;

    /**
     *
     */
    @ExcelProperty(value = "设备编号")
    private String deviceNo;


    /**
     *
     */
    @ExcelProperty(value = "数量")
    private BigDecimal quantity;

    /**
     *
     */
    @ExcelProperty(value = "发货批次")
    private String sendBatchNo;

    /**
     *
     */
    @ExcelProperty(value = "备注")
    private String remark;
}