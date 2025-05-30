package gs.com.gses.model.response.wms;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

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
    private Date sendTime;

    /**
     *
     */
    private Integer trunkType;

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
    private LocalDateTime creationTime;

    /**
     *
     */
    private LocalDateTime lastModificationTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
