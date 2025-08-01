package gs.com.gses.model.response.wms;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReceiptOrderItemResponse {
    /**
     *
     */
    private Long id;

    /**
     * 关联的收货单主表id
     */
    private Long receiptOrderId;

    /**
     * 关联的申请单号(如果多个，用逗号分隔，拼接存储)
     */
    private String applyReceiptOrderCode;

    /**
     * 行号
     */
    private Integer rowNo;

    /**
     * 第三方系统行号
     */
    private String thirdPartyRowNo;

    /**
     * 状态(1-打开;2-部分分配;3-整单分配;4-执行中;5-已完成;-1-作废)
     */
    private Integer XStatus;

    /**
     * 物料id
     */
    private Long materialId;

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
     * 包装单位
     */
    private Long packageUnitId;

    /**
     * 期望收货数量
     */
    private BigDecimal expectedPkgQuantity;

    /**
     * 期望数量单位
     */
    private String expectedUnit;

    /**
     * 已收货数量
     */
    private BigDecimal receivedPkgQuantity;

    /**
     * 收货数量单位
     */
    private String receivedUnit;

    /**
     * 已上架数量（不可编辑，由任务报完成得到）
     */
    private BigDecimal movedPkgQuantity;

    /**
     * 质检状态值
     */
    private Integer QCStatus;

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
     * 仓库
     */
    private Long warehouseId;

    /**
     * 包装方式
     */
    private String packageMethod;

    /**
     * 是否保税
     */
    private Boolean isBonded;

    /**
     * 图片文件地址
     */
    private String imageFile;

    /**
     * 不合格数量
     */
    private BigDecimal unqualifiedQuantity;

    /**
     * 生产日期
     */
    private Long productTime;

    private static final long serialVersionUID = 1L;
}
