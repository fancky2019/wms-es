package gs.com.gses.model.elasticsearch;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
数据库字段是下划线   数据库用logstash同步到ES上  ES字段也是下划线
java 是小驼峰命名。es 字段也用小驼峰，那么logstash同步mysql数据到es,sql查询语句使用小驼峰别名。
如：select student_name as studentName from student;
或者使用@Field 注解

text支持默认分词，keyword不支持分词

IK 分词器：ik_smart:尽可能少的进行中文分词。ik_max_word:尽可能多的进行中文分词。
analyzer = "ik_max_word"
 */
@Document(indexName = "demo_product")
@Data
public class DemoProduct implements Serializable {
    //    @Id 如果和es的id 名称不一样映射es的id.
    private Integer id;
    private String guid;
    /*
          java 实体和es字段名映射
     */
  //    @Field(name = "product_name",type = FieldType.Text)
    @Field(name = "product_name",analyzer = "ik_max_word",type = FieldType.Text)//中文分词设置
//    @Field(name = "product_name",type = FieldType.Text)//中文分词设置
    private String productName;
    @Field(name = "product_style")
    private String productStyle;

    @Field(name = "image_path")
    private String imagePath;

//    @Field(name = "create_time")
    //es 到java实体时间的转换格式
//    @Field(name = "create_time",index = true, store = true, type = FieldType.Date,format = DateFormat.custom, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    //es7
        @Field(name = "create_time",index = true, store = true, type = FieldType.Date,format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    //ES8
        //    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
        //ES7
        @Field(name = "modify_time", index = true, store = true, type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
//ES8
//    @Field(name = "modify_time",index = true, store = true, type = FieldType.Date,format = DateFormat.custom, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        //    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime modifyTime;


    private long status;

//    @Field(name = "produce_address",analyzer = "ik_max_word")
    @Field(name = "produce_address")
    private String description;
////DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//// pattern = "yyyy-MM-dd HH:mm:ss.SSS"
//// pattern = "yyyy-MM-dd HH:mm:ss"
//    @Field(name = "timestamp",index = true, store = true, type = FieldType.Date,format = DateFormat.custom, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
////    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
////    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    private LocalDateTime timestamp;
}
