package gs.com.gses.model.response.wms;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class ShipPickOrderResponse {
    /**
     *
     */
    private Long id;

    /**
     * 仓库id
     */
    private Long warehouseId;

    /**
     * 关联的出库单主表id
     */
    private Long shipOrderId;

    /**
     * 单据类型id
     */
    private Long billTypeId;

    /**
     * 状态 （1open新建，2部分分配，3整单分配，4执行中，5已完成，-1作废）
     */
    private Integer status;

    /**
     * 计划上架数量
     */
    private BigDecimal planPkgQuantity;

    /**
     * 分配数量
     */
    private BigDecimal allocatedPkgQuantity;

    /**
     * 已下架数量
     */
    private BigDecimal movedPkgQuantity;

    /**
     * 创建人ID
     */
    private Object creatorId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 最新修改人ID
     */
    private Object lastModifierId;

    /**
     * 最新修改人名称
     */
    private String lastModifierName;

    /**
     * 创建时间戳13位
     */
    private Long creationTime;

    /**
     * 修改时间戳13位
     */
    private Long lastModificationTime;

    /**
     * 拣货单号
     */
    private String shipPickOrderCode;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 生效时间
     */
    private LocalDateTime effectTime;

    private static final long serialVersionUID = 1L;
}
