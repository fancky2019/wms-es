package gs.com.gses.model.request.erp;

import gs.com.gses.model.request.RequestPage;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ErpWorkOrderInfoViewRequest extends RequestPage {
    /**
     *
     */
    @NotNull(message = "workOrderCode cannot be empty")
    private String workOrderCode;
    /**
     *
     */
    private String materialCode;

    /**
     *
     */
    private BigDecimal requiredQuantity;

    /**
     *
     */
    private Integer rowNo;



    /**
     *
     */
    private BigDecimal lackQuantity;

    /**
     *
     */
    private String auxiliaryMaterial;
    /**
     *
     */
    private String applyCode;
    /**
     *
     */
    private String batchNo;
    /**
     *
     */
    private BigDecimal erpApplyQuantity;

    /**
     *
     */
    private String inBoundCode;

    /**
     *
     */
    private String inBoundStatus;

    /**
     *
     */
    private String workOrderDate;

    /**
     *
     */
    private String materialName;


}
