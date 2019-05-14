package com.midea.dolphin.base.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.LayoutRes;

import com.midea.dolphin.base.IStatusView;


/**
 * 加载状态的内容View提供器
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public class LoadViewProvider {

    private IStatusView mStatusView;

    protected Context mContext;

    LoadViewProvider(LoadViewBuilder builder) {
        mContext = builder.mContext;
        if (!builder.isHasLoadStatus()) {
            mStatusView = null;
        } else {
            mStatusView = builder.statusView;
        }
    }


    /**
     * 通过绑定内容页与状态栏和Toolbar生成新的视图内容
     *
     * @param layoutRes 内容View layout资源id
     * @return 创造新的视图
     */
    public View createContentView(@LayoutRes int layoutRes) {
        View contentView = LayoutInflater.from(mContext).inflate(layoutRes, null);
        return createContentView(contentView);
    }

    /**
     * 通过绑定内容页与状态栏和Toolbar生成新的视图内容
     *
     * @param contentView 内容View
     * @return 创造新的视图
     */
    public View createContentView(View contentView) {
        View statusContent;
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        if (mStatusView != null && (statusContent = mStatusView.getView()) != null) {
            // 创建根布局View
            statusContent.setLayoutParams(params);
            return statusContent;
        } else {
            contentView.setLayoutParams(params);
            return contentView;
        }
    }
}
