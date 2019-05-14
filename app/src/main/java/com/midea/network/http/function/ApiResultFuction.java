package com.midea.network.http.function;


import com.midea.network.http.exception.ServiceException;
import com.midea.network.http.exception.ServiceNullException;
import com.midea.network.http.model.IApiResult;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * ApiResult<T> --> T
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class ApiResultFuction<T> implements Function<IApiResult<T>, T> {

    @Override
    public T apply(@NonNull IApiResult<T> apiResult) {
        if(apiResult != null){
            if (apiResult.isResultSuccess()) {
                return apiResult.getResultData();
            } else {
                throw new ServiceException(apiResult.getReultCode(), apiResult.getResultMessage());
            }
        }else {
            throw new ServiceNullException("Respons is null ...");
        }
    }
}
