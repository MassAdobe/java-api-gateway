package com.guangl.gateway.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.guangl.gateway.enums.ErrorCodeMsg;
import com.guangl.gateway.exception.GatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * RedisTemplate工具类：
 * 针对所有的hash都是以h开头的方法
 * 针对所有的Set都是以s开头的方法(不含通用方法)
 * 针对所有的List都是以l开头的方法
 */

/**
 * @ClassName: RedisClient
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: RedisTemplate工具类：针对所有的hash都是以h开头的方法，针对所有的Set都是以s开头的方法(不含通用方法)，针对所有的List都是以l开头的方法
 * @Date: Created in 2019-12-18 15:03
 * @Version: 1.0.0
 * @param: * @param null
 */
@Component
public class RedisClient {

    private final static Logger logger = LoggerFactory.getLogger(RedisClient.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RedisTemplate redisTemplate;

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // =============================common============================

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    public void expire(String key, long time) throws GatewayException {
        try {
            if (time > 0) {
                logger.info(Strings.lenientFormat("【REDIS】：EXEC EXPIRE; KEY:%s, TIME: %s", key, String.valueOf(time)));
                this.redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-EXPIRE】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC GET EXPIRE; KEY:%s", key));
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-GET-EXPIRE】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC HASKEY; KEY:%s", key));
            return this.redisTemplate.hasKey(key);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-HASKEY】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                logger.info(Strings.lenientFormat("【REDIS】：EXEC DEL; KEY:%s", key[0]));
                redisTemplate.delete(key[0]);
            } else {
                logger.info(Strings.lenientFormat("【REDIS】：EXEC DEL; KEY:%s", CollectionUtils.arrayToList(key).toString()));
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    // ============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        logger.info(Strings.lenientFormat("【REDIS】：EXEC GET; KEY:%s", key));
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) throws GatewayException {
        try {
            String val = objectMapper.writeValueAsString(value);
            logger.info(Strings.lenientFormat("【REDIS】：EXEC SET; KEY:%s, VALUE:%s", key, val));
            this.redisTemplate.opsForValue().set(key, val);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-SET】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public void set(String key, Object value, long time) throws GatewayException {
        try {
            String val = objectMapper.writeValueAsString(value);
            logger.info(Strings.lenientFormat("【REDIS】：EXEC SET EXPIRE; KEY:%s, VALUE:%s, TIME:%s", key, val, String.valueOf(time)));
            if (time > 0)
                this.redisTemplate.opsForValue().set(key, val, time, TimeUnit.SECONDS);
            else
                set(key, value);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-SET-EXPIRE】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta) throws GatewayException {
        if (delta < 0) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-INCR】：%s", ErrorCodeMsg.REDIS_INCR_ERROR.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_INCR_ERROR);
        }
        logger.info(Strings.lenientFormat("【REDIS】：EXEC INCR; KEY:%s, DELTA-TIME:%s", key, String.valueOf(delta)));
        return this.redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta) throws GatewayException {
        if (delta < 0) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-DECR】：%s", ErrorCodeMsg.REDIS_INCR_ERROR.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_INCR_ERROR);
        }
        logger.info(Strings.lenientFormat("【REDIS】：EXEC DECR; KEY:%s, DELTA-TIME:%s", key, String.valueOf(delta)));
        return this.redisTemplate.opsForValue().increment(key, -delta);
    }

    // ================================Map=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public Object hget(String key, String item) {
        logger.info(Strings.lenientFormat("【REDIS】：EXEC H-GET; KEY:%s, SUB-KEY:%s", key, item));
        return this.redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        logger.info(Strings.lenientFormat("【REDIS】：EXEC H-MGET; KEY:%s", key));
        return this.redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public void hmset(String key, Map<String, Object> map) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC H-MSET; KEY:%s, VALUE:%s", key, map.toString()));
            this.redisTemplate.opsForHash().putAll(key, map);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-H-MSET】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     */
    public void hmset(String key, Map<String, Object> map, long time) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC H-MSET EXPIRE; KEY:%s, VALUE:%s, TIME:%s", key, map.toString(), String.valueOf(time)));
            this.redisTemplate.opsForHash().putAll(key, map);
            if (time > 0)
                expire(key, time);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-H-MSET-EXPIRE】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     */
    public void hset(String key, String item, Object value) throws GatewayException {
        try {
            String val = objectMapper.writeValueAsString(value);
            logger.info(Strings.lenientFormat("【REDIS】：EXEC H-SET; KEY:%s, SUB-KEY:%s, VALUE:%s", key, item, val));
            this.redisTemplate.opsForHash().put(key, item, val);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-H-SET】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     */
    public void hset(String key, String item, Object value, long time) throws GatewayException {
        try {
            String val = objectMapper.writeValueAsString(value);
            logger.info(Strings.lenientFormat("【REDIS】：EXEC H-SET EXPIRE; KEY:%s, SUB-KEY:%s, VALUE:%s, TIME:%s", key, item, val, String.valueOf(time)));
            this.redisTemplate.opsForHash().put(key, item, val);
            if (time > 0)
                expire(key, time);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-H-SET-EXPIRE】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item) {
        logger.info(Strings.lenientFormat("【REDIS】：EXEC H-DEL; KEY:%s, SUB-KEYS:%s", key, CollectionUtils.arrayToList(item).toString()));
        this.redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 删除hash表中的值
     *
     * @param key 键 不能为null
     */
    public void hdel(String key) {
        logger.info(Strings.lenientFormat("【REDIS】：EXEC H-DEL; KEY:%s", key));
        this.redisTemplate.delete(key);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        logger.info(Strings.lenientFormat("【REDIS】：EXEC H-HASKEY; KEY:%s, SUB-KEYS:%s", key, item));
        return this.redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    public double hincr(String key, String item, double by) {
        logger.info(Strings.lenientFormat("【REDIS】：EXEC H-INCR; KEY:%s, SUB-KEYS:%s, BY:%s", key, item, String.valueOf(by)));
        return this.redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return
     */
    public double hdecr(String key, String item, double by) {
        logger.info(Strings.lenientFormat("【REDIS】：EXEC H-DECR; KEY:%s, SUB-KEYS:%s, BY:%s", key, item, String.valueOf(-by)));
        return this.redisTemplate.opsForHash().increment(key, item, -by);
    }

    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public Set<Object> sGet(String key) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC S-GET; KEY:%s", key));
            return this.redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-S-GET】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     */
    public boolean sHasKey(String key, Object value) throws GatewayException {
        try {
            String val = objectMapper.writeValueAsString(value);
            logger.info(Strings.lenientFormat("【REDIS】：EXEC S-HASKEY; KEY:%s, VALUE:%s", key, val));
            return this.redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-S-HASKEY】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     */
    public void sSet(String key, Object... values) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC S-SET; KEY:%s, VALUES:%s", key, CollectionUtils.arrayToList(values).toString()));
            this.redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-S-SET】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     */
    public void sSetAndTime(String key, long time, Object... values) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC S-SET EXPIRE; KEY:%s, VALUES:%s, TIME:%s", key, CollectionUtils.arrayToList(values).toString(), String.valueOf(time)));
            this.redisTemplate.opsForSet().add(key, values);
            if (time > 0)
                expire(key, time);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-S-SET-EXPIRE】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public long sGetSetSize(String key) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC S-SIZE; KEY:%s", key));
            return this.redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-S-SIZE】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     */
    public void setRemove(String key, Object... values) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC S-REMOVE; KEY:%s, VALUES:%s", key, CollectionUtils.arrayToList(values).toString()));
            this.redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-S-REMOVE】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }
    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public List<Object> lGet(String key, long start, long end) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC L-GET; KEY:%s, START:%s, END:%s", key, String.valueOf(start), String.valueOf(end)));
            return this.redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-L-GET】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public long lGetListSize(String key) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC L-GET-SIZE; KEY:%s", key));
            return this.redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-L-GET-SIZE】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC L-GET-IDX; KEY:%s, INDEX:%s", key, String.valueOf(index)));
            return this.redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-L-GET-IDX】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public void lSet(String key, Object value) throws GatewayException {
        try {
            String val = objectMapper.writeValueAsString(value);
            logger.info(Strings.lenientFormat("【REDIS】：EXEC L-SET; KEY:%s, VALUE:%s", key, val));
            this.redisTemplate.opsForList().rightPush(key, val);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-L-SET】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public void lSet(String key, Object value, long time) throws GatewayException {
        try {
            String val = objectMapper.writeValueAsString(value);
            logger.info(Strings.lenientFormat("【REDIS】：EXEC L-SET EXPIRE; KEY:%s, VALUE:%s, TIME:%s", key, val, String.valueOf(time)));
            this.redisTemplate.opsForList().rightPush(key, val);
            if (time > 0)
                expire(key, time);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-L-SET-EXPIRE】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public void lSet(String key, List<Object> value) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC L-SET; KEY:%s, VALUES:%s", key, CollectionUtils.arrayToList(value).toString()));
            this.redisTemplate.opsForList().rightPushAll(key, value);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-L-SET】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     */
    public void lSet(String key, List<Object> value, long time) throws GatewayException {
        try {
            logger.info(Strings.lenientFormat("【REDIS】：EXEC L-SET EXPIRE; KEY:%s, VALUES:%s, TIME:%s", key, CollectionUtils.arrayToList(value).toString(), String.valueOf(time)));
            this.redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0)
                expire(key, time);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-L-SET-EXPIRE】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     */
    public void lUpdateIndex(String key, long index, Object value) throws GatewayException {
        try {
            String val = objectMapper.writeValueAsString(value);
            logger.info(Strings.lenientFormat("【REDIS】：EXEC L-UPDATE-IDX; KEY:%s, INDEX:%s, VALUE:%s", key, String.valueOf(index), val));
            this.redisTemplate.opsForList().set(key, index, val);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-L-UPDATE-IDX】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     */
    public void lRemove(String key, long count, Object value) throws GatewayException {
        try {
            String val = objectMapper.writeValueAsString(value);
            logger.info(Strings.lenientFormat("【REDIS】：EXEC L-REMOVE; KEY:%s, COUNT:%s, VALUE:%s", key, String.valueOf(count), val));
            this.redisTemplate.opsForList().remove(key, count, val);
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【REDIS-CLIENT-L-REMOVE】：%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.REDIS_ERROR);
        }
    }
}
