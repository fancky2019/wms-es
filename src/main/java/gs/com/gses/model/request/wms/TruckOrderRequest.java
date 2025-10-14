package gs.com.gses.model.request.wms;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import gs.com.gses.model.request.RequestPage;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class TruckOrderRequest extends RequestPage {
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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 用于 Spring 接收前端传入的字符串日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")  // 用于返回给前端时格式化 JSON 输出
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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 用于 Spring 接收前端传入的字符串日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")  // 用于返回给前端时格式化 JSON 输出
    private LocalDateTime creationTime;

    /**
     *
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 用于 Spring 接收前端传入的字符串日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")  // 用于返回给前端时格式化 JSON 输出
    private LocalDateTime lastModificationTime;

    private static final long serialVersionUID = 1L;
}
