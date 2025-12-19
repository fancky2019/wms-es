package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.entity.WmsTask;
import gs.com.gses.model.request.wms.UpdateWmsTaskStatusRequest;
import gs.com.gses.model.response.wms.WmsResponse;
import gs.com.gses.service.WmsTaskService;
import gs.com.gses.mapper.wms.WmsTaskMapper;
import gs.com.gses.service.api.WmsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lirui
 * @description 针对表【WmsTask】的数据库操作Service实现
 * @createDate 2024-08-11 10:42:56
 */
@Slf4j
@Service
public class WmsTaskServiceImpl extends ServiceImpl<WmsTaskMapper, WmsTask>
        implements WmsTaskService {

    @Autowired
    private WmsService wmsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void cancelTaskByIdList(List<Long> idList, String token) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        UpdateWmsTaskStatusRequest request = null;
        for (Long id : idList) {
            request = new UpdateWmsTaskStatusRequest();
            request.setId(id);
            request.setXStatus(-1);
            request.setDescription("1");
            try {
                log.info("id - {} start request", id);
                WmsResponse wmsResponse = wmsService.CompleteOffline(request, token);
                String jsonResponse = objectMapper.writeValueAsString(wmsResponse);
                log.info("id - {} ,jsonResponse - {}", id, jsonResponse);
            } catch (Throwable ex) {
                log.error("id " + id, ex);
            }

        }

    }
}




