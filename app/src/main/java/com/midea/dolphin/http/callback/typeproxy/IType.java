
package com.midea.dolphin.http.callback.typeproxy;

import java.lang.reflect.Type;

/**
 * 泛型接口,用来处理泛型擦除的问题
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public interface IType<T> {

    /**
     * 获取实际业务数据对象
     */
    Type getType();

    /**
     * 顶层数据对象
     */
    Type getRawType();

}
