package com.xubo.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Druid_Xu
 * @Date 2020/10/12 下午 04:49
 * @Description key 序列化器  StringRedisSerializer<T>  implements RedisSerializer<T>
 * value 序列化器 FastJsonRedisSerializer<T> implements RedisSerializer<T>
 * redisConfig 定义bean redis缓存配置  redisTemplate
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnClass(value = CachingConfigurerSupport.class)
@EnableConfigurationProperties(value = RedisProperties.class)
public class RedisConfig extends CachingConfigurerSupport {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class, StandardCharsets.UTF_8);
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();
        configuration = configuration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(fastJsonRedisSerializer)).entryTtl(Duration.ofHours(2));
        return configuration;
    }

    /**
     * Spring data redis 中有redisTemplate这个类，Springboot启动的时候回放入Spring容器，但是这里重写了一部分功能，
     * 但是这里有重新定义了这个Bean
     *
     * @return
     */
    @Bean(name = "redisTemplate")
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
//      序列化
        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class, StandardCharsets.UTF_8);
//        value值的序列化采用fastJsonRedisSerializer
        template.setValueSerializer(fastJsonRedisSerializer);
        template.setHashValueSerializer(fastJsonRedisSerializer);
//        全局开启AutoType，这里方便开发，使用全局的方式
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
//        建议使用下面这种方式
//        ParserConfig.getGlobalInstance().addAccept("com.xubo.domain");
//        key序列化采用 StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    /**
     * 自定义缓存key生成策略，默认将使用该策略
     * 直接在 return 里面写一个 KeyGenerator 接口的实现
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            Map<String, Object> container = new HashMap<>(3);
            Class<?> targetClass = target.getClass();
//            类地址
            container.put("class", targetClass.toGenericString());
//            类名
            container.put("methodName", targetClass.getName());
//            包名
            container.put("package", targetClass.getPackage());
            for (int i = 0; i < params.length; i++) {
                container.put(String.valueOf(i), params[i]);
            }
            String jsonString = JSON.toJSONString(container);
            return DigestUtils.sha256Hex(jsonString);
        };
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
//        异常处理
        log.info("初始化 -> [{}]", "Redis CacheErrorHandler");
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.error("Redis occur handleCacheGetError：key -> [{}]", key, e);
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.error("Redis occur handleCachePutError：key -> [{}]；value -> [{}]", key, value, e);
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.error("Redis occur handleCacheEvictError：key -> [{}]", key, e);
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.error("Redis occur handleCacheClearError：", e);
            }
        };
    }
}

/**
 * Value 序列化
 *
 * @param <T>
 * @author /
 */
class FastJsonRedisSerializer<T> implements RedisSerializer<T> {

    private final Class<T> clazz;
    private Charset charset;

    public FastJsonRedisSerializer(Class<T> clazz, Charset charset) {
        super();
        this.clazz = clazz;
        this.charset = charset;
    }

    /**
     * 序列化，将class 转换成字节码
     *
     * @param t
     * @return
     * @throws SerializationException
     */
    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
//        System.out.println("fastjson 序列化： ");
//        System.out.println(t);
//        判断 t 的类型
//        System.out.println(t instanceof String);
//        System.out.println(t instanceof Integer);
        String string = JSON.toJSONString(t, SerializerFeature.WriteClassName);
//       如果不加上这一行往redis里面存value 会带有 '"' value 是以String类型存入redis的
        string = string.replace("\"","");
        return string.getBytes(charset);
    }

    /**
     * 将字节码 转换成 class
     *
     * @param bytes
     * @return
     * @throws SerializationException
     */
    @Override
    public T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        String string = new String(bytes, charset);
        System.out.println("fastRedisConfiguration 反序列化 ： " + string);
        return JSON.parseObject(string, clazz);
    }
}

/**
 * 重写序列化器
 *
 * @author /
 */
class StringRedisSerializer implements RedisSerializer<Object> {
    private Charset charset;

    /**
     * 无参构造,给下面的有参构造函数赋值(UTF-8)，
     */
    StringRedisSerializer() {
        this(StandardCharsets.UTF_8);
    }

    private StringRedisSerializer(Charset charset) {
        Assert.notNull(charset, "Charset must not be null!");
        this.charset = charset;
    }

    /**
     * 序列化
     * 将对象转成 btye[]
     *
     * @param o
     * @return
     * @throws SerializationException
     */
    @Override
    public byte[] serialize(Object o) throws SerializationException {
        String string = JSON.toJSONString(o);
        if (StringUtils.isBlank(string)) {
            return null;
        }
//        序列化后key 会多出 "" ,往redis里面存储的时候不存 "" ，'"' 需要转义 处理一下 ，为什么value 序列化之后没有了？(序列化的方式不一样)
        string = string.replace("\"", "");
        System.out.println("执行key StringRedisConfiguration序列化 ： " + string);
        return string.getBytes(charset);
    }

    /**
     * 反序列化
     * byte[] 转成 Object
     *
     * @param bytes
     * @return
     * @throws SerializationException
     */
    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        System.out.println("反序列化 ： " + (String) ((bytes == null) ? null : new String(bytes, charset)));
        return (bytes == null) ? null : new String(bytes, charset);
    }
}