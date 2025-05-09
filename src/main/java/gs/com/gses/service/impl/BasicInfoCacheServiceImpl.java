package gs.com.gses.service.impl;

import gs.com.gses.model.entity.*;
import gs.com.gses.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BasicInfoCacheServiceImpl implements BasicInfoCacheService {

    @Autowired
    private LocationService locationService;

    @Autowired
    private LanewayService lanewayService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private OrgnizationService orgnizationService;

    @Autowired
    private PackageUnitService packageUnitService;

    @Autowired
    private RedisTemplate redisTemplate;


    public static final String locationPrefix = "Location";
    public static final String lanewayPrefix = "Laneway";
    public static final String zonePrefix = "Zone";
    public static final String materialPrefix = "Material";
    public static final String warehousePrefix = "Warehouse";
    public static final String orgnizationPrefix = "Orgnization";
    public static final String packageUnitPrefix = "PackageUnit";

    @Async("threadPoolExecutor")
    @Override
    public void initLocation() {
        log.info("start init location");
        List<Location> list = this.locationService.list();

        Map<String, Location> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        //redis key  都是string
        HashOperations<String, String, Location> hashOps = redisTemplate.opsForHash();
        hashOps.putAll(locationPrefix, map);
//        redisTemplate.opsForValue().multiSet(map);
//        Map<String, Location> locationMap=   hashOps.entries(locationPrefix);

//        Pipeline 可以显著提高批量操作的性能，减少网络往返时间(RTT)。
//        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
//            for (Pair<String, Object> pair : map) {
//                byte[] key = redisTemplate.getKeySerializer().serialize(pair.getKey());
//                byte[] value = redisTemplate.getValueSerializer().serialize(pair.getValue());
//                connection.set(key, value);
//            }
//            return null;
//        });


        log.info("init location complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initLaneway() {
        log.info("start init Laneway");
        List<Laneway> list = this.lanewayService.list();
        Map<String, Laneway> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(lanewayPrefix, map);
        log.info("init Laneway complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initZone() {
        log.info("start init Zone");
        List<Zone> list = this.zoneService.list();
        Map<String, Zone> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(zonePrefix, map);
        log.info("init Zone complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initMaterial() {
        log.info("start init Material");
        List<Material> list = this.materialService.list();
        Map<String, Material> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(materialPrefix, map);
        log.info("init Material complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initWarehouse() {
        log.info("start init Warehouse");
        List<Warehouse> list = this.warehouseService.list();
        Map<String, Warehouse> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(warehousePrefix, map);
        log.info("init Warehouse complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initOrgnization() {
        log.info("start init Orgnization");
        List<Orgnization> list = this.orgnizationService.list();
        Map<String, Orgnization> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(orgnizationPrefix, map);
        log.info("init Orgnization complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initPackageUnit() {
        log.info("start init PackageUnit");
        List<PackageUnit> list = this.packageUnitService.list();
        Map<String, PackageUnit> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(packageUnitPrefix, map);
        log.info("init PackageUnit complete");
    }


    @Override
    public void initBasicInfoCache() {
        log.info("start initBasicInfoCache");
        Object proxyObj = AopContext.currentProxy();
        BasicInfoCacheService basicInfoCacheService = null;
        if (proxyObj instanceof BasicInfoCacheService) {
            basicInfoCacheService = (BasicInfoCacheService) proxyObj;
        } else {
            log.info("get proxyObj exception");
            return;
        }

        basicInfoCacheService.initLocation();
        basicInfoCacheService.initLaneway();
        basicInfoCacheService.initZone();
        basicInfoCacheService.initMaterial();
        basicInfoCacheService.initWarehouse();
        basicInfoCacheService.initOrgnization();
        basicInfoCacheService.initPackageUnit();
        log.info("initBasicInfoCache complete");
    }
}
