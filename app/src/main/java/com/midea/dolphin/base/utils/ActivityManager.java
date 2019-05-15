package com.midea.dolphin.base.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Activity管理器
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public class ActivityManager {

    private List<Activity> activityList = new ArrayList<>();

    private static ActivityManager instance;

    private boolean newTask;

    public static ActivityManager getInstance() {
        if (instance == null) {
            instance = new ActivityManager();
        }
        return instance;
    }

    /**
     * 仅仅从列表中移除某个Activity
     */
    public void popActivity(Activity activity) {
        if (activity != null) {
            activityList.remove(activity);
        }
    }

    /**
     * 推入某个Activity
     */
    public void pushActivity(Activity activity) {
        if (newTask) {
            activityList = new ArrayList<>();
            newTask = false;
        }
        activityList.add(activity);
    }

    /**
     * 获得当前栈顶Activity
     */
    public Activity getTopActivity() {
        if (!activityList.isEmpty()) {
            return activityList.get(activityList.size() - 1);
        }
        return null;
    }

    /**
     * 退出栈中Activity回到某个activity，如果有多个activityClass对象，则只回到顶部第一个activityClass的Activity
     *
     * @param activityClass 需要回调的Activity class对象
     */
    public void popToActivity(Class<? extends Activity> activityClass) {
        if (!activityList.isEmpty() && activityClass != null) {
            for (int i = activityList.size() - 1; i >= 0; i--) {
                Activity activity = activityList.get(i);
                if (!activity.getClass().equals(activityClass)) {
                    activity.finish();
                } else {
                    break;
                }
            }
        }
    }

    /**
     * 退出所有Activity
     */
    public void popAllActivity() {
        if (activityList.isEmpty()) {
            return;
        }
        final List<Activity> oldActivities = new ArrayList<>(activityList);
        newTask = true;
        // post finish是由于有些机型不post就会直接关闭了所有的activity
        AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                for (Activity activity : oldActivities) {
                    activity.finish();
                    activityList.remove(activity);
                }
            }
        });
    }

    /**
     * 获取activity的数量
     *
     * @return 正在运行的activity数量
     */
    public int getActivitySize() {
        return activityList == null ? 0 : activityList.size();
    }
}

