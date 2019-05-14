package com.midea.dolphin.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * 状态view提供器接口
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public interface StatusViewProvider {

    /**
     * 提供空白view
     *
     * @param context   Context
     * @param container 父View
     * @return Empty状态View
     */
    View getEmptyView(Context context, ViewGroup container);

    /**
     * 提供错误View
     *
     * @param context   Context
     * @param container 父View
     * @return 错误状态View
     */
    View getErrorView(Context context, ViewGroup container);

    /**
     * 提供加载View
     *
     * @param context   Context
     * @param container 父View
     * @return 加载中状态View
     */
    View getLoadingView(Context context, ViewGroup container);

    /**
     * 提供内容View
     *
     * @param context   Context
     * @param container 父View
     * @return 内容View
     */
    View getContentView(Context context, ViewGroup container);


}
