package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.ftp.FtpConfig;
import gs.com.gses.ftp.FtpService;
import gs.com.gses.model.entity.Material;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.MaterialRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.MaterialResponse;
import gs.com.gses.service.MaterialService;
import gs.com.gses.mapper.wms.MaterialMapper;
import gs.com.gses.utility.LambdaFunctionHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【Material】的数据库操作Service实现
 * @createDate 2024-08-11 10:16:00
 */
@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material>
        implements MaterialService {

    @Autowired
    private FtpService ftpService;

    @Autowired
    private FtpConfig ftpConfig;

    @Override
    public Material getByCode(String materialCode) throws Exception {
        if (StringUtils.isEmpty(materialCode)) {
            throw new Exception("materialCode is null ");
        }
        LambdaQueryWrapper<Material> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Material::getXCode, materialCode);
        List<Material> list = this.list(queryWrapper);
        if (list.size() > 1) {
            throw new Exception("find more than one material info  by " + materialCode);
        }
        if (list.isEmpty()) {
            throw new Exception("can't get material info  by " + materialCode);
        }
        return list.get(0);
    }

    @Override
    public List<Material> getByCodeList(List<String> materialCodeList) throws Exception {
        if (CollectionUtils.isEmpty(materialCodeList)) {
            throw new Exception("materialCodeList is null ");
        }
        LambdaQueryWrapper<Material> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Material::getXCode, materialCodeList);
        List<Material> list = this.list(queryWrapper);
//        Map<String, Material> map = list.stream().collect(Collectors.toMap(Material::getXCode, p -> p));
//        for (String materialCode : materialCodeList) {
//            Material material = map.get(materialCode);
//            if (material == null) {
//                throw new Exception("materialCode " + materialCode + " does not exist");
//            }
//        }

        Set<String> existCodes = list.stream()
                .map(Material::getXCode)
                .collect(Collectors.toSet());

        // 找出不存在的物料编码
        List<String> notExistCodes = materialCodeList.stream()
                .filter(code -> !existCodes.contains(code))
                .collect(Collectors.toList());

        if (!notExistCodes.isEmpty()) {
            throw new RuntimeException("Non-existent materialCode: " + notExistCodes);
        }
        return list;
    }

    @Override
    public List<MaterialResponse> getByMatchedCode(String materialCode) throws Exception {
        MaterialRequest materialRequest=new MaterialRequest();
        materialRequest.setPageSize(Integer.MAX_VALUE);
        materialRequest.setPageIndex(1);
        materialRequest.setXCode(materialCode);
        materialRequest.setSearchCount(false);
       PageData<MaterialResponse> pageData= this.getMaterialPage(materialRequest);
        return pageData.getData();
    }

    @Override
    public PageData<MaterialResponse> getMaterialPage(MaterialRequest request) {
        LambdaQueryWrapper<Material> queryWrapper = new LambdaQueryWrapper<>();

        if (request.getId() != null && request.getId() > 0) {
            queryWrapper.eq(Material::getId, request.getId());
        }

        if (CollectionUtils.isNotEmpty(request.getMaterialCodeList())) {
            queryWrapper.in(Material::getXCode, request.getMaterialCodeList());
        }

        if (StringUtils.isNotEmpty(request.getXCode())) {
            queryWrapper.in(Material::getXCode, request.getXCode());
        }

        // 创建分页对象 (当前页, 每页大小)
        Page<Material> page = new Page<>(request.getPageIndex(), request.getPageSize());

        if (CollectionUtils.isEmpty(request.getSortFieldList())) {
            List<Sort> sortFieldList = new ArrayList<>();
            Sort sort = new Sort();
            sort.setSortField("id");
            sort.setSortType("desc");
            sortFieldList.add(sort);
            request.setSortFieldList(sortFieldList);
        }
        if (CollectionUtils.isNotEmpty(request.getSortFieldList())) {
            List<OrderItem> orderItems = LambdaFunctionHelper.getWithDynamicSort(request.getSortFieldList());
            page.setOrders(orderItems);
        }

        if (request.getSearchCount() != null) {
            // 关键设置：不执行 COUNT 查询
            page.setSearchCount(request.getSearchCount());
        }

        // 执行分页查询, sqlserver 使用通用表达式 WITH selectTemp AS
        IPage<Material> receiptOrderPage = this.baseMapper.selectPage(page, queryWrapper);

        // 获取当前页数据
        List<Material> records = receiptOrderPage.getRecords();
        long total = receiptOrderPage.getTotal();

        List<MaterialResponse> materialResponseList = records.stream().map(p -> {
            MaterialResponse response = new MaterialResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<MaterialResponse> pageData = new PageData<>();
        pageData.setData(materialResponseList);
        pageData.setCount(total);
        return pageData;
    }


    @Override
    public void uploadInspectionTemple(MultipartFile[] files, MaterialRequest materialRequest) throws Exception {

        if (files == null || files.length == 0) {
            throw new Exception("files is null");
        }

        if (materialRequest.getId() == null || materialRequest.getId() <= 0) {
            throw new Exception("material id is null");
        }
        Material material = this.getById(materialRequest.getId());
        if (material == null) {
            throw new Exception("material is null");
        }
        String rootPath = ftpConfig.getBasePath();
        String basePath = rootPath + "Template/" + material.getXCode();
        List<String> filePathList = new ArrayList<>();

        boolean deleted = this.ftpService.deleteAllFilesInDirectory(basePath);
        if (!deleted) {
            throw new Exception("delete file error");
        }

        for (MultipartFile file : files) {
            String materialPath = MessageFormat.format("{0}/{1}", basePath, file.getOriginalFilename());
            this.ftpService.uploadFile(file.getBytes(), materialPath);
            filePathList.add(materialPath);
        }
        material.setStr10(String.join(",", filePathList));
        LambdaUpdateWrapper<Material> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Material::getId, material.getId());
        updateWrapper.set(Material::getStr10, material.getStr10());
        boolean re = this.update(updateWrapper);
        if (!re) {
            throw new Exception("update material failed");
        }
    }


}




