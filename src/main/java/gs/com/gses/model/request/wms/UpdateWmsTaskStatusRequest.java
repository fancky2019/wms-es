package gs.com.gses.model.request.wms;

import lombok.Data;

@Data
public class UpdateWmsTaskStatusRequest {
    /**
     *  任务id
     */
    private Long id;
    /**
     * 状态（1open新建，2生效，3执行中，4已完成，-1作废）
     */
    private Integer xStatus;
    private String description = "";
    /**
     * 是否系统自动报完成，默认false，可不填
     */
    private Boolean isSelfComplete = false;

}
