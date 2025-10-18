package gs.com.gses.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 *  0:未生产 1：已生产 2：已消费 3:消费失败
 */
public enum MqMessageStatus {

    /**
     *
     */
    NOT_PRODUCED(0, "未生产"),
    PRODUCE(1, "已生产"),
    CONSUMED(2, "已消费"),
    CONSUME_FAIL(3, "消费失败");


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


    private MqMessageStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 名称要对应  如：Zhi
     * @param str
     * @return
     */
    public static MqMessageStatus fromString(String str) {
        try {
            //名称要对应  如：Zhi
            return MqMessageStatus.valueOf(str);
        }
        catch (Exception ex)
        {
            String msg= ex.getMessage();
            return  null;
        }

    }

    public static String getDescription(int value) {
        MqMessageStatus[] values = values();
        for (MqMessageStatus unitEnum : values) {
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
    public static MqMessageStatus getMqMessageStatusEnum(int value) {
        //values= MessageType.values()
        for (MqMessageStatus item : MqMessageStatus.values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
