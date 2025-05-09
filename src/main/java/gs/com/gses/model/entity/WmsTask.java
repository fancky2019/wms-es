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
 * @TableName WmsTask
 */
@TableName(value ="WmsTask")
@Data
public class WmsTask implements Serializable {
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
     * 关联的单据的明细行项ID（上架单，拣货单，盘点移动单等）
     */
    @TableField(value = "RelationOrderItemId")
    private Long relationOrderItemId;

    /**
     * 任务编号
     */
    @TableField(value = "TaskNo")
    private String taskNo;

    /**
     * 托盘号
     */
    @TableField(value = "PalletCode")
    private String palletCode;

    /**
     * 物料id
     */
    @TableField(value = "MaterialId")
    private Long materialId;

    /**
     * 物料扩展属性id
     */
    @TableField(value = "MaterialPropertyId")
    private Long materialPropertyId;

    /**
     * 工单id
     */
    @TableField(value = "WorkOrderId")
    private Long workOrderId;

    /**
     * 状态（1open新建，2生效，3执行中，4已完成，-1作废）
     */
    @TableField(value = "XStatus")
    private Integer XStatus;

    /**
     * 原始单据号
     */
    @TableField(value = "OriginalBillCode")
    private String originalBillCode;

    /**
     * 起点位置ID
     */
    @TableField(value = "FromLocId")
    private Long fromLocId;

    /**
     * 起点位置编号
     */
    @TableField(value = "FromLocCode")
    private String fromLocCode;

    /**
     * 终点位置ID
     */
    @TableField(value = "ToLocId")
    private Long toLocId;

    /**
     * 终点位置编号
     */
    @TableField(value = "ToLocCode")
    private String toLocCode;

    /**
     * 拣货模式 1整托自动分拣手动 2全手动 3全自动
     */
    @TableField(value = "PickMode")
    private Integer pickMode;

    /**
     * 库存明细表id
     */
    @TableField(value = "InventoryItemId")
    private Long inventoryItemId;

    /**
     * 库存明细详情表ID
     */
    @TableField(value = "InventoryItemDetailId")
    private Long inventoryItemDetailId;

    /**
     * 计划数量
     */
    @TableField(value = "PlanQuantity")
    private BigDecimal planQuantity;

    /**
     * 计划包装数量
     */
    @TableField(value = "PlanPkgQuantity")
    private BigDecimal planPkgQuantity;

    /**
     * 移位数量
     */
    @TableField(value = "MovedPkgQuantity")
    private BigDecimal movedPkgQuantity;

    /**
     * 包装单位id
     */
    @TableField(value = "PackageUnitId")
    private Long packageUnitId;

    /**
     * 优先级
     */
    @TableField(value = "Proirity")
    private Integer proirity;

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
     * 托盘当前位置
     */
    @TableField(value = "CurrentLocCode")
    private String currentLocCode;

    /**
     * 描述任务执行中的信息
     */
    @TableField(value = "Description")
    private String description;

    /**
     * 起点巷道
     */
    @TableField(value = "FromLanwayNo")
    private String fromLanwayNo;

    /**
     * 高
     */
    @TableField(value = "Height")
    private BigDecimal height;

    /**
     * 长
     */
    @TableField(value = "Length")
    private BigDecimal length;

    /**
     * 托盘类型（如大小托盘，预留）
     */
    @TableField(value = "PalletType")
    private String palletType;

    /**
     * 发货批次
     */
    @TableField(value = "ShipBatchNo")
    private String shipBatchNo;

    /**
     * LED信息
     */
    @TableField(value = "Showlnfor")
    private String showlnfor;

    /**
     * 可选终点
     */
    @TableField(value = "ToAvailableLocCodes")
    private String toAvailableLocCodes;

    /**
     * 终点巷道
     */
    @TableField(value = "ToLanwayNo")
    private String toLanwayNo;

    /**
     * 实际终点
     */
    @TableField(value = "ToRealLocCode")
    private String toRealLocCode;

    /**
     * 重量
     */
    @TableField(value = "Weight")
    private BigDecimal weight;

    /**
     * 宽
     */
    @TableField(value = "Width")
    private BigDecimal width;

    /**
     * 起点列
     */
    @TableField(value = "FromColumn")
    private Integer fromColumn;

    /**
     * 起点深度
     */
    @TableField(value = "FromDepth")
    private Integer fromDepth;

    /**
     * 起点层
     */
    @TableField(value = "FromLevel")
    private Integer fromLevel;

    /**
     * 起点货架
     */
    @TableField(value = "FromRack")
    private Integer fromRack;

    /**
     * 终点列
     */
    @TableField(value = "ToColumn")
    private Integer toColumn;

    /**
     * 终点深度
     */
    @TableField(value = "ToDepth")
    private Integer toDepth;

    /**
     * 终点层
     */
    @TableField(value = "ToLevel")
    private Integer toLevel;

    /**
     * 终点货架
     */
    @TableField(value = "ToRack")
    private Integer toRack;

    /**
     * 任务类型（收货上架，库内移位，发货拣货，补货移位，盘点下架，盘点回库，库存调拨，质检下架）
     */
    @TableField(value = "TaskType")
    private Integer taskType;

    /**
     * 任务方向(1=in ,2= Out ，3= Move，4=return）
     */
    @TableField(value = "TaskDirection")
    private Integer taskDirection;

    /**
     * 工艺请求ID（入库任务接收入库模块接口写入，出库由出库模块写入，缺省为空）
     */
    @TableField(value = "RequestNo")
    private Object requestNo;

    /**
     * 原始单据类型名称
     */
    @TableField(value = "OriginalBillTypeName")
    private String originalBillTypeName;

    /**
     * 终点月台
     */
    @TableField(value = "ToDock")
    private String toDock;

    /**
     * 关联的单据的明细行项对应的主单code（上架单，拣货单，盘点移动单等）
     */
    @TableField(value = "RelationOrderItemCode")
    private String relationOrderItemCode;

    /**
     * 
     */
    @TableField(value = "TaskGroup")
    private Integer taskGroup;

    /**
     * 
     */
    @TableField(value = "ToWarehouseId")
    private Long toWarehouseId;

    /**
     * 
     */
    @TableField(value = "Carton")
    private String carton;

    /**
     * 
     */
    @TableField(value = "SerialNo")
    private String serialNo;

    /**
     * WMS移位任务对应的出库任务号
     */
    @TableField(value = "MoveTargetMainTaskNo")
    private String moveTargetMainTaskNo;

    /**
     * 实际托盘号
     */
    @TableField(value = "RealPallet")
    private String realPallet;

    /**
     * 第三方单据号
     */
    @TableField(value = "ThirdOrderCode")
    private String thirdOrderCode;

    /**
     * WMS下发给下位系统的附加信息
     */
    @TableField(value = "WmsBusinessDic")
    private String wmsBusinessDic;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}