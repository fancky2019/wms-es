package gs.com.gses.model.response.wms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class TruckOrderResponse implements Serializable {
    /**
     *
     */
    private Long id;

    /**
     *
     */
    private String truckOrderCode;

    /**
     *
     */
    private String senderAddress;

    /**
     *
     */
    private String receiverAddress;

    /**
     *
     */
    private String senderPhone;

    /**
     *
     */
    private String receiverPhone;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /**
     *
     */
    private String trunkType;

    /**
     *
     */
    private String driverPhone;

    /**
     *
     */
    private String trunkNo;
    private String filePath;
    /**
     *
     */
    private Integer deleted;

    /**
     *
     */
    private Integer version;
    private Integer status;
    /**
     *
     */
    private String creatorId;

    /**
     *
     */
    private String creatorName;

    /**
     *
     */
    private String lastModifierId;

    /**
     *
     */
    private String lastModifierName;

    /**
     *
     */
//    @DateTimeFormat	用于 接收请求参数（如表单、URL 参数）时格式化	Spring MVC 的数据绑定，例如表单提交中的时间字段
//    @JsonFormat	用于 JSON 序列化/反序列化时的时间格式控制	与 Jackson 配合，将时间转为特定格式的字符串输出到前端
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creationTime;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModificationTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
