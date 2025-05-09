package gs.com.gses.elasticsearch;


import gs.com.gses.model.elasticsearch.DemoProduct;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemoProductRepository extends ElasticsearchRepository<DemoProduct, Long> {
    //spring data repository 可以添加自定义方法。spring会自动实现。规则参见doc文件夹下repository
}
