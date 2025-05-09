package gs.com.gses.utility;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PooledObjectFactoryImpl<T> implements PooledObjectFactory<T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private Class<T> tClass;

    public PooledObjectFactoryImpl(Class<T> tClass) {
        this.tClass = tClass;
    }

    @Override
    public PooledObject<T> makeObject() throws Exception {
        return new DefaultPooledObject<>(
                (T) tClass.newInstance());
    }

    @Override
    public void destroyObject(PooledObject<T> pooledObject) throws Exception {
        LOGGER.info("destroyObject..state = {} {}", pooledObject.getState() ,pooledObject.toString());
    }

    @Override
    public boolean validateObject(PooledObject<T> pooledObject) {
        LOGGER.info("validateObject..state = {}", pooledObject.getState());
        return true;
    }

    @Override
    public void activateObject(PooledObject<T> pooledObject) throws Exception {
        LOGGER.info("activateObject..state = {}", pooledObject.getState());
    }

    @Override
    public void passivateObject(PooledObject<T> pooledObject) throws Exception {
        LOGGER.info("passivateObject..state = {}", pooledObject.getState());
    }
}
