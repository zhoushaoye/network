package com.midea.network.http.transformer;




import com.midea.network.http.function.HttpResponseErrorFunction;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;

/**
 * 错误转换Transformer
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class HandleErrTransformer<T> implements ObservableTransformer<T, T> {
    @Override
    public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
        return upstream.onErrorResumeNext(new HttpResponseErrorFunction<T>());
    }
}
