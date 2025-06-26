package gs.com.gses.service;

import gs.com.gses.model.entity.*;

public interface BasicInfoCacheService {
    void initLocation();

    void initLaneway();

    void initZone();

    void initBasicInfoCache();

    void initMaterial();

    void initWarehouse();

    void initOrgnization();

    void initPackageUnit();

    Location loadFromDbLocation(Long locationId) throws InterruptedException;

    Laneway loadFromDbLaneway(Long lanewayId) throws InterruptedException;

    Zone loadFromDbZone(Long zoneId) throws InterruptedException;

    Material loadFromDbMaterial(Long materialId) throws InterruptedException;

    Warehouse loadFromDbWarehouse(Long wareHouseId) throws InterruptedException;

    Orgnization loadFromDbOrgnization(Long orgnizationd) throws InterruptedException;

    PackageUnit loadFromDbPackageUnit(Long packageUnitId) throws InterruptedException;

    void updateLocation(Location location) throws InterruptedException;

    void updateLaneway(Laneway laneway) throws InterruptedException;

    void updateZone(Zone zone) throws InterruptedException;

    void updateMaterial(Material material) throws InterruptedException;

    void updateWarehouse(Warehouse wareHouse) throws InterruptedException;





    void batch();
}
