package gs.com.gses.model.request.wms;

import gs.com.gses.model.request.RequestPage;
import lombok.Data;

import java.util.List;

@Data
public class ApplyShipOrderRequest extends RequestPage {
    /**
     *
     */
    private Long id;

    /**
     * 仓库id
     */
    private Long whid;

    /**
     * 单据编号
     */
    private String XCode;

    private List<String> XCodeList;
    /**
     * 状态（1 open新建，2生效，3执行中，4已完成，-1作废）
     */
    private Integer XStatus;

    /**
     * 单据类型id
     */
    private Long billTypeId;

    /**
     * 审核状态(0 未审核 1 通过 -1不通过)
     */
    private Integer auditStatus;

    /**
     * 审核时间
     */
    private Long auditTime;

    /**
     * 指定出库口
     */
    private String destination;

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
     * 审核人
     */
    private String auditorName;

    /**
     * 单据优先级[0,8]
     */
    private Integer proirity;

    /**
     * 客户
     */
    private Long organiztionCustomId;

    /**
     * 部门
     */
    private Long organiztionDepartmentId;

    /**
     * 组织（货主）
     */
    private Long organiztionId;

    /**
     * 组织（供应商）
     */
    private Long organiztionSupplierId;

}
