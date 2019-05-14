package com.midea.dolphin.base.view;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


import androidx.annotation.LayoutRes;

import com.midea.dolphin.base.widget.StatusToolbar;


/**
 * 状态栏+toolbar+内容提供器，此提供器将会绑定三者的关系，生成新的视图
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public class ContentViewProvider extends LoadViewProvider {

    private StatusToolbar mStatusToolbar;

    @ContentViewBuilder.Mode
    private int mode;

    ContentViewProvider(ContentViewBuilder builder) {
        super(builder);
        this.mStatusToolbar = builder.statusToolbar;
        this.mode = builder.mode;
        if (!builder.hasHeaderBar) {
            mStatusToolbar = null;
        }
        if (mStatusToolbar != null) {
            mStatusToolbar.showToolbar(builder.hasToolbar);
            if (mStatusToolbar.getLayoutParams() == null) {
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                mStatusToolbar.setLayoutParams(params);
            }
        }
    }

    /**
     * 通过绑定内容页与状态栏和Toolbar生成新的视图内容
     *
     * @param layoutRes 内容View layout资源id
     * @return 创造新的视图
     */
    @Override
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
    @Override
    @SuppressLint("RestrictedApi")
    public View createContentView(View contentView) {
        // 创建根布局View
        return createRootView(super.createContentView(contentView));
    }

    /**
     * 创建整个根布局
     *
     * @param contentView 内容View
     * @return 根布局
     */
    private View createRootView(View contentView) {
        if (contentView == null || mStatusToolbar == null) {
            return contentView;
        }
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        ViewGroup.LayoutParams p = contentView.getLayoutParams();
        if (ContentViewBuilder.MODE_OVERLAP == mode) {
            FrameLayout frameLayout = new FrameLayout(mContext);
            frameLayout.setLayoutParams(params);
            if (!(p instanceof FrameLayout.LayoutParams)) {
                p = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
            }
            contentView.setLayoutParams(p);
            frameLayout.addView(contentView);
            frameLayout.addView(mStatusToolbar);
            return frameLayout;
        } else {
            LinearLayout linearLayout = new LinearLayout(mContext);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(params);
            linearLayout.addView(mStatusToolbar);
            if (!(p instanceof LinearLayout.LayoutParams)) {
                p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            }
            ((LinearLayout.LayoutParams) p).weight = 1;
            contentView.setLayoutParams(p);
            linearLayout.addView(contentView);
            return linearLayout;
        }
    }


}
