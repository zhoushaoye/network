package com.midea.dolphin.base.view;

import android.content.Context;
import android.view.View;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import com.midea.dolphin.base.widget.StatusToolbar;


/**
 * 内容View基础builder
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public class ContentViewBuilder<B extends ContentViewBuilder> extends LoadViewBuilder<B> {

    /**
     * 如果为该模式，toolbar重叠覆盖在内容view上，使用FrameLayout
     */
    public static final int MODE_OVERLAP = 1;

    /**
     * 如果为该模式，toolbar在内容view之上，使用LinearLayout
     */
    public static final int MODE_ABOVE = 2;

    @IntDef({MODE_OVERLAP, MODE_ABOVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {

    }

    StatusToolbar statusToolbar;

    boolean hasToolbar = true;

    boolean hasHeaderBar = true;

    @Mode
    int mode = MODE_ABOVE;

    public ContentViewBuilder(Context context) {
        super(context);
    }

    /**
     * 状态栏+toolbar
     *
     * @param statusToolbar StatusToolbar
     */
    public B statusToolbar(StatusToolbar statusToolbar) {
        this.statusToolbar = statusToolbar;
        if (statusToolbar != null) {
            hasHeaderBar = true;
        }
        return (B) this;
    }

    /**
     * StatusToolbarProvider与内容view的位置关系模式
     *
     * @param mode 对应{@link #MODE_ABOVE}/{@link #MODE_OVERLAP}
     */
    public B mode(@Mode int mode) {
        this.mode = mode;
        return (B) this;
    }

    /**
     * 是否默认显示Toolbar，默认值为true
     *
     * @param hasToolbar 是否默认显示Toolbar
     */
    public B hasToolbar(boolean hasToolbar) {
        this.hasToolbar = hasToolbar;
        return (B) this;
    }

    /**
     * 是否有整个头部bar（status + toolbar)
     */
    public B hasHeaderBar(boolean hasHeaderBar) {
        this.hasHeaderBar = hasHeaderBar;
        return (B) this;
    }

    public boolean isHasHeaderBar() {
        return hasHeaderBar;
    }

    public boolean isHasToolbar() {
        return hasToolbar;
    }

    public StatusToolbar getStatusToolbar() {
        return statusToolbar;
    }

    /**
     * 创建内容根布局View
     *
     * @param content 内容View
     * @return 根布局View
     */
    @Override
    public View build(View content) {
        return new ContentViewProvider(this).createContentView(content);
    }

}
