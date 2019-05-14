package com.midea.dolphin.base.config;

/**
 * 基本的页面配置类
 */
public class PageConfig {

    /**
     * 是否有工具栏 默认有工具栏
     */
    private boolean toolbar = true;

    /**
     * 是否有页面加载状态的view，比如正在加载中，加载完成，加载失败
     * 默认有页面加载状态view
     */
    private boolean pageStatusView = true;

    /**
     * 配置statusToolbar的类
     */
    private IStatusToolbarConfig mIStatusToolbarConfig;

    public boolean isToolbar() {
        return toolbar;
    }

    public void setToolbar(boolean toolbar) {
        this.toolbar = toolbar;
    }

    public boolean isPageStatusView() {
        return pageStatusView;
    }

    public void setPageStatusView(boolean pageStatusView) {
        this.pageStatusView = pageStatusView;
    }

    public IStatusToolbarConfig getStatusToolbarConfig() {
        return mIStatusToolbarConfig;
    }

    public void setStatusToolbarConfig(IStatusToolbarConfig statusToolbarConfig) {
        mIStatusToolbarConfig = statusToolbarConfig;
    }

    /**
     * 判断statusToolbarConfig是否为空
     * @return
     */
    public boolean statusToolbarConfigIsNull() {
        return mIStatusToolbarConfig == null;
    }
}
