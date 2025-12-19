package gs.com.gses.model.entity.demo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
// 禁用链式调用，否则easyexcel读取时候无法生成实体对象的值
@Accessors(chain = false)
public class EntityBase {

    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "id")
//    @ColumnWidth(25)
    @ExcelIgnore
    @TableId(value = "id", type = IdType.AUTO)
    //雪花id js number 精度丢失要转成string.前段js long 精度丢失
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public BigInteger id;

    @ExcelProperty(value = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    public LocalDateTime createTime;


    /**
     * 时间相差8小时。
     * 原因：
     * JASKSON在序列化时间时是按照国际标准时间GMT进行格式化的，而在国内默认时区使用的是CST时区，两者相差8小时，
     * 因为我们是东八区(北京时间)，所以我们在格式化的时候要指定时区(timezone)。
     * 中国时间(Asia/Shanghai) = 格林尼治时间(GMT) + 8
     * 格林尼治时间(GMT)=世界协调时间(UTC）+0
     */
    @ExcelProperty(value = "修改时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    public LocalDateTime modifyTime;
    //update_time

    @ExcelProperty(value = "版本号")
    public Integer version;

    @ExcelProperty(value = "删除")
    public boolean deleted;

    @ExcelProperty(value = "traceId")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    public String traceId;
}
