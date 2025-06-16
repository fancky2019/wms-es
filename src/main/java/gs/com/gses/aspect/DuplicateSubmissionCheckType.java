package gs.com.gses.aspect;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;



public enum DuplicateSubmissionCheckType {
    /**
     *
     */
    FINGERPRINT(0, "fingerprint"),
    /**
     *
     */
    TOKEN(1, "token");

    private  int value;
    private  String description;


    public void setValue(int value) {
        this.value = value;
    }
    //jackson序列化

    //JsonVale：序列化时 枚举对应生成的值:0或1
    @JsonValue
    public int getValue() {
        return this.value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    private DuplicateSubmissionCheckType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 名称要对应  如：Zhi
     * @param str
     * @return
     */
    public static DuplicateSubmissionCheckType fromString(String str) {
        try {
               //名称要对应  如：Zhi
            return DuplicateSubmissionCheckType.valueOf(str);
        }
        catch (Exception ex)
        {
            String msg= ex.getMessage();
            return  null;
        }

    }

    public static String getDescription(int value) {
        DuplicateSubmissionCheckType[] values = values();
        for (DuplicateSubmissionCheckType unitEnum : values) {
            if (unitEnum.value ==value) {
                return unitEnum.description;
            }
        }
        return "Unknown value";
    }




    /**
     * JsonCreator ：反序列化时的 初始化函数，入参为 对应该枚举的 json值
     * @param value
     * @return
     */
    @JsonCreator
    public static DuplicateSubmissionCheckType getUnitEnum(int value) {
        //values= MessageType.values()
        for (DuplicateSubmissionCheckType item : DuplicateSubmissionCheckType.values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }

}
