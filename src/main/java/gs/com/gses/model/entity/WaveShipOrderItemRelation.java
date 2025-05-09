package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 
 * @TableName WaveShipOrderItemRelation
 */
@TableName(value ="WaveShipOrderItemRelation")
@Data
public class WaveShipOrderItemRelation implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 关联的出库单明细表id
     */
    @TableField(value = "ShipOrderItemId")
    private Long shipOrderItemId;

    /**
     * 关联的出库申请单明细表id
     */
    @TableField(value = "ApplyShipOrderItemId")
    private Long applyShipOrderItemId;

    /**
     * 波次需求数量
     */
    @TableField(value = "RequiredNumber")
    private BigDecimal requiredNumber;

    /**
     * 已拣货数量
     */
    @TableField(value = "PickedNumber")
    private BigDecimal pickedNumber;

    /**
     * 创建人ID
     */
    @TableField(value = "CreatorId")
    private Object creatorId;

    /**
     * 创建人名称
     */
    @TableField(value = "CreatorName")
    private String creatorName;

    /**
     * 最新修改人ID
     */
    @TableField(value = "LastModifierId")
    private Object lastModifierId;

    /**
     * 最新修改人名称
     */
    @TableField(value = "LastModifierName")
    private String lastModifierName;

    /**
     * 创建时间戳13位
     */
    @TableField(value = "CreationTime")
    private Long creationTime;

    /**
     * 修改时间戳13位
     */
    @TableField(value = "LastModificationTime")
    private Long lastModificationTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}