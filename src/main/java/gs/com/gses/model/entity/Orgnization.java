package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName Orgnization
 */
@TableName(value ="Orgnization")
@Data
public class Orgnization implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 组织机构代码
     */
    @TableField(value = "XCode")
    private String XCode;

    /**
     * 组织机构名称
     */
    @TableField(value = "XName")
    private String XName;

    /**
     * 状态
     */
    @TableField(value = "XStatus")
    private Integer XStatus;

    /**
     * 国家
     */
    @TableField(value = "Country")
    private String country;

    /**
     * 省
     */
    @TableField(value = "Province")
    private String province;

    /**
     * 市
     */
    @TableField(value = "City")
    private String city;

    /**
     * 地址
     */
    @TableField(value = "Address")
    private String address;

    /**
     * 邮编
     */
    @TableField(value = "Postcode")
    private String postcode;

    /**
     * 联系人
     */
    @TableField(value = "ContactName")
    private String contactName;

    /**
     * 手机
     */
    @TableField(value = "Mobile")
    private String mobile;

    /**
     * 电话
     */
    @TableField(value = "Telephone")
    private String telephone;

    /**
     * 机构类型(1客户，2供应商，3部门，4货主)
     */
    @TableField(value = "Type")
    private Integer type;

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
     * 
     */
    @TableField(value = "DisplayCode")
    private String displayCode;

    /**
     * ERP账号标记
     */
    @TableField(value = "ErpAccountSet")
    private String erpAccountSet;

    /**
     * 
     */
    @TableField(value = "ErpUniqueCode")
    private String erpUniqueCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}