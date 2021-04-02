package com.utopia.recovery.callback;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.utopia.recovery.activity.RecoveryActivity;
import com.utopia.recovery.core.Recovery;
import com.utopia.recovery.core.RecoveryStore;
import com.utopia.reflecct.Reflect;

import androidx.annotation.NonNull;


public class RecoveryActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(@NonNull final Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull final Activity activity) {
        //检验Activity
        if (!RecoveryStore.getInstance().verifyActivity(activity)) {
            return;
        }

        if (activity.getIntent().getBooleanExtra(RecoveryActivity.RECOVERY_MODE_ACTIVE, false)) {
            Reflect.on(Recovery.class).method("registerRecoveryProxy").call(Recovery.getInstance());
        }

        if (RecoveryStore.getInstance().contains(activity))
            return;

        Window window = activity.getWindow();
        if (window != null) {
            View decorView = window.getDecorView();
            decorView.post(() -> {
                RecoveryStore.getInstance().putActivity(activity);
                Object o = activity.getIntent().clone();
                RecoveryStore.getInstance().setIntent((Intent) o);
            });
        }

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        RecoveryStore.getInstance().removeActivity(activity);
    }

}
