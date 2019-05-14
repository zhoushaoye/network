
package com.midea.dolphin.http.function;


import com.midea.dolphin.http.cache.model.CacheResult;
import com.midea.dolphin.http.model.IApiResult;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;


/**
 * 缓存结果转换
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class CacheResultFunction<T> implements Function<CacheResult<T>, T> {
    @Override
    public T apply(@NonNull CacheResult<T> cacheResult) {
        T data = cacheResult.data;
        //主要是为了给返回数据添加一个是否来自于缓存的标识
        if(data instanceof IApiResult){
            ((IApiResult)data).isFromCache(cacheResult.isFromCache);
        }
        return data;
    }
}
