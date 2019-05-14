
package com.midea.network.http.cache.stategy;




import com.midea.network.http.cache.RxCache;
import com.midea.network.http.cache.model.CacheResult;

import java.lang.reflect.Type;

import io.reactivex.Observable;

/**
 * <p>描述：只请求网络</p>
 */
public final class OnlyRemoteStrategy extends BaseStrategy{
    @Override
    public <T> Observable<CacheResult<T>> execute(RxCache rxCache, String key, long time, Observable<T> source, Type type) {
        return loadRemoteAndSaveSync(rxCache,key, source,false);
    }
}
