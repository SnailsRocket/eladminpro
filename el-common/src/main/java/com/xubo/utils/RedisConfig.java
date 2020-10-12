package com.xubo.utils;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
//import org.springframework.data.redis.core.RedisOperations;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author Druid_Xu
 * @Date 2020/10/12 下午 04:49
 * @Description
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnClass(value = CachingConfigurerSupport.class)
@EnableConfigurationProperties(value = RedisProperties.class)
public class RedisConfig extends CachingConfigurerSupport {



}

/**
 * Value 序列化
 *
 * @author /
 * @param <T>
 */
class FastJsonRedisSerializer<T> implements RedisSerializer<T> {

    @Override
    public byte[] serialize(T t) throws SerializationException {
        return new byte[0];
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        return null;
    }
}

/**
 * 重写序列化器
 *
 * @author /
 */
class StringRedisSerializer implements RedisSerializer<Object> {

    @Override
    public byte[] serialize(Object o) throws SerializationException {
        return new byte[0];
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        return null;
    }
}