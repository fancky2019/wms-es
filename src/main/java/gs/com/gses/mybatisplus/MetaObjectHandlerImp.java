package gs.com.gses.mybatisplus;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.model.entity.TruckOrder;
import gs.com.gses.model.entity.TruckOrderItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 *
 *
 * mybatisplus统一修改审计信息
 * 审计字段填充，不能操作物理删除，可以设计逻辑删除
 *
 *  更新时指定实体才会触发。批量更新实体也会触发
 *  boolean re1 = this.update(productTest, updateWrapper2);
 *
 * 指定null 不会触发：  this.update(updateWrapper3);*  this.update(null,updateWrapper3);
 *
 *
 * 如果没进入MetaObjectHandlerImp 可在 SqlSessionFactory bean 里 配置MetaObjectHandlerImp。参见MybatisPlusDataSourceConfig
 *
 *实体类字段有 @TableField(fill = ...) 注解
 *
 *
 * 使用正确的更新方法（updateById 或 update(entity, wrapper)）
 *
 *
 * 事务可能执行不成功：此方法先事务提交执行
 *
 *插入无法获取id信息
 *
 *
 */
@Slf4j
@Component
public class MetaObjectHandlerImp implements MetaObjectHandler {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    public void insertFill(MetaObject metaObject) {
        //create_time
//        System.out.println("Insert operation detected.");
//        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
//        this.strictInsertFill(metaObject, "modifyTime", LocalDateTime.class, LocalDateTime.now());
//        this.strictInsertFill(metaObject, "traceId", String.class, MDC.get("traceId"));

        //  这里只能处理非ID字段的填充
        Object originalObject = metaObject.getOriginalObject();
        if (originalObject instanceof TruckOrder) {
            TruckOrder truckOrder = (TruckOrder) originalObject;
            log.info("TruckOrderInsert:truckOrderId {} truckOrderCode {} filePath {}", truckOrder.getId(), truckOrder.getTruckOrderCode(),truckOrder.getFilePath());
        } else if (originalObject instanceof TruckOrderItem) {
            TruckOrderItem truckOrderItem = (TruckOrderItem) originalObject;
            log.info("TruckOrderItemInsert:truckOrderId {} truckOrderItemId {}", truckOrderItem.getTruckOrderId(), truckOrderItem.getId());
        } else if (originalObject instanceof MqMessage) {
            MqMessage message = (MqMessage) originalObject;
            log.info("MqMessageInsert:BusinessId {} MsgContent {} status{}", message.getBusinessId(), message.getMsgContent(),message.getStatus());
        }
        int n = 0;
    }

    @Override
    public void updateFill(MetaObject metaObject) {
//        TraceContext traceContext = (TraceContext) httpServletRequest.getAttribute(TraceContext.class.getName());
//        String traceId = traceContext.traceId();
        String traceId = MDC.get("traceId");
        //modify_time
//        System.out.println("Update operation detected.");
//        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
//        this.strictInsertFill(metaObject, "traceId", String.class, traceId);

//        if (metaObject.getOriginalObject() instanceof EntityBase) {
//            EntityBase entityBase = (EntityBase) metaObject.getOriginalObject();
//            log.info("id - {} traceId {}-->{}", entityBase.getId(), entityBase.getTraceId(), traceId);
//        }
//
//        this.setFieldValByName("modifyTime", LocalDateTime.now(), metaObject);
//        this.setFieldValByName("traceId", traceId, metaObject);
//        if (metaObject.getOriginalObject() instanceof ProductTest) {
//            log.info("ProductTest");
//        }


//        Object oldValue = this.getFieldValByName("oldValue", metaObject);
//        Object newValue = this.getFieldValByName("newValue", metaObject);

//        System.out.println("Old value: " + oldValue);
//        System.out.println("New value: " + newValue);


        Object originalObject = metaObject.getOriginalObject();
        if (originalObject instanceof TruckOrder) {
            TruckOrder truckOrder = (TruckOrder) originalObject;
            log.info("TruckOrderUpdate:truckOrderId {} truckOrderCode {} filePath {}", truckOrder.getId(), truckOrder.getTruckOrderCode(),truckOrder.getFilePath());
        } else if (originalObject instanceof TruckOrderItem) {
            TruckOrderItem truckOrderItem = (TruckOrderItem) originalObject;
            log.info("TruckOrderItemUpdate:truckOrderId {} truckOrderItemId {}", truckOrderItem.getTruckOrderId(), truckOrderItem.getId());
        }else if (originalObject instanceof MqMessage) {
            MqMessage message = (MqMessage) originalObject;
            log.info("MqMessageUpdate:BusinessId {} MsgContent {} status{}", message.getBusinessId(), message.getMsgContent(),message.getStatus());
        }
        int n = 0;
    }
}

