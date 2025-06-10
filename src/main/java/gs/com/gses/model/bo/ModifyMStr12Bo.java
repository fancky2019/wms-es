package gs.com.gses.model.bo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = false)
// 禁用链式调用，否则easyexcel读取时候无法生成实体对象的值
@Accessors(chain = false)
@Data
public class ModifyMStr12Bo {

    @ExcelProperty(value = "工单号")
    private String orderNo;

    @ExcelProperty(value = "项目号")
    private String mStr7;

    @ExcelIgnore
    private Long materialId;

    @ExcelProperty("物料代码")
    private String materialCode;

    @ExcelProperty("设备号")
    private String mStr12;

    @ExcelProperty("数量")
    private BigDecimal quantity;

    @ExcelProperty("备注")
    private String remark;

}
