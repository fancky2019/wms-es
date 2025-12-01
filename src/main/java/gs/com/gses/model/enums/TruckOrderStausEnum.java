package gs.com.gses.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TruckOrderStausEnum {


    /**
     *
     */
    NOT_DEBITED(0, "新建"),
    DEBITING(1, "扣账中"),
    DEBITED(2, "扣账完成"),
    DEBIT_FAIL(3, "扣账失败");

    private int value;
    private String description;


    public void setValue(int value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    private TruckOrderStausEnum(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 名称要对应  如：Zhi
     * @param str
     * @return
     */
    public static TruckOrderStausEnum fromString(String str) {
        try {
            //名称要对应  如：Zhi
            return TruckOrderStausEnum.valueOf(str);
        } catch (Exception ex) {
            String msg = ex.getMessage();
            return null;
        }

    }

    public static String getDescription(int value) {
        TruckOrderStausEnum[] values = values();
        for (TruckOrderStausEnum unitEnum : values) {
            if (unitEnum.value == value) {
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
    public static TruckOrderStausEnum getTruckOrderStausEnum(int value) {
        //values= MessageType.values()
        for (TruckOrderStausEnum item : TruckOrderStausEnum.values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}

