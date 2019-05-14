package com.midea.dolphin.http.cache.core;




import com.midea.dolphin.http.utils.HttpLogUtil;
import com.midea.dolphin.http.utils.HttpUtil;

import java.lang.reflect.Type;

import okio.ByteString;

/**
 * <p>描述：缓存核心管理类</p>
 * <p>
 * 1.采用LruDiskCache<br>
 * 2.对Key进行MD5加密<br>
 * <p>
 * 以后可以扩展 增加内存缓存，但是内存缓存的时间不好控制，暂未实现，后续可以添加》<br>
 * 1.这里笔者给读者留个提醒，ByteString其实已经很强大了，不需要我们自己再去处理加密了，只要善于发现br>
 * 2.这里为设么把MD5改成ByteString呢？其实改不改无所谓，只是想把ByteString这个好东西介绍给大家。(ok)br>
 * 3.在ByteString中有很多好用的方法包括MD5.sha1 base64  encodeUtf8 等等功能。br>
 */
public class CacheCore {

    private LruDiskCache mDiskCache;

    public CacheCore(LruDiskCache disk) {
        this.mDiskCache = HttpUtil.checkNotNull(disk, "disk==null");
    }


    /**
     * 读取
     */
    public synchronized <T> T load(Type type, String key, long time) {
        String cacheKey = ByteString.of(key.getBytes()).md5().hex();
        HttpLogUtil.d("loadCache  key=" + cacheKey);
        if (mDiskCache != null) {
            T result = mDiskCache.load(type, cacheKey, time);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * 保存
     */
    public synchronized <T> boolean save(String key, T value) {
        String cacheKey = ByteString.of(key.getBytes()).md5().hex();
        HttpLogUtil.d("saveCache  key=" + cacheKey);
        return mDiskCache.save(cacheKey, value);
    }

    /**
     * 是否包含
     *
     * @param key
     * @return
     */
    public synchronized boolean containsKey(String key) {
        String cacheKey = ByteString.of(key.getBytes()).md5().hex();
        HttpLogUtil.d("containsCache  key=" + cacheKey);
        if (mDiskCache != null) {
            if (mDiskCache.containsKey(cacheKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public synchronized boolean remove(String key) {
        String cacheKey = ByteString.of(key.getBytes()).md5().hex();
        HttpLogUtil.d("removeCache  key=" + cacheKey);
        if (mDiskCache != null) {
            return mDiskCache.remove(cacheKey);
        }
        return true;
    }

    /**
     * 清空缓存
     */
    public synchronized boolean clear() {
        if (mDiskCache != null) {
            return mDiskCache.clear();
        }
        return false;
    }

    /**
     * 关闭DiskCache
     */
    public synchronized boolean close() {
        if (mDiskCache != null) {
            return mDiskCache.close();
        }
        return false;
    }

}
