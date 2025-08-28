package gs.com.gses.model.bo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 多个字段分组统计 MultiKey addTruckOrderAndItem
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ModifyMStr12BoGroupKey {
    private String mStr7;
    private String materialCode;

    //默认只会生成无参构造器、getter、setter、toString、equals、hashCode 等。
    //ModifyMStr12BoGroupKey::new
    public ModifyMStr12BoGroupKey(ModifyMStr12Bo bo) {
        this.mStr7 = bo.getMStr7(); // 替换为实际字段名
        this.materialCode = bo.getMaterialCode();
    }
}
