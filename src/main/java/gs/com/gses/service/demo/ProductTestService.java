package gs.com.gses.service.demo;

import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.entity.demo.ProductTest;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2022-11-17
 */
public interface ProductTestService extends IService<ProductTest> {

    ProductTest getProductTestById(int id);

    void mssqlSlaveTranTest1();

    void mssqlSlaveTranTest2();

    void mysqlSlaveTranTest1();

    void mysqlSlaveTranTest2();


    void nestedTranTest1();

    void nestedTranTest2();

    void nestedTranTest3();

}
