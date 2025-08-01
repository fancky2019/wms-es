package gs.com.gses.model.request.wms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import gs.com.gses.model.request.RequestPage;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReceiptOrderRequest extends RequestPage {
    /**
     *
     */
    private Long id;

    /**
     * 收货单号
     */
    @JsonProperty("XCode")
    private String XCode;

    /**
     * 关联的入库申请单号(如果多个申请单合并入库，逗号分隔，拼接存储)
     */
    private String applyReceiptOrderCode;

    /**
     * 状态(1-打开;2-生效;3-执行中;4-已完成;5-手动完成;-1-作废)
     */
    private Integer XStatus;

    /**
     * 单据类型id
     */
    private Long billTypeId;

    /**
     * 期望收货数量（不可编辑，由明细加总得到）
     */
    private BigDecimal expectedPkgQuantity;

    /**
     * 已收货数量（不可编辑，由明细加总得到）
     */
    private BigDecimal receivedPkgQuantity;

    /**
     * 已上架数量（不可编辑，由明细加总得到）
     */
    private BigDecimal movedPkgQuantity;

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
     * 组织（客户）
     */
    private Long organiztionId;

    /**
     * 组织（供应商）
     */
    private Long organiztionSupplierId;

    /**
     * 仓库
     */
    private Long whid;

    /**
     *
     */
    private Long organiztionCustomId;

    /**
     *
     */
    private Long organiztionDepartmentId;

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
