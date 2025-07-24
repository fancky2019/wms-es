package gs.com.gses.flink;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.ObjectMapper;

import gs.com.gses.utility.TraceIdCreater;
import io.debezium.data.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.source.SourceRecord;
import org.apache.flink.cdc.debezium.DebeziumDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.data.Field;
import org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.data.Schema;
import org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.data.Struct;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author
 * @version 1.0
 * @description: mysql消息读取自定义序列化
 */
@Slf4j
public class SqlServerDeserialization implements DebeziumDeserializationSchema<DataChangeInfo> {


    public static final String TS_MS = "ts_ms";
    public static final String BIN_FILE = "file";
    public static final String POS = "pos";
    public static final String CREATE = "CREATE";
    public static final String BEFORE = "before";
    public static final String AFTER = "after";
    public static final String SOURCE = "source";
    public static final String UPDATE = "UPDATE";

//    /**
//     * 反序列化数据,转为变更JSON对象
//     */
//    @Override
//    public void deserialize(SourceRecord sourceRecord, Collector<DataChangeInfo> collector) throws JsonProcessingException {
//        String topic = sourceRecord.topic();
//        String[] fields = topic.split("\\.");
////        String database = fields[1];
//        String tableName = fields[2];
//        Struct struct = (Struct) sourceRecord.value();
//        final Struct source = struct.getStruct(SOURCE);
//        String database = "";
//
//        DataChangeInfo dataChangeInfo = new DataChangeInfo();
//        ObjectMapper objectMapper = new ObjectMapper();
//        String beforeJson = objectMapper.writeValueAsString(getJsonObject(struct, BEFORE));
//        String afterJson = objectMapper.writeValueAsString(getJsonObject(struct, AFTER));
//
//
////        dataChangeInfo.setBeforeData(getJsonObject(struct, BEFORE).toJSONString());
////        dataChangeInfo.setAfterData(getJsonObject(struct, AFTER).toJSONString());
//        dataChangeInfo.setBeforeData(beforeJson);
//        dataChangeInfo.setAfterData(afterJson);
//
//        //5.获取操作类型  CREATE UPDATE DELETE
//        Envelope.Operation operation = Envelope.operationFor(sourceRecord);
////        String type = operation.toString().toUpperCase(
////        String type = operation.toString().toUpperCase();
////        int eventType = type.equals(CREATE) ? 1 : UPDATE.equals(type) ? 2 : 3;
//        dataChangeInfo.setEventType(operation.name());
//
//
////        dataChangeInfo.setFileName(Optional.ofNullable(source.get(BIN_FILE)).map(Object::toString).orElse(""));
////        dataChangeInfo.setFilePos(Optional.ofNullable(source.get(POS)).map(x -> Integer.parseInt(x.toString())).orElse(0));
//        dataChangeInfo.setDatabase(database);
//        dataChangeInfo.setTableName(tableName);
////        dataChangeInfo.setChangeTime(Optional.ofNullable(struct.get(TS_MS)).map(x -> Long.parseLong(x.toString())).orElseGet(System::currentTimeMillis));
//        //7.输出数据
//        collector.collect(dataChangeInfo);
//    }

    private Struct getStruct(Struct value, String fieldElement) {
        return value.getStruct(fieldElement);
    }

    /**
     * 从元数据获取出变更之前或之后的数据
     */
    private HashMap<String, Object> getJsonObject(Struct value, String fieldElement) {
        Struct element = value.getStruct(fieldElement);
        HashMap<String, Object> jsonObject = new HashMap();
        if (element != null) {
            Schema afterSchema = element.schema();
            List<Field> fieldList = afterSchema.fields();
            for (Field field : fieldList) {
                Object afterValue = element.get(field);
                jsonObject.put(field.name(), afterValue);
            }
        }
        return jsonObject;
    }


    @Override
    public TypeInformation<DataChangeInfo> getProducedType() {
        return TypeInformation.of(DataChangeInfo.class);
    }

    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<DataChangeInfo> collector) throws Exception {

        try {


            //     int m=0;
            String traceId = TraceIdCreater.getTraceId();
            MDC.put("traceId", traceId);
//     return;
            String topic = sourceRecord.topic();
            String[] fields = topic.split("\\.");
//        String database = fields[1];
            String tableName = fields[2];
            Struct struct = (Struct) sourceRecord.value();
            final Struct source = struct.getStruct(SOURCE);
            String database = "";

            DataChangeInfo dataChangeInfo = new DataChangeInfo();
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, Object> beforeMap = getJsonObject(struct, BEFORE);
            log.info("beforeMap complete");
            Object id = beforeMap.get("Id");
            if (id != null) {
                dataChangeInfo.setId(id.toString());
                log.info("Id - {}} ", id);
            } else {
                log.info("Id is null ");
            }
            log.info("beforeMapId complete");
            String beforeJson = objectMapper.writeValueAsString(beforeMap);
            HashMap<String, Object> afterMap = getJsonObject(struct, AFTER);
            log.info("afterMap complete");

            if (id == null) {
                id = afterMap.get("Id");
                if (id != null) {
                    dataChangeInfo.setId(id.toString());
                    log.info("Id - {}} ", id);
                }
            }
            log.info("afterMapId complete");
            String afterJson = objectMapper.writeValueAsString(afterMap);

            dataChangeInfo.setTraceId(traceId);
//        dataChangeInfo.setBeforeData(getJsonObject(struct, BEFORE).toJSONString());
//        dataChangeInfo.setAfterData(getJsonObject(struct, AFTER).toJSONString());
            dataChangeInfo.setBeforeData(beforeJson);
            dataChangeInfo.setAfterData(afterJson);
            String lsn = source.getString("change_lsn");
            //5.获取操作类型  CREATE UPDATE DELETE
            Envelope.Operation operation = Envelope.operationFor(sourceRecord);
//        String type = operation.toString().toUpperCase(
//        String type = operation.toString().toUpperCase();
//        int eventType = type.equals(CREATE) ? 1 : UPDATE.equals(type) ? 2 : 3;
            dataChangeInfo.setEventType(operation.name());
            log.info("operation complete");

//        dataChangeInfo.setFileName(Optional.ofNullable(source.get(BIN_FILE)).map(Object::toString).orElse(""));
//        dataChangeInfo.setFilePos(Optional.ofNullable(source.get(POS)).map(x -> Integer.parseInt(x.toString())).orElse(0));
            dataChangeInfo.setDatabase(database);
            dataChangeInfo.setTableName(tableName);
            // 北京时间的时间戳
            dataChangeInfo.setChangeTime(Optional.ofNullable(struct.get(TS_MS)).map(x -> Long.parseLong(x.toString())).orElseGet(System::currentTimeMillis));


//            try {
//                String jsonStr = objectMapper.writeValueAsString(dataChangeInfo);
//                log.info("dataChangeInfoJson - ", jsonStr);
//            } catch (Exception ee) {
//                log.error("jsonStr error", ee);
//            }

            //7.输出数据  把反序列化结果发出去
            collector.collect(dataChangeInfo);
            log.info("receive {} - {} - {} ", dataChangeInfo.getTableName(), dataChangeInfo.getId(), dataChangeInfo.getEventType());

        } catch (Exception ex) {
//            objectMapper.writeValueAsString(sourceRecord);
            log.error("deserialize err {}", sourceRecord.value());
            log.error("", ex);
        } finally {
            MDC.remove("traceId");
        }
    }
}
