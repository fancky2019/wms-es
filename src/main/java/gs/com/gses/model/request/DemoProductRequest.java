package gs.com.gses.model.request;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DemoProductRequest extends Page {
    private Integer id;
    private String guid;
    private String productName;
    private String productStyle;
    private String imagePath;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
    private LocalDateTime modifyTime;
    private long status;
    private String description;
    private LocalDateTime timestamp;

    //searchAfter 排序字段值，前段要传过来
    private Integer searchAfterId;
    private Integer searchAfterCreateTime;
}
