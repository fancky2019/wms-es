package gs.com.gses.Interceptor;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

import gs.com.gses.model.entity.TruckOrderItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * mybatisplus统一修改审计信息
 * 处理公共的审计字段
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
 *
 *
 *
 */
@Slf4j
//@Component
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

        Object originalObject = metaObject.getOriginalObject();
        if (originalObject instanceof TruckOrderItem) {
            int m = 0;
        }
        int n = 0;
    }

    @Override
    public void updateFill(MetaObject metaObject) {
//        TraceContext traceContext = (TraceContext) httpServletRequest.getAttribute(TraceContext.class.getName());
//        String traceId = traceContext.traceId();
        String traceId = MDC.get("traceId");
        //modify_time
        System.out.println("Update operation detected.");
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
        if (originalObject instanceof TruckOrderItem) {
            int m = 0;
        }
        int n = 0;
    }
}

