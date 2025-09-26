package gs.com.gses.model.request.wms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import gs.com.gses.model.request.RequestPage;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShipPickOrderItemRequest extends RequestPage {
    /**
     *
     */
    private Long id;

    /**
     * 拣货主表单id
     */
    private Long shipPickOrderId;

    /**
     * 发货单明细表id（标识这个拣货明细对应的发货单明细行项）
     */
    private Long shipOrderItemId;

    /**
     * 计划拣货数量
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
     * 起点位置ID
     */
    private Long fromLocId;

    /**
     * 起点位置编号
     */
    private String fromLocCode;

    /**
     * 终点位置ID
     */
    private Long toLocId;

    /**
     * 终点位置编号
     */
    private String toLocCode;

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

    private static final long serialVersionUID = 1L;
}
