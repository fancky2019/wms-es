package gs.com.gses.model.response.wms;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ShipOrderItemResponse implements Serializable {
    /**
     *
     */
    private Long id;

    /**
     * 关联的出库单主表id
     */
    private Long shipOrderId;
    /**
     * 第三方系统单号（兼容字符型）
     */
    private String shipOrderCode;

    private String applyShipOrderCode;
    /**
     * 行号
     */
    private Integer rowNo;

    /**
     * 第三方系统单号（兼容字符型）
     */
    private String erpCode;

    /**
     * 第三方系统行号（兼容字符型）
     */
    private String thirdPartyRowNo;

    /**
     * 状态（1open新建，2生效，3执行中，4已完成，-1作废）
     */
    private Integer XStatus;

    /**
     * 物料id
     */
    private Long materialId;
    /**
     * 物料id
     */
    private String materialCode;
    /**
     * 批号
     */
    private String batchNo;

    /**
     * 批号2
     */
    private String batchNo2;

    /**
     * 批号3
     */
    private String batchNo3;

    /**
     * 包装单位id
     */
    private Long packageUnitId;

    /**
     * 需求数量
     */
    private BigDecimal requiredPkgQuantity;

    /**
     * 需求数量单位
     */
    private String requiredUnit;

    /**
     * 分配数量
     */
    private BigDecimal alloactedPkgQuantity;

    private BigDecimal currentAllocatedPkgQuantity;
    /**
     * 已拣货数量
     */
    private BigDecimal pickedPkgQuantity;

    /**
     * 备注
     */
    private String comments;

    /**
     * 预留字段1
     */
    private String str1;

    /**
     * 预留字段2
     */
    private String str2;

    /**
     * 预留字段3
     */
    private String str3;

    /**
     * 预留字段4
     */
    private String str4;

    /**
     * 预留字段5
     */
    private String str5;

    /**
     * 预留字段6
     */
    private String str6;

    /**
     * 预留字段7
     */
    private String str7;

    /**
     * 预留字段8
     */
    private String str8;

    /**
     * 预留字段9
     */
    private String str9;

    /**
     * 预留字段10
     */
    private String str10;

    /**
     * 预留字段11
     */
    private String str11;

    /**
     * 预留字段12
     */
    private String str12;

    /**
     * 预留字段13
     */
    private String str13;

    /**
     * 预留字段14
     */
    private String str14;

    /**
     * 预留字段15
     */
    private String str15;

    /**
     * 预留字段16
     */
    private String str16;

    /**
     * 预留字段17
     */
    private String str17;

    /**
     * 预留字段18
     */
    private String str18;

    /**
     * 预留字段19
     */
    private String str19;

    /**
     * 预留字段20
     */
    private String str20;

    /**
     * 物料扩展属性预留字段1
     */
    private String m_Str1;

    /**
     * 物料扩展属性预留字段2
     */
    private String m_Str2;

    /**
     * 物料扩展属性预留字段3
     */
    private String m_Str3;

    /**
     * 物料扩展属性预留字段4
     */
    private String m_Str4;

    /**
     * 物料扩展属性预留字段5
     */
    private String m_Str5;

    /**
     * 物料扩展属性预留字段6
     */
    private String m_Str6;

    /**
     * 物料扩展属性预留字段7
     */
    private String m_Str7;

    /**
     * 物料扩展属性预留字段8
     */
    private String m_Str8;

    /**
     * 物料扩展属性预留字段9
     */
    private String m_Str9;

    /**
     * 物料扩展属性预留字段10
     */
    private String m_Str10;

    /**
     * 物料扩展属性预留字段11
     */
    private String m_Str11;

    /**
     * 物料扩展属性预留字段12
     */
    private String m_Str12;

    /**
     * 物料扩展属性预留字段13
     */
    private String m_Str13;

    /**
     * 物料扩展属性预留字段14
     */
    private String m_Str14;

    /**
     * 物料扩展属性预留字段15
     */
    private String m_Str15;

    /**
     * 物料扩展属性预留字段16
     */
    private String m_Str16;

    /**
     * 物料扩展属性预留字段17
     */
    private String m_Str17;

    /**
     * 物料扩展属性预留字段18
     */
    private String m_Str18;

    /**
     * 物料扩展属性预留字段19
     */
    private String m_Str19;

    /**
     * 物料扩展属性预留字段20
     */
    private String m_Str20;

    /**
     * 物料扩展属性预留字段21
     */
    private String m_Str21;

    /**
     * 物料扩展属性预留字段22
     */
    private String m_Str22;

    /**
     * 物料扩展属性预留字段23
     */
    private String m_Str23;

    /**
     * 物料扩展属性预留字段24
     */
    private String m_Str24;

    /**
     * 物料扩展属性预留字段25
     */
    private String m_Str25;

    /**
     * 物料扩展属性预留字段26
     */
    private String m_Str26;

    /**
     * 物料扩展属性预留字段27
     */
    private String m_Str27;

    /**
     * 物料扩展属性预留字段28
     */
    private String m_Str28;

    /**
     * 物料扩展属性预留字段29
     */
    private String m_Str29;

    /**
     * 物料扩展属性预留字段30
     */
    private String m_Str30;

    /**
     * 物料扩展属性预留字段31
     */
    private String m_Str31;

    /**
     * 物料扩展属性预留字段32
     */
    private String m_Str32;

    /**
     * 物料扩展属性预留字段33
     */
    private String m_Str33;

    /**
     * 物料扩展属性预留字段34
     */
    private String m_Str34;

    /**
     * 物料扩展属性预留字段35
     */
    private String m_Str35;

    /**
     * 物料扩展属性预留字段36
     */
    private String m_Str36;

    /**
     * 物料扩展属性预留字段37
     */
    private String m_Str37;

    /**
     * 物料扩展属性预留字段38
     */
    private String m_Str38;

    /**
     * 物料扩展属性预留字段39
     */
    private String m_Str39;

    /**
     * 物料扩展属性预留字段40
     */
    private String m_Str40;

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
     * 过期时间
     */
    private Long expiredTime;

    /**
     * 入库时间
     */
    private Long inboundTime;

    /**
     * 生产时间
     */
    private Long productTime;

    /**
     * 质检状态
     */
    private Integer QCStatus;

    /**
     * 箱号
     */
    private String carton;

    /**
     * 序列号
     */
    private String serialNo;

    /**
     * 单据优先级[0,8]
     */
    private Integer proirity;

    /**
     * 起点位置Code
     */
    private String fromLocationCode;

    /**
     * 起点位置ID
     */
    private Long fromLocationId;

    /**
     * 组织（客户，供应商）
     */
    private Long organiztionId;

    /**
     *
     */
    private String shipAccordingToOrderCode;

    /**
     * 终点位置Code
     */
    private String toLocCode;

    /**
     * 终点位置ID
     */
    private Long toLocId;

    /**
     * 包装方式
     */
    private String packageMethod;

    /**
     * 是否封存
     */
    private Boolean isSealed;

    /**
     * 库存是否足够
     */
    private Boolean sufficientInventory;

    /**
     * 缺少数量
     */
    private BigDecimal lackQuantity;

    /**
     * 起点位置Code
     */
    private String palletCode;

    private static final long serialVersionUID = 1L;
}

