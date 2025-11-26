package gs.com.gses.service;

import gs.com.gses.model.entity.TruckOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.request.wms.AddTruckOrderRequest;
import gs.com.gses.model.request.wms.TruckOrderRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.mqtt.TrunkOderMq;
import gs.com.gses.model.response.wms.TruckOrderResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * @author lirui
 * @description 针对表【TruckOrder】的数据库操作Service
 * @createDate 2025-05-28 13:18:54
 */
public interface TruckOrderService extends IService<TruckOrder> {
    void addTruckOrderAndItem(AddTruckOrderRequest request, String token) throws Throwable;
    void addTruckOrderAndItemAsync(AddTruckOrderRequest request, String token) throws Throwable;

    void addTruckOrderAndItemOnly(AddTruckOrderRequest request, String token) throws Throwable;

    void updateTruckOrder(MultipartFile[] files, TruckOrderRequest request) throws Exception;

    public void expungeStaleAttachment(long id) throws Exception;

    TruckOrder add(TruckOrderRequest truckOrderRequest);

    PageData<TruckOrderResponse> getTruckOrderPage(TruckOrderRequest request);

    void trunkOrderMq(Integer id) throws Exception;

    void exportTrunkOrderExcel(Long id, HttpServletResponse httpServletResponse) throws Exception;


    Boolean deleteByIds(List<Long> idList);
}
