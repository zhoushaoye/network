package com.midea.dolphin.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.midea.dolphin.R;


/**
 * 默认状态View提供器
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public class DefaultStatusViewProvider implements StatusViewProvider {

    @Override
    public View getEmptyView(Context context, ViewGroup container) {
        return LayoutInflater.from(context).inflate(R.layout.status_empty_view, container, false);
    }

    @Override
    public View getErrorView(Context context, ViewGroup container) {
        return LayoutInflater.from(context).inflate(R.layout.status_error_view, container, false);
    }

    @Override
    public View getLoadingView(Context context, ViewGroup container) {
        return LayoutInflater.from(context).inflate(R.layout.status_loading_view, container, false);
    }

    @Override
    public View getContentView(Context context, ViewGroup container) {
        return null;
    }
}
