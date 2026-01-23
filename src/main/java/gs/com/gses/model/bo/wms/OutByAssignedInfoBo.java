package gs.com.gses.model.bo.wms;

import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 依赖：
 * spring-boot-starter-validation
 *
 * @Valid 或 @Validated @RequestBody OutByAssignedInfoBo requestList
 *
 * //判 字符串  null  ""  "   "
 * Assert.hasText(text, "text is empty");
 * //判  null
 *  Assert.notNull(detailRequest.getId(), "Detail id cannot be empty");
 *
 */
@Data
public class OutByAssignedInfoBo {
    @NotNull(message = "Ship order item ID can not be empty")
    private Long shipOrderItemId;

    @Valid  // 重要：这个注解会让Spring验证嵌套对象
    @NotNull(message = "InventoryItemDetail can not be empty")
    private List<InventoryItemDetailRequest> detailRequest;

    private String remark;
}
