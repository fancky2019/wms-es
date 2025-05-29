package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName ShipOrder
 */
@TableName(value ="ShipOrder")
@Data
public class ShipOrder implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 仓库id
     */
    @TableField(value = "Whid")
    private Long whid;

    /**
     * 出库单号
     */
    @TableField(value = "XCode")
    private String XCode;

    /**
     * 关联的出库申请单号(如果多个申请单合并出库，逗号分隔，拼接存储)
     */
    @TableField(value = "ApplyShipOrderCode")
    private String applyShipOrderCode;

    /**
     * 状态（1 open新建，2生效，3执行中，4已完成，5已发运,-1作废）
     */
    @TableField(value = "XStatus")
    private Integer XStatus;

    /**
     * 单据类型id
     */
    @TableField(value = "BillTypeId")
    private Long billTypeId;

    /**
     * 期望出库数量（不可编辑，由明细加总得到）
     */
    @TableField(value = "ExpectedPkgQuantity")
    private BigDecimal expectedPkgQuantity;

    /**
     * 已分配数量（不可编辑，由明细加总得到）
     */
    @TableField(value = "AlloactedPkgQuantity")
    private BigDecimal alloactedPkgQuantity;

    /**
     * 已拣货数量（不可编辑，由明细加总得到）
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
     * 组织（货主）
     */
    @TableField(value = "OrganiztionId")
    private Long organiztionId;

    /**
     * 指定月台
     */
    @TableField(value = "Destination")
    private String destination;

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
     * 单据优先级[0,8]
     */
    @TableField(value = "Proirity")
    private Integer proirity;

    /**
     * 一键下发时间
     */
    @TableField(value = "OneClickTime")
    private Long oneClickTime;

    /**
     * 客户
     */
    @TableField(value = "OrganiztionCustomId")
    private Long organiztionCustomId;

    /**
     * 部门
     */
    @TableField(value = "OrganiztionDepartmentId")
    private Long organiztionDepartmentId;

    /**
     * 组织（供应商）
     */
    @TableField(value = "OrganiztionSupplierId")
    private Long organiztionSupplierId;

    /**
     * 指定月台站点
     */
    @TableField(value = "DestinationDock")
    private String destinationDock;

    /**
     * 库区
     */
    @TableField(value = "ZoneCode")
    private String zoneCode;

    /**
     * 库区
     */
    @TableField(value = "ZoneID")
    private Long zoneID;

    /**
     * 分组统计标识
     */
    @TableField(value = "GroupFlag")
    private String groupFlag;

    /**
     * 打印次数
     */
    @TableField(value = "PrintingTimes")
    private Long printingTimes;

    /**
     * 完成时间
     */
    @TableField(value = "CompleteTime")
    private Date completeTime;

    /**
     * 生效时间
     */
    @TableField(value = "EffectTime")
    private Date effectTime;

    /**
     * 库存是否足够
     */
    @TableField(value = "SufficientInventory")
    private Boolean sufficientInventory;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}