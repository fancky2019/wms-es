package gs.com.gses.model.response.wms;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaterialResponse {
    /**
     *
     */
    private Long id;

    /**
     * 编码（编码去重）
     */
    private String XCode;

    /**
     * 名称
     */
    private String XName;

    /**
     * 物料大类目录id
     */
    private Long materialCategoryId;

    /**
     * 简称
     */
    private String shortName;

    /**
     * 条码值
     */
    private String barcode;

    /**
     * 助记码
     */
    private String mnemonicCode;

    /**
     * 库存上限
     */
    private BigDecimal upper;

    /**
     * 库存下限
     */
    private BigDecimal lower;

    /**
     * 规格
     */
    private String spec;

    /**
     * 有效期天数
     */
    private Integer days;

    /**
     * 最小单位
     */
    private String smallestUnit;

    /**
     * 备注
     */
    private String comments;

    /**
     * 是否禁用
     */
    private Boolean isForbidden;

    /**
     * 禁用说明
     */
    private String forbiddenComments;

    /**
     * 禁用人
     */
    private Object forbiddenUserId;

    //region str
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

    //endregion

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
     * ERP账号标记
     */
    private String erpAccountSet;

    /**
     *
     */
    private String displayCode;

    /**
     *
     */
    private String erpUniqueCode;

    /**
     *
     */
    private String picUrl;

    /**
     *
     */
    private Integer closeToExpiredDays;

    /**
     *
     */
    private Integer storageWarningDays;

    /**
     * 入库层级，用于特殊需求指定物料在立库的层级
     */
    private Integer inboundLevel;

    private static final long serialVersionUID = 1L;
}
