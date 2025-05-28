package gs.com.gses.service.elasticsearch;


import gs.com.gses.elasticsearch.ShipOrderInfoRepository;
import gs.com.gses.model.elasticsearch.DemoProduct;
import gs.com.gses.model.request.DemoProductRequest;
import gs.com.gses.model.response.PageData;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ESDemoProductService {


    @Autowired
    private ShipOrderInfoRepository shipOrderInfoRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public PageData<DemoProduct> search(DemoProductRequest request) {



        //endregion

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (request.getId() != null && request.getId() > 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("id", request.getId()));
        }
        if (StringUtils.isNotEmpty(request.getGuid())) {
            //guid 设置keyword  不成功 ES8
//            boolQueryBuilder.must(QueryBuilders.termQuery("guid.keyword", request.getGuid()));
            //es7
            boolQueryBuilder.must(QueryBuilders.termQuery("guid", request.getGuid()));
        }
        if (StringUtils.isNotEmpty(request.getProductName())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("product_name", request.getProductName()));
        }
        if (StringUtils.isNotEmpty(request.getProductStyle())) {
            //ES8要转消息，ES7不用转小写
//            //模糊查询待测试 : Wildcard 性能会比较慢。如果非必要，尽量避免在开头加通配符 ? 或者 *，这样会明显降低查询性能
//            boolQueryBuilder.must(QueryBuilders.wildcardQuery("product_style", "*" + request.getProductStyle().toLowerCase() + "*"));

            //模糊查询待测试 : Wildcard 性能会比较慢。如果非必要，尽量避免在开头加通配符 ? 或者 *，这样会明显降低查询性能
            boolQueryBuilder.must(QueryBuilders.wildcardQuery("product_style", "*" + request.getProductStyle() + "*"));

        }
        if (request.getCreateTimeStart() != null) {
            boolQueryBuilder.must(QueryBuilders.
                    rangeQuery("create_time")
                    .gte(request.getCreateTimeStart()).lte(request.getCreateTimeEnd()));
        }

        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                //查询条件:es支持分词查询，最小是一个词，要精确匹配分词
                //在指定字段中查找值
//                .withQuery(QueryBuilders.queryStringQuery("合肥").field("product_name").field("produce_address"))
                // .withQuery(QueryBuilders.multiMatchQuery("安徽合肥", "product_name", "produce_address"))

                .withQuery(boolQueryBuilder)//必须要加keyword，否则查不出来
                //SEARCH_AFTER 不用指定 from size
//                .withQuery(QueryBuilders.rangeQuery("price").from("5").to("9"))//多个条件and 的关系
                //分页：page 从0开始
                .withPageable(PageRequest.of(request.getPageIndex(), request.getPageSize()))
                //排序
                .withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC))
                //高亮字段显示
//                .withHighlightFields(new HighlightBuilder.Field("product_name"))
                .withTrackTotalHits(true)//解除最大1W条限制
                .build();
//        nativeSearchQuery.setTrackTotalHitsUpTo(10000000);
        SearchHits<DemoProduct> search = elasticsearchRestTemplate.search(nativeSearchQuery, DemoProduct.class);
        List<DemoProduct> productList = search.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());

        long count = search.getTotalHits();
        PageData<DemoProduct> pageData = new PageData<>();
        pageData.setCount(count);
        pageData.setData(productList);
//        elasticsearchRestTemplate.bulkUpdate();
//        elasticsearchRestTemplate.bulkIndex();
//        elasticsearchRestTemplate.delete()
        return pageData;
    }


    /*
     searchAfter
    上一页解决：
    对于某一页，正序search_after该页的最后一条数据id为下一页，
    则逆序search_after该页的第一条数据id则为上一页
     */

//    /**
//     * searchAfter
//     * searchAfter 不支持跳页，类似app 一页一页请求
//     * @param request
//     * @return
//     */
//    public PageData<DemoProduct> searchAfter(DemoProductRequest request) {
//
//
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        if (request.getId() != null && request.getId() > 0) {
//            boolQueryBuilder.must(QueryBuilders.termQuery("id", request.getId()));
//        }
//        if (StringUtils.isNotEmpty(request.getGuid())) {
//            //guid 设置keyword  不成功 ES8
////            boolQueryBuilder.must(QueryBuilders.termQuery("guid.keyword", request.getGuid()));
//            //es7
//            boolQueryBuilder.must(QueryBuilders.termQuery("guid", request.getGuid()));
//        }
//        if (StringUtils.isNotEmpty(request.getProductName())) {
//            boolQueryBuilder.must(QueryBuilders.matchQuery("product_name", request.getProductName()));
//        }
//        if (StringUtils.isNotEmpty(request.getProductStyle())) {
//            //ES8要转消息，ES7不用转小写
////            //模糊查询待测试 : Wildcard 性能会比较慢。如果非必要，尽量避免在开头加通配符 ? 或者 *，这样会明显降低查询性能
////            boolQueryBuilder.must(QueryBuilders.wildcardQuery("product_style", "*" + request.getProductStyle().toLowerCase() + "*"));
//
//            //模糊查询待测试 : Wildcard 性能会比较慢。如果非必要，尽量避免在开头加通配符 ? 或者 *，这样会明显降低查询性能
//            boolQueryBuilder.must(QueryBuilders.wildcardQuery("product_style", "*" + request.getProductStyle() + "*"));
//
//        }
//        if (request.getCreateTimeStart() != null) {
//            boolQueryBuilder.must(QueryBuilders.
//                    rangeQuery("create_time")
//                    .gte(request.getCreateTimeStart()).lte(request.getCreateTimeEnd()));
//        }
//
//        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
//                //查询条件:es支持分词查询，最小是一个词，要精确匹配分词
//                //在指定字段中查找值
////                .withQuery(QueryBuilders.queryStringQuery("合肥").field("product_name").field("produce_address"))
//                // .withQuery(QueryBuilders.multiMatchQuery("安徽合肥", "product_name", "produce_address"))
//
//                .withQuery(boolQueryBuilder)//必须要加keyword，否则查不出来
//                //SEARCH_AFTER 不用指定 from size
////                .withQuery(QueryBuilders.rangeQuery("price").from("5").to("9"))//多个条件and 的关系
//                //分页：page 从0开始
//                .withPageable(PageRequest.of(request.getPageIndex(), request.getPageSize()))
//                //排序
//                .withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC))
//                //高亮字段显示
////                .withHighlightFields(new HighlightBuilder.Field("product_name"))
//                .withTrackTotalHits(true)//解除最大1W条限制
//                .build();
//        //前段传上次查询的排序最后的id
//        if (request.getSearchAfterId() > 0) {
//            // "search_after": [124648691, "624812"],
//            //searchAfter 是sort 排序的字段值  最后一条的值
//            List<Object> searchAfterList = new ArrayList<>();
//            searchAfterList.add(request.getSearchAfterId());
//            nativeSearchQuery.setSearchAfter(searchAfterList);
//        }
//
////        nativeSearchQuery.setTrackTotalHitsUpTo(10000000);
//        SearchHits<DemoProduct> search = elasticsearchRestTemplate.search(nativeSearchQuery, DemoProduct.class);
//        List<DemoProduct> productList = search.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
//
//        long count = search.getTotalHits();
//        PageData<DemoProduct> pageData = new PageData<>();
//        pageData.setCount(count);
//        pageData.setData(productList);
////        elasticsearchRestTemplate.bulkUpdate();
////        elasticsearchRestTemplate.bulkIndex();
////        elasticsearchRestTemplate.delete()
//        return pageData;
//    }






}
