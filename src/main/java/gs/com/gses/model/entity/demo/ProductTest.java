package gs.com.gses.model.entity.demo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;

import com.fasterxml.jackson.annotation.JsonFormat;
import gs.com.gses.easyecel.DropDownSetField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 *
 *
 *
 * @author author
 * @since 2022-11-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
// 禁用链式调用，否则easyexcel读取时候无法生成实体对象的值
@Accessors(chain = false)
//设置chain=true，生成setter方法返回this（也就是返回的是对象），代替了默认的返回void。
//@Accessors(chain = true)
@TableName("demo_product")
public class ProductTest extends EntityBase implements Serializable {
    /*
    @ExcelProperty
    @ColumnWith 列宽
    @ContentFontStyle 文本字体样式
    @ContentLoopMerge 文本合并
    @ContentRowHeight 文本行高度
    @ContentStyle 文本样式
    @HeadFontStyle 标题字体样式
    @HeadRowHeight 标题高度
    @HeadStyle 标题样式
    @ExcelIgnore 忽略项
    @ExcelIgnoreUnannotated 忽略未注解
    ————————————————

     */
//    @ExcelIgnore
//    private static final long serialVersionUID = 1L;
//    @ExcelProperty(value = "id")
////    @ColumnWidth(25)
//    @ExcelIgnore
//    @TableId(value = "id", type = IdType.AUTO)
//    //雪花id js number 精度丢失要转成string.前段js long 精度丢失
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
//    private BigInteger id;

    @ExcelProperty(value = "guid")
    private String guid;
    @ExcelProperty({"${productName}"})
//    @ExcelProperty(value = "产品名称")
    private String productName;

    @ExcelProperty({"${productStyle}"})
//    @ExcelProperty(value = "产品型号")
    private String productStyle;

    @ExcelProperty(value = "图片路径")
    private String imagePath;

//    @ExcelProperty(value = "创建时间")
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    @TableField(fill = FieldFill.INSERT)
//    private LocalDateTime createTime;
//
//    @ExcelProperty(value = "修改时间")
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    @TableField(fill = FieldFill.INSERT_UPDATE)
//    private LocalDateTime modifyTime;


//    @DropDownSetField(sourceClass = ProductTestStatusDropDown.class)

//    @ExcelProperty(value = "状态", converter = EnumConverterUtil.class)
    //    @ExcelProperty(value = "状态",converter = ProductTestStatusEnumConverter.class)

//    @EnumAnnotation(enumClass = ProductTestStatusEnum.class)
    private Integer status;

    @ExcelProperty(value = "描述")
    private String description;

    @ExcelProperty(value = "时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime timestamp;

//    @ExcelProperty(value = "版本号")
//    private Integer version;
//
//    @ExcelProperty(value = "traceId")
//    @TableField(fill = FieldFill.INSERT_UPDATE)
//    private String traceId;
}
