package gs.com.gses.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.entity.*;
import gs.com.gses.model.utility.RedisKey;
import gs.com.gses.service.*;
import gs.com.gses.utility.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BasicInfoCacheServiceImpl implements BasicInfoCacheService {
    //    @Autowired
//    @Lazy  // 防止循环依赖
//    private BasicInfoCacheService selfProxy;
    @Autowired
    private ApplicationContext applicationContext;
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
    private RedisUtil redisUtil;

    @Autowired
    private ObjectMapper objectMapper;

    public static final String LOCATION_PREFIX = "BasicInfo:Location";
    public static final String LANEWAY_PREFIX = "BasicInfo:Laneway";
    public static final String ZONE_PREFIX = "BasicInfo:Zone";
    public static final String MATERIAL_PREFIX = "BasicInfo:Material";
    public static final String WAREHOUSE_PREFIX = "BasicInfo:Warehouse";
    public static final String ORGNIZATION_PREFIX = "BasicInfo:Orgnization";
    public static final String PACKAGE_UNIT_PREFIX = "BasicInfo:PackageUnit";
    public static final String CONVEYOR_PREFIX = "BasicInfo:Conveyor";
    public static final String CONVEYOR_LANEWAY_PREFIX = "BasicInfo:ConveyorLaneway";

    public static final int EMPTY_VALUE_EXPTRE_TIME = 5;


    //__NULL__
    public static final String EMPTY_VALUE = "-1@.EmptyValue";

    @Override
    public void getBasicInfoCache() {
        Map<String, Location> locationMap = redisTemplate.opsForHash().entries(BasicInfoCacheServiceImpl.LOCATION_PREFIX);
        Location location = locationMap.get("509955478157011");
        int m = 0;
    }

    @Async("threadPoolExecutor")
    @Override
    public void initLocation() {
        log.info("start init location");
        redisTemplate.delete(LOCATION_PREFIX);
        log.info("delete Location complete");
        List<Location> list = this.locationService.list();

        Map<String, Location> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        //redis key  都是string
        HashOperations<String, String, Location> hashOps = redisTemplate.opsForHash();
        hashOps.putAll(LOCATION_PREFIX, map);
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
        redisTemplate.delete(LANEWAY_PREFIX);
        log.info("delete Laneway complete");
        List<Laneway> list = this.lanewayService.list();
        Map<String, Laneway> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(LANEWAY_PREFIX, map);
        log.info("init Laneway complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initZone() {
        log.info("start init Zone");
        redisTemplate.delete(ZONE_PREFIX);
        log.info("delete Zone complete");
        List<Zone> list = this.zoneService.list();
        Map<String, Zone> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(ZONE_PREFIX, map);
        log.info("init Zone complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initMaterial() {
        log.info("start init Material");
        redisTemplate.delete(MATERIAL_PREFIX);
        log.info("delete Material complete");
        List<Material> list = this.materialService.list();
        //双key
        Map<String, Material> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(MATERIAL_PREFIX, map);

        Map<String, Material> mapCode = list.stream().collect(Collectors.toMap(p -> p.getXCode(), p -> p));
        redisTemplate.opsForHash().putAll(MATERIAL_PREFIX, mapCode);
        log.info("init Material complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initWarehouse() {
        log.info("start init Warehouse");
        redisTemplate.delete(WAREHOUSE_PREFIX);
        log.info("delete Warehouse complete");
        List<Warehouse> list = this.warehouseService.list();
        Map<String, Warehouse> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(WAREHOUSE_PREFIX, map);
        log.info("init Warehouse complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initOrgnization() {
        log.info("start init Orgnization");
        redisTemplate.delete(ORGNIZATION_PREFIX);
        log.info("delete Orgnization complete");
        List<Orgnization> list = this.orgnizationService.list();
        Map<String, Orgnization> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(ORGNIZATION_PREFIX, map);
        log.info("init Orgnization complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initPackageUnit() {
        log.info("start init PackageUnit");
        redisTemplate.delete(PACKAGE_UNIT_PREFIX);
        log.info("delete PackageUnit complete");
        List<PackageUnit> list = this.packageUnitService.list();
        Map<String, PackageUnit> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(PACKAGE_UNIT_PREFIX, map);
        log.info("init PackageUnit complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initConveyor() {

        log.info("start init Conveyor");
        redisTemplate.delete(CONVEYOR_PREFIX);
        log.info("delete Conveyor complete");
        List<Conveyor> list = this.conveyorService.list();
        Map<String, Conveyor> map = list.stream().collect(Collectors.toMap(p -> p.getId().toString(), p -> p));
        redisTemplate.opsForHash().putAll(CONVEYOR_PREFIX, map);

        Map<String, Conveyor> codeMap = list.stream().collect(Collectors.toMap(p -> p.getXCode(), p -> p));
        redisTemplate.opsForHash().putAll(CONVEYOR_PREFIX, codeMap);

        log.info("init Conveyor complete");
    }

    @Async("threadPoolExecutor")
    @Override
    public void initConveyorLaneway() {
        log.info("start init ConveyorLaneway");
        redisTemplate.delete(CONVEYOR_LANEWAY_PREFIX);
        log.info("delete ConveyorLaneway complete");
        List<ConveyorLaneway> list = this.conveyorLanewayService.list();
        Map<String, List<Long>> conveyorGroupMap = list.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getConveyorsId().toString(),
                        Collectors.mapping(
                                p -> p.getLanewaysId(),
                                Collectors.toList()
                        )
                ));
        redisTemplate.opsForHash().putAll(CONVEYOR_LANEWAY_PREFIX, conveyorGroupMap);

        log.info("init ConveyorLaneway complete");
    }

    @Override
    public Location loadFromDbLocation(Long locationId) throws InterruptedException {
        HashOperations<String, String, Location> hashOps = redisTemplate.opsForHash();
        String key = LOCATION_PREFIX;
        Location location = (Location) hashOps.get(key, locationId.toString());
        if (location == null) {

            String lockKey = LOCATION_PREFIX + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            boolean lockSuccessfully = false;
            try {

                lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
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
//                lock.unlock();
                redisUtil.releaseLock(lock, lockSuccessfully);
            }
        }
        return location;
    }


    @Override
    public Laneway loadFromDbLaneway(Long lanewayId) throws InterruptedException {
        HashOperations<String, String, Laneway> hashOps = redisTemplate.opsForHash();
        String key = LANEWAY_PREFIX;
        Laneway laneway = (Laneway) hashOps.get(key, lanewayId.toString());
        if (laneway == null) {

            String lockKey = LANEWAY_PREFIX + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            boolean lockSuccessfully = false;
            try {

                lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
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
//                lock.unlock();
                redisUtil.releaseLock(lock, lockSuccessfully);
            }
        }
        return laneway;
    }

    @Override
    public Zone loadFromDbZone(Long zoneId) throws InterruptedException {
        HashOperations<String, String, Zone> hashOps = redisTemplate.opsForHash();
        String key = ZONE_PREFIX;
        Zone zone = (Zone) hashOps.get(key, zoneId.toString());
        if (zone == null) {

            String lockKey = ZONE_PREFIX + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            boolean lockSuccessfully = false;
            try {

                lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
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
//                lock.unlock();
                redisUtil.releaseLock(lock, lockSuccessfully);
            }
        }
        return zone;
    }


    @Override
    public Material loadFromDbMaterial(Long materialId) throws InterruptedException {
//        Material material = (Material) redisTemplate.opsForHash().get(BasicInfoCacheServiceImpl.materialPrefix, inventoryItemDetail.getMaterialId().toString());

//boolean locked = redisTemplate.opsForValue().setIfAbsent("lock:material:123", "1", 10, TimeUnit.SECONDS);


        HashOperations<String, String, Material> hashOps = redisTemplate.opsForHash();
        String key = MATERIAL_PREFIX;
        Material material = (Material) hashOps.get(key, materialId.toString());
        if (material == null) {

            String lockKey = MATERIAL_PREFIX + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            boolean lockSuccessfully = false;
            try {

                lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
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
                } else {
//                    穿透：设置个空值,待优化
                }
            } catch (Exception e) {
                throw e;
            } finally {
                //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
                //unlock 删除key
                //如果锁因超时（leaseTime）会抛异常
//                lock.unlock();
                redisUtil.releaseLock(lock, lockSuccessfully);
            }
        }
        return material;
    }

    @Override
    public Material loadFromDbMaterial(String materialCode) throws Exception {
//        Material material = (Material) redisTemplate.opsForHash().get(BasicInfoCacheServiceImpl.materialPrefix, inventoryItemDetail.getMaterialId().toString());

//boolean locked = redisTemplate.opsForValue().setIfAbsent("lock:material:123", "1", 10, TimeUnit.SECONDS);


        HashOperations<String, String, Material> hashOps = redisTemplate.opsForHash();
        String key = MATERIAL_PREFIX;
        Material material = (Material) hashOps.get(key, materialCode);
        if (material == null) {

            String lockKey = MATERIAL_PREFIX + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            boolean lockSuccessfully = false;
            try {

                lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
                if (!lockSuccessfully) {
                    log.info("Thread - {} 获得锁 {}失败！锁被占用！", Thread.currentThread().getId(), lockKey);

                    //获取不到锁，抛异常处理 服务器繁忙，稍后重试
//                    throw new Exception("服务器繁忙，稍后重试");
                    return null;
                }
                material = this.materialService.getByCode(materialCode);
                //穿透：设置个空值,待优化
                if (material != null) {
                    hashOps.put(key, materialCode, material);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
                //unlock 删除key
                //如果锁因超时（leaseTime）会抛异常
//                lock.unlock();
                redisUtil.releaseLock(lock, lockSuccessfully);
            }
        }
        return material;
    }

    @Override
    public Warehouse loadFromDbWarehouse(Long wareHouseId) throws InterruptedException {
        HashOperations<String, String, Warehouse> hashOps = redisTemplate.opsForHash();
        String key = WAREHOUSE_PREFIX;
        Warehouse warehouse = (Warehouse) hashOps.get(key, wareHouseId.toString());
        if (warehouse == null) {

            String lockKey = WAREHOUSE_PREFIX + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            boolean lockSuccessfully = false;
            try {

                lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
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
//                lock.unlock();
                redisUtil.releaseLock(lock, lockSuccessfully);
            }
        }
        return warehouse;
    }

    @Override
    public Orgnization loadFromDbOrgnization(Long orgnizationd) throws InterruptedException {
        HashOperations<String, String, Orgnization> hashOps = redisTemplate.opsForHash();
        String key = ORGNIZATION_PREFIX;
        Orgnization orgnization = (Orgnization) hashOps.get(key, orgnizationd.toString());
        if (orgnization == null) {

            String lockKey = ORGNIZATION_PREFIX + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            boolean lockSuccessfully = false;
            try {

                lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
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
//                lock.unlock();
                redisUtil.releaseLock(lock, lockSuccessfully);
            }
        }
        return orgnization;
    }

    @Override
    public PackageUnit loadFromDbPackageUnit(Long packageUnitId) throws InterruptedException {
        HashOperations<String, String, PackageUnit> hashOps = redisTemplate.opsForHash();
        String key = PACKAGE_UNIT_PREFIX;
        PackageUnit packageUnit = (PackageUnit) hashOps.get(key, packageUnitId.toString());
        if (packageUnit == null) {

            String lockKey = PACKAGE_UNIT_PREFIX + "redisson";
            //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
            RLock lock = redissonClient.getLock(lockKey);
            boolean lockSuccessfully = false;
            try {

                lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
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
//                lock.unlock();
                redisUtil.releaseLock(lock, lockSuccessfully);
            }
        }
        return packageUnit;
    }


    @Override
    public void updateLocation(Location location) throws InterruptedException {
        HashOperations<String, String, Location> hashOps = redisTemplate.opsForHash();
        String key = LOCATION_PREFIX;
        hashOps.put(key, location.getId().toString(), location);
    }

    @Override
    public void updateLaneway(Laneway laneway) throws InterruptedException {
        HashOperations<String, String, Laneway> hashOps = redisTemplate.opsForHash();
        String key = LANEWAY_PREFIX;
        hashOps.put(key, laneway.getId().toString(), laneway);
    }

    @Override
    public void updateZone(Zone zone) throws InterruptedException {
        HashOperations<String, String, Zone> hashOps = redisTemplate.opsForHash();
        String key = ZONE_PREFIX;
        hashOps.put(key, zone.getId().toString(), zone);
    }

    @Override
    public void updateMaterial(Material material) throws InterruptedException {
        HashOperations<String, String, Material> hashOps = redisTemplate.opsForHash();
        String key = MATERIAL_PREFIX;
        hashOps.put(key, material.getId().toString(), material);
    }

    @Override
    public void updateWarehouse(Warehouse wareHouse) throws InterruptedException {
        HashOperations<String, String, Warehouse> hashOps = redisTemplate.opsForHash();
        String key = WAREHOUSE_PREFIX;
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
        /*
         * Hash/String 的 put/set 操作会覆盖
         *
         * List/Set 的 push/add 操作不会覆盖，而是追加
         */
        log.info("start initBasicInfoCache");
        BasicInfoCacheService basicInfoCacheService = applicationContext.getBean(BasicInfoCacheService.class);
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

    @Override
    public boolean getSbpEnable() {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Object val = valueOperations.get(RedisKey.SBP_ENABLE);
        return val != null && val.equals(1);
    }

    @Override
    public void setSbpEnable() {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(RedisKey.SBP_ENABLE, 1, 3600, TimeUnit.SECONDS);
    }

    @Override
    public void setKeyVal(String keyVal, Object val) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(keyVal, val);
    }

    @Override
    public void setKeyValExpire(String keyVal, Object val, long timeout, TimeUnit unit) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(keyVal, val, timeout, unit);
    }

}
