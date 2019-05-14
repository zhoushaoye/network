package com.midea.dolphin.base.config;

import android.app.Activity;
import android.view.View;

import com.midea.dolphin.base.widget.StatusToolbar;


/**
 *  toolbar 配置接口
 *
 * 此类的作用是定义toolbar 的配置接口
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public interface IStatusToolbarConfig {

    /**
     * 初始化配置
     * @param mActivity
     * @param contentView
     * @param statusToolbar
     */
    void init(Activity mActivity, View contentView, StatusToolbar statusToolbar);

    /**
     * 配置StatusToolbar
     */
    void config();

    /**
     * 获取StatusToolbar
     * @return
     */
    StatusToolbar getStatusToolbar();
}
