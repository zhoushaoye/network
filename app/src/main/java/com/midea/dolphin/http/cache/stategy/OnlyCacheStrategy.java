
package com.midea.dolphin.http.cache.stategy;
import com.midea.dolphin.http.cache.RxCache;
import com.midea.dolphin.http.cache.model.CacheResult;

import java.lang.reflect.Type;

import io.reactivex.Observable;

/**
 * <p>描述：只读缓存</p>
 */
public final class OnlyCacheStrategy extends BaseStrategy{
    @Override
    public <T> Observable<CacheResult<T>> execute(RxCache rxCache, String key, long time, Observable<T> source, Type type) {
        return loadCache(rxCache,type,key,time,false);
    }
}
