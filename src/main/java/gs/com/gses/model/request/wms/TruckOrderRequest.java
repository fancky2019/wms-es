package gs.com.gses.model.request.wms;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import gs.com.gses.model.request.RequestPage;
import lombok.Data;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creationTime;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModificationTime;

    private static final long serialVersionUID = 1L;
}
