package com.midea.dolphin.base.view;

import android.view.View;

import com.midea.dolphin.base.widget.StatusViewProvider;

/**
 * 状态view接口
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public interface IStatusView {

    int STATUS_LOADING = 1;

    int STATUS_EMPTY = 2;

    int STATUS_ERROR = 3;

    int STATUS_CONTENT = 4;

    /**
     * 显示空白view
     */
    void showEmpty();

    /**
     * 显示加载中View
     */
    void showLoading();

    /**
     * 显示失败View
     */
    void showError();

    /**
     * 显示内容View
     */
    void showContent();

    /**
     * 获取状态
     *
     * @return 当前状态
     */
    int getStatus();

    /**
     * 状态View
     * @return 状态View
     */
    View getView();

    /**
     * 状态View提供器
     * @param provider StatusViewProvider
     */
    void setStatusViewProvider(StatusViewProvider provider);

}
