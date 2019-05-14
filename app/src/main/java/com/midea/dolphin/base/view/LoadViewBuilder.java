package com.midea.dolphin.base.view;

import android.content.Context;
import android.view.View;


/**
 * 加载状态的View构造类
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public class LoadViewBuilder<B extends LoadViewBuilder> {

    Context mContext;

    boolean hasLoadStatus = true;

    IStatusView statusView;

    public LoadViewBuilder(Context context) {
        this.mContext = context;
    }

    /**
     * 是否显示加载状态状态，默认值为true
     */
    public B hasLoadStatus(boolean hasLoadStatus) {
        this.hasLoadStatus = hasLoadStatus;
        return (B) this;
    }

    /**
     * 状态View
     */
    public B statusView(IStatusView statusView) {
        this.statusView = statusView;
        if (statusView != null) {
            this.hasLoadStatus = true;
        }
        return (B) this;
    }

    public boolean isHasLoadStatus() {
        return hasLoadStatus;
    }

    public IStatusView getStatusView() {
        return statusView;
    }

    /**
     * 创建内容根布局View
     *
     * @param content 内容View
     * @return 根布局View
     */
    public View build(View content) {
        return new LoadViewProvider(this).createContentView(content);
    }

}
