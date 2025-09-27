package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName Conveyor_Laneway
 */
@TableName(value ="Conveyor_Laneway")
@Data
public class ConveyorLaneway {
    /**
     * 联合主键不设置@TableId
     */
//    @TableId(value = "ConveyorsId")
    private Long conveyorsId;

    /**
     * 
     */
//    @TableId(value = "LanewaysId")
    private Long lanewaysId;
}