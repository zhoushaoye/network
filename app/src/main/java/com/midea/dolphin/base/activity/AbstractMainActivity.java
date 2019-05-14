package com.midea.dolphin.base.activity;

import android.view.View;
import android.view.ViewStub;

import com.midea.dolphin.R;
import com.midea.dolphin.base.PageConfig;

/**
 * 基础主界面的抽象类
 * 提供基础的主界面管理
 * 支持的样式为，下面4个tab
 * 上面显示body内容
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public abstract class AbstractMainActivity extends BaseAppActivity {

    /**
     * tab 容器，存放按钮
     */
    private ViewStub mTabBarStub;

    /**
     * 存放body的容器
     */
    private ViewStub mBodyContainerStub;

    /**
     * 包含body 和tabbar的父view
     */
    protected View mRootView;

    /**
     * body view
     */
    protected View mBodyView;

    /**
     * body下面的tab栏view
     */
    protected View mTabBarView;
    /**
     * 关闭toolbar
     * @param pageConfig
     */
    @Override
    public void updatePageConfig(PageConfig pageConfig) {
        pageConfig.setPageStatusView(false);
        pageConfig.setToolbar(false);
    }

    @Override
    protected void initView(View contentView) {
        mRootView = findViewById(R.id.abstract_main_root);
        initBodyView(contentView);
        initTabbarView(contentView);
        initMainView(contentView);
    }

    /**
     * 该方法留给子类进行 view 的初始化等其他处理
     * @param contentView
     */
    protected abstract void initMainView(View contentView);

    /**
     * 加载body容器
     * @param contentView
     */
    protected void initBodyView(View contentView) {
        mBodyContainerStub = contentView.findViewById(R.id.body_container);
        mBodyContainerStub.setLayoutResource(getBodyView());
        if (mBodyView == null) {
            mBodyView = mBodyContainerStub.inflate();
        }
    }

    /**
     * 加载tabbar 容器
     * @param contentView
     */
    protected void initTabbarView(View contentView) {
        mTabBarStub = contentView.findViewById(R.id.tabbar_group);
        mTabBarStub.setLayoutResource(getTabBarView());
        if (mTabBarView == null) {
            mTabBarView = mTabBarStub.inflate();
        }
    }

    /**
     * 显示body的View资源文件id
     * @return
     */
    protected abstract int getBodyView();

    /**
     * 显示下面tabbar的资源文件id
     * @return
     */
    protected abstract int getTabBarView();

    @Override
    protected int getLayout() {
        return R.layout.abstract_main_activity_layout;
    }


}
