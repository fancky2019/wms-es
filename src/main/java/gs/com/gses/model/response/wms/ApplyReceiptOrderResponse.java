package gs.com.gses.model.response.wms;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ApplyReceiptOrderResponse implements Serializable {
    /**
     *
     */
    private Long id;

    /**
     * 单据编号
     */
    private String XCode;

    /**
     * 状态(1-打开;2-生效;3-执行中;4-已完成;5-手动完成;-1-作废)
     */
    private Integer XStatus;

    /**
     * 单据类型id
     */
    private Long billTypeId;

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
     * 备注
     */
    private String comments;

    /**
     * 仓库
     */
    private Long whid;

    /**
     * 货主
     */
    private Long organiztionId;

    /**
     * 供应商
     */
    private Long organiztionSupplierId;

    /**
     *
     */
    private Long organiztionCustomId;

    /**
     *
     */
    private Long organiztionDepartmentId;

    /**
     * 上架数量
     */
    private BigDecimal movedPkgQuantity;

    /**
     * 库区编码
     */
    private String zoneCode;

    /**
     * 库区Id
     */
    private Long zoneID;

    /**
     * 分区编码
     */
    private String areaCode;
    private static final long serialVersionUID = 1L;
}
