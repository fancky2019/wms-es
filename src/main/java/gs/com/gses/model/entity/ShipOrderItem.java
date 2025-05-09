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
 * @TableName ShipOrderItem
 */
@TableName(value ="ShipOrderItem")
@Data
public class ShipOrderItem implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 关联的出库单主表id
     */
    @TableField(value = "ShipOrderId")
    private Long shipOrderId;

    /**
     * 行号
     */
    @TableField(value = "RowNo")
    private Integer rowNo;

    /**
     * 第三方系统单号（兼容字符型）
     */
    @TableField(value = "ErpCode")
    private String erpCode;

    /**
     * 第三方系统行号（兼容字符型）
     */
    @TableField(value = "ThirdPartyRowNo")
    private String thirdPartyRowNo;

    /**
     * 状态（1open新建，2生效，3执行中，4已完成，-1作废）
     */
    @TableField(value = "XStatus")
    private Integer XStatus;

    /**
     * 物料id
     */
    @TableField(value = "MaterialId")
    private Long materialId;

    /**
     * 批号
     */
    @TableField(value = "BatchNo")
    private String batchNo;

    /**
     * 批号2
     */
    @TableField(value = "BatchNo2")
    private String batchNo2;

    /**
     * 批号3
     */
    @TableField(value = "BatchNo3")
    private String batchNo3;

    /**
     * 包装单位id
     */
    @TableField(value = "PackageUnitId")
    private Long packageUnitId;

    /**
     * 需求数量
     */
    @TableField(value = "RequiredPkgQuantity")
    private BigDecimal requiredPkgQuantity;

    /**
     * 需求数量单位
     */
    @TableField(value = "RequiredUnit")
    private String requiredUnit;

    /**
     * 分配数量
     */
    @TableField(value = "AlloactedPkgQuantity")
    private BigDecimal alloactedPkgQuantity;

    /**
     * 已拣货数量
     */
    @TableField(value = "PickedPkgQuantity")
    private BigDecimal pickedPkgQuantity;

    /**
     * 备注
     */
    @TableField(value = "Comments")
    private String comments;

    /**
     * 预留字段1
     */
    @TableField(value = "Str1")
    private String str1;

    /**
     * 预留字段2
     */
    @TableField(value = "Str2")
    private String str2;

    /**
     * 预留字段3
     */
    @TableField(value = "Str3")
    private String str3;

    /**
     * 预留字段4
     */
    @TableField(value = "Str4")
    private String str4;

    /**
     * 预留字段5
     */
    @TableField(value = "Str5")
    private String str5;

    /**
     * 预留字段6
     */
    @TableField(value = "Str6")
    private String str6;

    /**
     * 预留字段7
     */
    @TableField(value = "Str7")
    private String str7;

    /**
     * 预留字段8
     */
    @TableField(value = "Str8")
    private String str8;

    /**
     * 预留字段9
     */
    @TableField(value = "Str9")
    private String str9;

    /**
     * 预留字段10
     */
    @TableField(value = "Str10")
    private String str10;

    /**
     * 预留字段11
     */
    @TableField(value = "Str11")
    private String str11;

    /**
     * 预留字段12
     */
    @TableField(value = "Str12")
    private String str12;

    /**
     * 预留字段13
     */
    @TableField(value = "Str13")
    private String str13;

    /**
     * 预留字段14
     */
    @TableField(value = "Str14")
    private String str14;

    /**
     * 预留字段15
     */
    @TableField(value = "Str15")
    private String str15;

    /**
     * 预留字段16
     */
    @TableField(value = "Str16")
    private String str16;

    /**
     * 预留字段17
     */
    @TableField(value = "Str17")
    private String str17;

    /**
     * 预留字段18
     */
    @TableField(value = "Str18")
    private String str18;

    /**
     * 预留字段19
     */
    @TableField(value = "Str19")
    private String str19;

    /**
     * 预留字段20
     */
    @TableField(value = "Str20")
    private String str20;

    /**
     * 物料扩展属性预留字段1
     */
    @TableField(value = "M_Str1")
    private String m_Str1;

    /**
     * 物料扩展属性预留字段2
     */
    @TableField(value = "M_Str2")
    private String m_Str2;

    /**
     * 物料扩展属性预留字段3
     */
    @TableField(value = "M_Str3")
    private String m_Str3;

    /**
     * 物料扩展属性预留字段4
     */
    @TableField(value = "M_Str4")
    private String m_Str4;

    /**
     * 物料扩展属性预留字段5
     */
    @TableField(value = "M_Str5")
    private String m_Str5;

    /**
     * 物料扩展属性预留字段6
     */
    @TableField(value = "M_Str6")
    private String m_Str6;

    /**
     * 物料扩展属性预留字段7
     */
    @TableField(value = "M_Str7")
    private String m_Str7;

    /**
     * 物料扩展属性预留字段8
     */
    @TableField(value = "M_Str8")
    private String m_Str8;

    /**
     * 物料扩展属性预留字段9
     */
    @TableField(value = "M_Str9")
    private String m_Str9;

    /**
     * 物料扩展属性预留字段10
     */
    @TableField(value = "M_Str10")
    private String m_Str10;

    /**
     * 物料扩展属性预留字段11
     */
    @TableField(value = "M_Str11")
    private String m_Str11;

    /**
     * 物料扩展属性预留字段12
     */
    @TableField(value = "M_Str12")
    private String m_Str12;

    /**
     * 物料扩展属性预留字段13
     */
    @TableField(value = "M_Str13")
    private String m_Str13;

    /**
     * 物料扩展属性预留字段14
     */
    @TableField(value = "M_Str14")
    private String m_Str14;

    /**
     * 物料扩展属性预留字段15
     */
    @TableField(value = "M_Str15")
    private String m_Str15;

    /**
     * 物料扩展属性预留字段16
     */
    @TableField(value = "M_Str16")
    private String m_Str16;

    /**
     * 物料扩展属性预留字段17
     */
    @TableField(value = "M_Str17")
    private String m_Str17;

    /**
     * 物料扩展属性预留字段18
     */
    @TableField(value = "M_Str18")
    private String m_Str18;

    /**
     * 物料扩展属性预留字段19
     */
    @TableField(value = "M_Str19")
    private String m_Str19;

    /**
     * 物料扩展属性预留字段20
     */
    @TableField(value = "M_Str20")
    private String m_Str20;

    /**
     * 物料扩展属性预留字段21
     */
    @TableField(value = "M_Str21")
    private String m_Str21;

    /**
     * 物料扩展属性预留字段22
     */
    @TableField(value = "M_Str22")
    private String m_Str22;

    /**
     * 物料扩展属性预留字段23
     */
    @TableField(value = "M_Str23")
    private String m_Str23;

    /**
     * 物料扩展属性预留字段24
     */
    @TableField(value = "M_Str24")
    private String m_Str24;

    /**
     * 物料扩展属性预留字段25
     */
    @TableField(value = "M_Str25")
    private String m_Str25;

    /**
     * 物料扩展属性预留字段26
     */
    @TableField(value = "M_Str26")
    private String m_Str26;

    /**
     * 物料扩展属性预留字段27
     */
    @TableField(value = "M_Str27")
    private String m_Str27;

    /**
     * 物料扩展属性预留字段28
     */
    @TableField(value = "M_Str28")
    private String m_Str28;

    /**
     * 物料扩展属性预留字段29
     */
    @TableField(value = "M_Str29")
    private String m_Str29;

    /**
     * 物料扩展属性预留字段30
     */
    @TableField(value = "M_Str30")
    private String m_Str30;

    /**
     * 物料扩展属性预留字段31
     */
    @TableField(value = "M_Str31")
    private String m_Str31;

    /**
     * 物料扩展属性预留字段32
     */
    @TableField(value = "M_Str32")
    private String m_Str32;

    /**
     * 物料扩展属性预留字段33
     */
    @TableField(value = "M_Str33")
    private String m_Str33;

    /**
     * 物料扩展属性预留字段34
     */
    @TableField(value = "M_Str34")
    private String m_Str34;

    /**
     * 物料扩展属性预留字段35
     */
    @TableField(value = "M_Str35")
    private String m_Str35;

    /**
     * 物料扩展属性预留字段36
     */
    @TableField(value = "M_Str36")
    private String m_Str36;

    /**
     * 物料扩展属性预留字段37
     */
    @TableField(value = "M_Str37")
    private String m_Str37;

    /**
     * 物料扩展属性预留字段38
     */
    @TableField(value = "M_Str38")
    private String m_Str38;

    /**
     * 物料扩展属性预留字段39
     */
    @TableField(value = "M_Str39")
    private String m_Str39;

    /**
     * 物料扩展属性预留字段40
     */
    @TableField(value = "M_Str40")
    private String m_Str40;

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

    /**
     * 过期时间
     */
    @TableField(value = "ExpiredTime")
    private Long expiredTime;

    /**
     * 入库时间
     */
    @TableField(value = "InboundTime")
    private Long inboundTime;

    /**
     * 生产时间
     */
    @TableField(value = "ProductTime")
    private Long productTime;

    /**
     * 质检状态
     */
    @TableField(value = "QCStatus")
    private Integer QCStatus;

    /**
     * 箱号
     */
    @TableField(value = "Carton")
    private String carton;

    /**
     * 序列号
     */
    @TableField(value = "SerialNo")
    private String serialNo;

    /**
     * 单据优先级[0,8]
     */
    @TableField(value = "Proirity")
    private Integer proirity;

    /**
     * 起点位置Code
     */
    @TableField(value = "FromLocationCode")
    private String fromLocationCode;

    /**
     * 起点位置ID
     */
    @TableField(value = "FromLocationId")
    private Long fromLocationId;

    /**
     * 组织（客户，供应商）
     */
    @TableField(value = "OrganiztionId")
    private Long organiztionId;

    /**
     * 
     */
    @TableField(value = "ShipAccordingToOrderCode")
    private String shipAccordingToOrderCode;

    /**
     * 终点位置Code
     */
    @TableField(value = "ToLocCode")
    private String toLocCode;

    /**
     * 终点位置ID
     */
    @TableField(value = "ToLocId")
    private Long toLocId;

    /**
     * 包装方式
     */
    @TableField(value = "PackageMethod")
    private String packageMethod;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}