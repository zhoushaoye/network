package com.midea.dolphin.base.mvp;



import com.trello.rxlifecycle3.LifecycleTransformer;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;


/**
 * MVP结构基础View接口
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public interface IView {

    /**
     * 得到View的生命周期管理器
     *
     * @return 生命周期管理器
     */
    Lifecycle getLifecycle();


    /**
     * 绑定生命周期，直到销毁时解绑
     *
     * @param <T> Rx泛型类型
     * @return 生命周期转换器
     */
    <T> LifecycleTransformer<T> bindUntilDestroy();

    /**
     * 绑定生命周期，直到某个生命周期时解绑
     *
     * @param event 解绑时机的生命周期
     * @param <T>   Rx泛型类型
     * @return 生命周期转换器
     */
    <T> LifecycleTransformer<T> bindUntilEvent(@NonNull Lifecycle.Event event);

    /**
     * 显示加载状态View
     */
    void showLoadingView();

    /**
     * 显示内容View
     */
    void showContentView();

    /**
     * 显示加载错误View(网络失败等)
     */
    void showErrorView();

    /**
     * 显示空白View(无数据)
     */
    void showEmptyView();

}
