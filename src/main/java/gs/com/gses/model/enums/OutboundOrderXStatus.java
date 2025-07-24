package gs.com.gses.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OutboundOrderXStatus
{

    /**
     *
     */
    NEW(1, "open新建"),
    ENABLED(2, "生效"),
    EXECUTING(3, "执行中"),
    COMPLETED(4, "已完成"),
    SHIPED(5, "已发运"),
    CANCEL(-1, "作废");

    private  int value;
    private  String description;


    public void setValue(int value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    private OutboundOrderXStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 名称要对应  如：Zhi
     * @param str
     * @return
     */
    public static OutboundOrderXStatus fromString(String str) {
        try {
            //名称要对应  如：Zhi
            return OutboundOrderXStatus.valueOf(str);
        }
        catch (Exception ex)
        {
            String msg= ex.getMessage();
            return  null;
        }

    }

    public static String getDescription(int value) {
        OutboundOrderXStatus[] values = values();
        for (OutboundOrderXStatus unitEnum : values) {
            if (unitEnum.value ==value) {
                return unitEnum.description;
            }
        }
        return "Unknown value";
    }

    //jackson序列化

    //JsonVale：序列化时 枚举对应生成的值:0或1
    @JsonValue
    public int getValue() {
        return this.value;
    }



    /**
     * JsonCreator ：反序列化时的 初始化函数，入参为 对应该枚举的 json值
     * @param value
     * @return
     */
    @JsonCreator
    public static OutboundOrderXStatus getUnitEnum(int value) {
        //values= MessageType.values()
        for (OutboundOrderXStatus item : OutboundOrderXStatus.values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
