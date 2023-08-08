package com.mhhy.util;

import com.mhhy.SpringContextHolder;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis工具类
 */
public class RedisUtil {

    private final static StringRedisTemplate stringRedisTemplate = SpringContextHolder.getBean("stringRedisTemplate");

    /**
     * 匹配key
     */
    public static Set<String> keys(String pattern) {
        return stringRedisTemplate.keys(pattern);
    }

    /**
     * 删除一个key
     */
    public static void del(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 批量删除key
     */
    public static void delByPattern(String pattern) {
        Set<String> keySet = keys(pattern);
        stringRedisTemplate.delete(keySet);
    }

    /**
     * 设置过期时间，单位为秒
     */
    public static boolean expire(String key, long seconds) {
        return stringRedisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    /**
     * 获取自动过期时间
     */
    public static long ttl(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 移除过期时间
     */
    public static boolean persist(String key) {
        return stringRedisTemplate.persist(key);
    }

    /////// String 操作

    /**
     * 给key赋值
     */
    public static void set(String key, String value) {
        ValueOperations<String, String> op = stringRedisTemplate.opsForValue();
        op.set(key, value);
    }

    /**
     * 给key赋值，并设置过期时间，单位为秒
     */
    public static void setEx(String key, String value, long seconds) {
        set(key, value);
        expire(key, seconds);
    }

    public static boolean setNewNx(String key,String value,long seconds ){
        ValueOperations<String, String> op = stringRedisTemplate.opsForValue();
        return op.setIfAbsent(key, value,Duration.ofSeconds(seconds));
    }
    public static boolean setNx(String key, String value) {
        ValueOperations<String, String> op = stringRedisTemplate.opsForValue();
        return op.setIfAbsent(key, value);
    }

    public static void getLock(String key){
        getLock(key, 2L);
    }

    public static void getLock(String key, long expireSecond) {
        if (expireSecond > 10) {
            expireSecond = 2;
        }
        String md5Key = "vexLock:" + key + ":" + Md5Util.md5(key);
        String value = "0";
        while (!setNx(md5Key, value)) {
            sleep(20);
        }
        expire(md5Key, expireSecond);
    }

    public static void unLock(String key) {
        String md5Key = "vexLock:" + key + ":" + Md5Util.md5(key);
        del(md5Key);
    }

    public static void sleep(int timeMills) {
        try {
            Thread.sleep(timeMills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将key的值加num
     */
    public static long incrBy(String key, long num) {
        ValueOperations<String, String> op = stringRedisTemplate.opsForValue();
        return op.increment(key, num);
    }

    /**
     * 获取key的值
     */
    public static String get(String key) {
        ValueOperations<String, String> op = stringRedisTemplate.opsForValue();
        return op.get(key);
    }

    /////// list操作

    /**
     * 插入到表头
     */
    public static void lPush(String key, String... values) {
        ListOperations<String, String> listOp = stringRedisTemplate.opsForList();
        listOp.leftPushAll(key, values);
    }

    /**
     * 移除第一个
     */
    public static String rPop(String key) {
        ListOperations<String, String> listOp = stringRedisTemplate.opsForList();
        return listOp.rightPop(key);
    }

    public static int lLen(String key) {
        ListOperations<String, String> opsForList = stringRedisTemplate.opsForList();
        return opsForList.size(key).intValue();
    }

    /**
     * 获取list所有
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static List<String> lRange(String key, int start, int end) {
        ListOperations<String, String> opsForList = stringRedisTemplate.opsForList();
        return opsForList.range(key, start, end);
    }

    /////// hash

	/*
	 * public static void hset(String key,String hashKey,String value){
	 * HashOperations<String,String,String> opsForHash =
	 * stringRedisTemplate.opsForHash(); opsForHash.put(key, hashKey, value); }
	 */
    /////// set
    /////// sorted set

    /**
     * 存放list
     * @param key
     * @param list
     */
    public static void setList(String key, List<String> list){
        ListOperations<String, String> opsForList = stringRedisTemplate.opsForList();
        opsForList.leftPushAll(key, list);
    }

    /**
     * HashGet
     * @param key 键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public static String hGet(String key, String item) {
        HashOperations<String, String, String> mapOp = stringRedisTemplate.opsForHash();
        return mapOp.get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     * @param key 键
     * @return 对应的多个键值
     */
    public static Map<String, String> hmGet(String key) {
        HashOperations<String, String, String> mapOp = stringRedisTemplate.opsForHash();
        return mapOp.entries(key);
    }

    /**
     * HashSet
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public static boolean hmSet(String key, Map<String,String> map){
        try {
            HashOperations<String, String, String> mapOp = stringRedisTemplate.opsForHash();
            mapOp.putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     * @param key 键
     * @param map 对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public static boolean hmset(String key, Map<String, String> map, long time) {
        try {
            HashOperations<String, String, String> mapOp = stringRedisTemplate.opsForHash();
            mapOp.putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @return true 成功 false失败
     */
    public static boolean hset(String key, String item, String value) {
        try {
            HashOperations<String, String, String> mapOp = stringRedisTemplate.opsForHash();
            mapOp.put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @param time 时间(秒)  注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public static boolean hset(String key,String item,Object value,long time) {
        try {
            HashOperations<String, Object, Object> mapOp = stringRedisTemplate.opsForHash();
            mapOp.put(key, item, value);
            if(time>0){
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     * @param key 键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public static void hdel(String key, Object... item){
        HashOperations<String, Object, Object> mapOp = stringRedisTemplate.opsForHash();

        mapOp.delete(key,item);
    }

    /**
     * 判断hash表中是否有该项的值
     * @param key 键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public static boolean hHasKey(String key, String item){
        HashOperations<String, Object, Object> mapOp = stringRedisTemplate.opsForHash();

        return mapOp.hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     * @param key 键
     * @param item 项
     * @param by 要增加几(大于0)
     * @return
     */
    public static double hincr(String key, String item,double by){
        HashOperations<String, Object, Object> mapOp = stringRedisTemplate.opsForHash();

        return mapOp.increment(key, item, by);
    }

    /**
     * hash递减
     * @param key 键
     * @param item 项
     * @param by 要减少记(小于0)
     * @return
     */
    public static double hdecr(String key, String item,double by){
        HashOperations<String, Object, Object> mapOp = stringRedisTemplate.opsForHash();

        return mapOp.increment(key, item,-by);
    }

    /**
     * set中增加元素，支持一次增加多个元素，逗号分隔即可，结果返回添加的个数
     *
     * @param key
     * @param value
     * @return
     */
    public static Long addSet(String key, String... value) {
        Long size = null;
        try {
            size = stringRedisTemplate.opsForSet().add(key, value);
        } catch (Exception e) {
            //log.error("[RedisUtils.addSet] [error]", e);
            return size;
        }
        return size;
    }
    /**
     * set中移除指定元素
     *
     * @param key
     * @param value
     * @return
     */
    public static Long removeSet(String key, Object value) {
        Long size = null;
        try {
            size = stringRedisTemplate.opsForSet().remove(key, value);
        } catch (Exception e) {
            //log.error("[RedisUtils.removeSet] [error]", e);
            return size;
        }
        return size;
    }

    /**
     * 获取key下的所有元素
     *
     * @param key
     * @return
     */
    public static Set<String> members(String key) {
        Set<String> result = null;
        try {
            result = stringRedisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            //log.error("[RedisUtils.members] [error]", e);
        }
        return result;
    }




}
