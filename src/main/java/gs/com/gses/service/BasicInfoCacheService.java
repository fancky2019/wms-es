package gs.com.gses.service;

import gs.com.gses.model.entity.Material;

public interface BasicInfoCacheService {
    void initLocation();

    void initLaneway();

    void initZone();

    void initBasicInfoCache();

    void initMaterial();

    void initWarehouse();

    void initOrgnization();

    void initPackageUnit();

    void loadFromDbLocation(Long locationId);

    void loadFromDbLaneway(Long lanewayId);

    void loadFromDbZone(Long zoneId);

    Material loadFromDbMaterial(Long materialId) throws InterruptedException;

    void loadFromDbWarehouse(Long wareHouseId);

    void loadFromDbOrgnization(Long orgnizationd);

    void loadFromDbPackageUnit(Long packageUnitId);

    void batch();
}
