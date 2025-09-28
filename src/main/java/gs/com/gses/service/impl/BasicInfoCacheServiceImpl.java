package gs.com.gses.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.entity.*;
import gs.com.gses.service.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
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
    private ConveyorService conveyorService;

    @Autowired
    private ConveyorLanewayService conveyorLanewayService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ObjectMapper objectMapper;

    public static final String locationPrefix = "Location";
    public static final String lanewayPrefix = "Laneway";
    public static final String zonePrefix = "Zone";
    public static final String materialPrefix = "Material";
    public static final String warehousePrefix = "Warehouse";
    public static final String orgnizationPrefix = "Orgnization";
    public static final String packageUnitPrefix = "PackageUnit";
    public static final String conveyorPrefix = "Conveyor";
    public static final String conveyorLanewayPrefix = "ConveyorLaneway";


    public static final String DEMO_PRODUCT_PREFIX = "DemoProduct:";



    //__NULL__
    public static final String EMPTY_VALUE = "-1@.EmptyValue";

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

    @Async("threadPoolExecutor")
    @Override
    public void initConveyor() {

        log.info("start init Conveyor");
        List<Conveyor> list = this.conveyorService.list();
        Map<String, Conveyor> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(conveyorPrefix, map);

        Map<String, Conveyor> codeMap = list.stream().collect(Collectors.toMap(p -> p.getXCode(), p -> p));
        redisTemplate.opsForHash().putAll(conveyorPrefix, codeMap);

        log.info("init Conveyor complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initConveyorLaneway() {
        log.info("start init ConveyorLaneway");
        List<ConveyorLaneway> list = this.conveyorLanewayService.list();
        Map<String, List<Long>> conveyorGroupMap = list.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getConveyorsId().toString(),
                        Collectors.mapping(
                                p -> p.getLanewaysId(),
                                Collectors.toList()
                        )
                ));
        redisTemplate.opsForHash().putAll(conveyorLanewayPrefix, conveyorGroupMap);

        log.info("init ConveyorLaneway complete");
    }

    @Override
    public Location loadFromDbLocation(Long locationId) throws InterruptedException {
        HashOperations<String, String, Location> hashOps = redisTemplate.opsForHash();
        String key = locationPrefix;
        Location location = (Location) hashOps.get(key, locationId.toString());
        if (location == null) {

            String lockKey = locationPrefix + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            try {

                boolean lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
                if (!lockSuccessfully) {
                    log.info("Thread - {} 获得锁 {}失败！锁被占用！", Thread.currentThread().getId(), lockKey);

                    //获取不到锁，抛异常处理 服务器繁忙，稍后重试
//                    throw new Exception("服务器繁忙，稍后重试");
                    return null;
                }
                location = this.locationService.getById(locationId);
                //穿透：设置个空值,待优化
                if (location != null) {
                    hashOps.put(key, locationId.toString(), location);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
                //unlock 删除key
                //如果锁因超时（leaseTime）会抛异常
                lock.unlock();
            }
        }
        return location;
    }


    @Override
    public Laneway loadFromDbLaneway(Long lanewayId) throws InterruptedException {
        HashOperations<String, String, Laneway> hashOps = redisTemplate.opsForHash();
        String key = lanewayPrefix;
        Laneway laneway = (Laneway) hashOps.get(key, lanewayId.toString());
        if (laneway == null) {

            String lockKey = lanewayPrefix + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            try {

                boolean lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
                if (!lockSuccessfully) {
                    log.info("Thread - {} 获得锁 {}失败！锁被占用！", Thread.currentThread().getId(), lockKey);

                    //获取不到锁，抛异常处理 服务器繁忙，稍后重试
//                    throw new Exception("服务器繁忙，稍后重试");
                    return null;
                }
                laneway = this.lanewayService.getById(lanewayId);
                //穿透：设置个空值,待优化
                if (laneway != null) {
                    hashOps.put(key, lanewayId.toString(), laneway);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
                //unlock 删除key
                //如果锁因超时（leaseTime）会抛异常
                lock.unlock();
            }
        }
        return laneway;
    }

    @Override
    public Zone loadFromDbZone(Long zoneId) throws InterruptedException {
        HashOperations<String, String, Zone> hashOps = redisTemplate.opsForHash();
        String key = zonePrefix;
        Zone zone = (Zone) hashOps.get(key, zoneId.toString());
        if (zone == null) {

            String lockKey = zonePrefix + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            try {

                boolean lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
                if (!lockSuccessfully) {
                    log.info("Thread - {} 获得锁 {}失败！锁被占用！", Thread.currentThread().getId(), lockKey);

                    //获取不到锁，抛异常处理 服务器繁忙，稍后重试
//                    throw new Exception("服务器繁忙，稍后重试");
                    return null;
                }
                zone = this.zoneService.getById(zoneId);
                //穿透：设置个空值,待优化
                if (zone != null) {
                    hashOps.put(key, zoneId.toString(), zone);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
                //unlock 删除key
                //如果锁因超时（leaseTime）会抛异常
                lock.unlock();
            }
        }
        return zone;
    }


    @Override
    public Material loadFromDbMaterial(Long materialId) throws InterruptedException {
//        Material material = (Material) redisTemplate.opsForHash().get(BasicInfoCacheServiceImpl.materialPrefix, inventoryItemDetail.getMaterialId().toString());

//boolean locked = redisTemplate.opsForValue().setIfAbsent("lock:material:123", "1", 10, TimeUnit.SECONDS);


        HashOperations<String, String, Material> hashOps = redisTemplate.opsForHash();
        String key = materialPrefix;
        Material material = (Material) hashOps.get(key, materialId.toString());
        if (material == null) {

            String lockKey = materialPrefix + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            try {

                boolean lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
                if (!lockSuccessfully) {
                    log.info("Thread - {} 获得锁 {}失败！锁被占用！", Thread.currentThread().getId(), lockKey);

                    //获取不到锁，抛异常处理 服务器繁忙，稍后重试
//                    throw new Exception("服务器繁忙，稍后重试");
                    return null;
                }
                material = this.materialService.getById(materialId);
                //穿透：设置个空值,待优化
                if (material != null) {
                    hashOps.put(key, materialId.toString(), material);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
                //unlock 删除key
                //如果锁因超时（leaseTime）会抛异常
                lock.unlock();
            }
        }
        return material;
    }

    @Override
    public Warehouse loadFromDbWarehouse(Long wareHouseId) throws InterruptedException {
        HashOperations<String, String, Warehouse> hashOps = redisTemplate.opsForHash();
        String key = warehousePrefix;
        Warehouse warehouse = (Warehouse) hashOps.get(key, wareHouseId.toString());
        if (warehouse == null) {

            String lockKey = warehousePrefix + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            try {

                boolean lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
                if (!lockSuccessfully) {
                    log.info("Thread - {} 获得锁 {}失败！锁被占用！", Thread.currentThread().getId(), lockKey);

                    //获取不到锁，抛异常处理 服务器繁忙，稍后重试
//                    throw new Exception("服务器繁忙，稍后重试");
                    return null;
                }
                warehouse = this.warehouseService.getById(wareHouseId);
                //穿透：设置个空值,待优化
                if (warehouse != null) {
                    hashOps.put(key, wareHouseId.toString(), warehouse);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
                //unlock 删除key
                //如果锁因超时（leaseTime）会抛异常
                lock.unlock();
            }
        }
        return warehouse;
    }

    @Override
    public Orgnization loadFromDbOrgnization(Long orgnizationd) throws InterruptedException {
        HashOperations<String, String, Orgnization> hashOps = redisTemplate.opsForHash();
        String key = orgnizationPrefix;
        Orgnization orgnization = (Orgnization) hashOps.get(key, orgnizationd.toString());
        if (orgnization == null) {

            String lockKey = orgnizationPrefix + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            try {

                boolean lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
                if (!lockSuccessfully) {
                    log.info("Thread - {} 获得锁 {}失败！锁被占用！", Thread.currentThread().getId(), lockKey);

                    //获取不到锁，抛异常处理 服务器繁忙，稍后重试
//                    throw new Exception("服务器繁忙，稍后重试");
                    return null;
                }
                orgnization = this.orgnizationService.getById(orgnizationd);
                //穿透：设置个空值,待优化
                if (orgnization != null) {
                    hashOps.put(key, orgnizationd.toString(), orgnization);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
                //unlock 删除key
                //如果锁因超时（leaseTime）会抛异常
                lock.unlock();
            }
        }
        return orgnization;
    }

    @Override
    public PackageUnit loadFromDbPackageUnit(Long packageUnitId) throws InterruptedException {
        HashOperations<String, String, PackageUnit> hashOps = redisTemplate.opsForHash();
        String key = packageUnitPrefix;
        PackageUnit packageUnit = (PackageUnit) hashOps.get(key, packageUnitId.toString());
        if (packageUnit == null) {

            String lockKey = packageUnitPrefix + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            try {

                boolean lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
                if (!lockSuccessfully) {
                    log.info("Thread - {} 获得锁 {}失败！锁被占用！", Thread.currentThread().getId(), lockKey);

                    //获取不到锁，抛异常处理 服务器繁忙，稍后重试
//                    throw new Exception("服务器繁忙，稍后重试");
                    return null;
                }
                packageUnit = this.packageUnitService.getById(packageUnitId);
                //穿透：设置个空值,待优化
                if (packageUnit != null) {
                    hashOps.put(key, packageUnitId.toString(), packageUnit);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
                //unlock 删除key
                //如果锁因超时（leaseTime）会抛异常
                lock.unlock();
            }
        }
        return packageUnit;
    }


    @Override
    public void updateLocation(Location location) throws InterruptedException {
        HashOperations<String, String, Location> hashOps = redisTemplate.opsForHash();
        String key = locationPrefix;
        hashOps.put(key, location.getId().toString(), location);
    }

    @Override
    public void updateLaneway(Laneway laneway) throws InterruptedException {
        HashOperations<String, String, Laneway> hashOps = redisTemplate.opsForHash();
        String key = lanewayPrefix;
        hashOps.put(key, laneway.getId().toString(), laneway);
    }

    @Override
    public void updateZone(Zone zone) throws InterruptedException {
        HashOperations<String, String, Zone> hashOps = redisTemplate.opsForHash();
        String key = zonePrefix;
        hashOps.put(key, zone.getId().toString(), zone);
    }

    @Override
    public void updateMaterial(Material material) throws InterruptedException {
        HashOperations<String, String, Material> hashOps = redisTemplate.opsForHash();
        String key = materialPrefix;
        hashOps.put(key, material.getId().toString(), material);
    }

    @Override
    public void updateWarehouse(Warehouse wareHouse) throws InterruptedException {
        HashOperations<String, String, Warehouse> hashOps = redisTemplate.opsForHash();
        String key = warehousePrefix;
        hashOps.put(key, wareHouse.getId().toString(), wareHouse);
    }


    @Override
    public void batch() {
        //        Map<String, Material> materialMap=   redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.materialPrefix);
        Map<String, Warehouse> warehouseMap = new HashMap<>();
        Map<String, Orgnization> orgnizationMap = new HashMap<>();
        // Map<String, PackageUnit> packageUnitMap = redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.packageUnitPrefix);

    }


    @Override
    public void initBasicInfoCache() {
        /**
         * Hash/String 的 put/set 操作会覆盖
         *
         * List/Set 的 push/add 操作不会覆盖，而是追加
         */
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

        basicInfoCacheService.initConveyor();
        basicInfoCacheService.initConveyorLaneway();
        log.info("initBasicInfoCache complete");
    }

    //region redis

    /**
     * * 雪崩：随机过期时间
     * * 击穿：分布式锁（表名），没有取到锁，sleep(50)+重试 .获取不到锁，抛异常处理 服务器繁忙，稍后重试
     * * 穿透：分布式锁（表名）+设置一段时间的null值，没有取到锁，sleep(50)+重试
     *
     * @param id
     * @return
     * @throws Exception
     */
    public String loadFromDb(@PathVariable int id) throws Exception {
//        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
//        String key = ConfigConst.DEMO_PRODUCT_PREFIX + id;
//        String val = valueOperations.get(key);
//        if (StringUtils.isEmpty(val)) {
//
//            String lockKey = DEMO_PRODUCT_PREFIX + "redisson";
//            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
//            RLock lock = redissonClient.getLock(lockKey);
//            try {
//
//                boolean lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
//                if (!lockSuccessfully) {
//                    log.info("Thread - {} 获得锁 {}失败！锁被占用！", Thread.currentThread().getId(), lockKey);
//
//                    //获取不到锁，抛异常处理 服务器繁忙，稍后重试
////                    throw new Exception("服务器繁忙，稍后重试");
//                    return null;
//                }
//                BigInteger idB = BigInteger.valueOf(id);
//                ProductTest productTest = this.getById(idB);
//                //穿透：设置个空值
//                if (productTest == null) {
//                    valueOperations.set(key, EMPTY_VALUE);
//                    redisTemplate.expire(key, 60, TimeUnit.SECONDS);
//                } else {
//                    String json = objectMapper.writeValueAsString(productTest);
//                    //要设置个过期时间
//                    valueOperations.set(key, json);
//                    //[100,2000)
//                    long expireTime = ThreadLocalRandom.current().nextInt(3600, 24 * 3600);
//                    redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
//                }
//            } catch (Exception e) {
//                throw e;
//            } finally {
//                //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
//                //unlock 删除key
//                //如果锁因超时（leaseTime）会抛异常
//                lock.unlock();
//            }
//
//
//        } else {
//            if (EMPTY_VALUE.equals(val)) {
//                return null;
//            }
//        }
//
//
//        return val;
        return "";
    }
}
