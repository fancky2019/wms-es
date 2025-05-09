package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName ApplyShipOrderArchieved
 */
@TableName(value ="ApplyShipOrderArchieved")
@Data
public class ApplyShipOrderArchieved implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

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
     * 仓库id
     */
    @TableField(value = "Whid")
    private Long whid;

    /**
     * 单据编号
     */
    @TableField(value = "XCode")
    private String XCode;

    /**
     * 状态（1 open新建，2生效，3执行中，4已完成，-1作废）
     */
    @TableField(value = "XStatus")
    private Integer XStatus;

    /**
     * 单据类型id
     */
    @TableField(value = "BillTypeId")
    private Long billTypeId;

    /**
     * 审核状态(0 未审核 1 通过 -1不通过)
     */
    @TableField(value = "AuditStatus")
    private Integer auditStatus;

    /**
     * 审核人
     */
    @TableField(value = "AuditorName")
    private String auditorName;

    /**
     * 审核时间
     */
    @TableField(value = "AuditTime")
    private Long auditTime;

    /**
     * 指定出库口
     */
    @TableField(value = "Destination")
    private String destination;

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
     * 单据优先级[0,8]
     */
    @TableField(value = "Proirity")
    private Integer proirity;

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
     * 组织（货主）
     */
    @TableField(value = "OrganiztionId")
    private Long organiztionId;

    /**
     * 组织（供应商）
     */
    @TableField(value = "OrganiztionSupplierId")
    private Long organiztionSupplierId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}