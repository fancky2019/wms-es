package gs.com.gses.model.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShipOrderResponse {
    /**
     *
     */
    private Long id;

    /**
     * 仓库id
     */
    private Long whid;

    /**
     * 出库单号
     */
    private String XCode;

    /**
     * 关联的出库申请单号(如果多个申请单合并出库，逗号分隔，拼接存储)
     */
    private String applyShipOrderCode;

    /**
     * 状态（1 open新建，2生效，3执行中，4已完成，5已发运,-1作废）
     */
    private Integer XStatus;

    /**
     * 单据类型id
     */
    private Long billTypeId;

    /**
     * 期望出库数量（不可编辑，由明细加总得到）
     */
    private BigDecimal expectedPkgQuantity;

    /**
     * 已分配数量（不可编辑，由明细加总得到）
     */
    private BigDecimal alloactedPkgQuantity;

    /**
     * 已拣货数量（不可编辑，由明细加总得到）
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
     * 组织（货主）
     */
    private Long organiztionId;

    /**
     * 指定月台
     */
    private String destination;

    /**
     * 终点位置Code
     */
    private String toLocCode;

    /**
     * 终点位置ID
     */
    private Long toLocId;

    /**
     * 单据优先级[0,8]
     */
    private Integer proirity;

    /**
     * 一键下发时间
     */
    private Long oneClickTime;

    /**
     * 客户
     */
    private Long organiztionCustomId;

    /**
     * 部门
     */
    private Long organiztionDepartmentId;

    /**
     * 组织（供应商）
     */
    private Long organiztionSupplierId;

    /**
     * 指定月台站点
     */
    private String destinationDock;

    /**
     * 库区
     */
    private String zoneCode;

    /**
     * 库区
     */
    private Long zoneID;

    /**
     * 分组统计标识
     */
    private String groupFlag;

    /**
     * 打印次数
     */
    private Long printingTimes;
    /**
     *
     */
    private String shipOrderCode;

    /**
     *
     */
    private String pallet;

    /**
     *
     */
    private Long inventoryItemDetailId;
    private static final long serialVersionUID = 1L;
}