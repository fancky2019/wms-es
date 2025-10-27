package gs.com.gses.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import gs.com.gses.config.zonedDateTimeConfig.ZonedDateTimeDeserializer;
import gs.com.gses.config.zonedDateTimeConfig.ZonedDateTimeSerializer;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * 添加此配置类解决框架redis序列化存储乱码问题
 */
@Configuration
public class RedisConfig {

    @Autowired
    @Qualifier("nonEmptyObjectMapper")
    public ObjectMapper nonEmptyObjectMapper;


    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);
        ObjectMapper objectMapper = new ObjectMapper();

//
//        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
//        //jackson 序列化器
//        // key采用String的序列化方式
//        template.setKeySerializer(stringRedisSerializer);
//        // hash的key也采用String的序列化方式
//        template.setHashKeySerializer(stringRedisSerializer);
//        // value序列化方式采用jackson
//        template.setValueSerializer(jackson2JsonRedisSerializer);
//        // hash的value序列化方式采用jackson
//        template.setHashValueSerializer(jackson2JsonRedisSerializer);

//        //使用jasckson 配置
//        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
//        //jackson 序列化器
//        // key采用String的序列化方式
//        template.setKeySerializer(stringRedisSerializer);
//        // hash的key也采用String的序列化方式
//        template.setHashKeySerializer(stringRedisSerializer);
//        // value序列化方式采用jackson
//        template.setValueSerializer(jackson2JsonRedisSerializer());
//        // hash的value序列化方式采用jackson
//        template.setHashValueSerializer(jackson2JsonRedisSerializer());



        // 使用 MessagePack 序列化器。可减小30%内存。内存占用命令：MEMORY USAGE BasicInfo:Material
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jacksonMessagePackSerializer());
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(jacksonMessagePackSerializer());


        template.afterPropertiesSet();
        return template;

    }

    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplateObj(RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();

        return template;

    }

    /**
     * 配置cacheManager
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory) {
        //默认的redisCahce配置，此处我们没用，而是自己配置
        //RedisCacheConfiguration.defaultCacheConfig();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置缓存的默认过期时间
                .entryTtl(Duration.ofSeconds(180))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(this.jackson2JsonRedisSerializer()));
        // 不缓存空值
//                .disableCachingNullValues();
        //根据redis缓存配置和reid连接工厂生成redis缓存管理器
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
        return redisCacheManager;
    }


    /***
     * 配置jackson2JsonRedisSerializer
     * @return
     */
    private Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(nonEmptyObjectMapper);
        return jackson2JsonRedisSerializer;

    }


    @Bean
    public RedisSerializer<Object> jacksonMessagePackSerializer() {
        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
        //        ZonedDateTime
        // 设置时区为 GMT+8   UTC
        //LocalDateTime 是一个不带时区的日期时间类型，它只表示一个本地时间。
        //当你将 LocalDateTime 序列化为 JSON 时，Jackson 会直接将其值输出，而不会进行任何时区转换。
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 关闭时间戳模式:jackson ZonedDateTime  默认序列化是时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        全局配置忽略实体中不存在的字段  _class 字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
// 使用小写字母开头的命名策略 LOWER_CAMEL_CASE
//        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE);
        //不序列化空值 ：默认JsonInclude.Include.ALWAYS
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);


        //PropertyAccessor.ALL：指定对所有类型的属性访问器生效
        //JsonAutoDetect.Visibility.ANY：允许检测所有类型的属性（包括 private）
//        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        objectMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
//        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //Jackson 在序列化每个对象时都附带类型信息（@class 字段），//注释了 禁用类型信息
        // objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);


        // 日期和时间格式化
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));


        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));

        // 注册自定义的序列化和反序列化器
        javaTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());
        javaTimeModule.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
//        javaTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
//        javaTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(DateTimeFormatter.ISO_ZONED_DATE_TIME));

//        SimpleModule module = new SimpleModule();
        // "" 不序列化成null
//        javaTimeModule.addSerializer(String.class, new JsonSerializer<String>() {
//            @Override
//            public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
////                if (value == null || value.isEmpty()) {
//                if (value == null) {
//                    gen.writeNull(); // 序列化为 null
//                } else {
//                    gen.writeString(value); // 正常序列化
//                }
//            }
//        });
//        objectMapper.registerModule(module);

        objectMapper.registerModule(javaTimeModule);

// 使用 Jackson2JsonRedisSerializer 并传入 MessagePack ObjectMapper
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        serializer.setObjectMapper(objectMapper);

//        return new GenericJackson2JsonRedisSerializer(objectMapper);
        return serializer;
    }

}
