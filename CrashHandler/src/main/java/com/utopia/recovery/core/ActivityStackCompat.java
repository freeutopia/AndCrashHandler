package com.utopia.recovery.core;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import java.util.List;

import androidx.annotation.RequiresApi;

class ActivityStackCompat {

    /**
     * Android 5.0以后获取栈顶Activity
     */
    @RequiresApi(21)
    private static ActivityManager.AppTask getTopTaskAfterL(ActivityManager ams) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return ams.getAppTasks().get(0);
        return null;
    }

    private static ActivityManager.RunningTaskInfo getTopTaskBeforeL(ActivityManager ams) {
        List<ActivityManager.RunningTaskInfo> tasks = null;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                tasks = ams.getRunningTasks(1);
        } catch (Exception e) {
            return null;
        }
        if (tasks == null || tasks.size() == 0)
            return null;
        return tasks.get(0);
    }

    /**
     * 获取任务栈中,Activity的个数
     */
    public static int getActivityCount(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager.AppTask appTask = getTopTaskAfterL(activityManager);
            if (appTask == null)
                return 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return appTask.getTaskInfo().numActivities;
            } else {
                return RecoveryStore.getInstance().getRunningActivityCount();
            }
        } else {
            ActivityManager.RunningTaskInfo taskInfo = getTopTaskBeforeL(activityManager);
            if (taskInfo == null)
                return 0;
            return taskInfo.numActivities;
        }
    }

    /**
     * The component of the first activity in the task, can be considered the "application" of this
     */
    public static String getBaseActivityName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager.AppTask appTask = getTopTaskAfterL(activityManager);
            if (appTask == null)
                return null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return appTask.getTaskInfo().baseActivity.getClassName();
            } else {
                ComponentName componentName = RecoveryStore.getInstance().getBaseActivity();
                return componentName == null ? null : componentName.getClassName();
            }
        } else {
            ActivityManager.RunningTaskInfo taskInfo = getTopTaskBeforeL(activityManager);
            if (taskInfo == null)
                return null;
            return taskInfo.baseActivity.getClassName();
        }
    }

}
