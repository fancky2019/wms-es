package gs.com.gses.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//移到 MybatisPlusDataSourceConfig 里，要添加到SqlSessionFactory 里，否则分页失效
//@Configuration
public class MybatisPlusPageInterceptor {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //        PaginationInnerInterceptor paginationInterceptor=new PaginationInnerInterceptor(DbType.SQL_SERVER);

        PaginationInnerInterceptor paginationInterceptor=new PaginationInnerInterceptor();
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}
