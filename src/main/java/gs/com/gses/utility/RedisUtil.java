package gs.com.gses.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RedisUtil
{
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    public <HK, HV> Map<HK, HV> getHashEntries(String key, Collection<HK> hashKeys) {
        List<HV> values = redisTemplate.<HK, HV>opsForHash().multiGet(key, hashKeys);
        Map<HK, HV> result = new HashMap<>();

        Iterator<HK> keyIter = hashKeys.iterator();
        Iterator<HV> valueIter = values.iterator();

        while (keyIter.hasNext() && valueIter.hasNext()) {
            result.put(keyIter.next(), valueIter.next());
        }

        return result;
    }
}
