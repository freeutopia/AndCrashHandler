package com.utopia.demo;

import android.app.Application;

import com.utopia.recovery.core.Recovery;
import com.utopia.recovery.core.RecoveryConfig;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //异常页面处理
        RecoveryConfig recoveryConfig = new RecoveryConfig.Builder()
                .recoverInBackground(false)
                .recoverStack(true)
                .mainPageActivity(MainActivity.class)
                .recoverEnabled(true)
                .silent(false)
                .build();
        Recovery.debug(true);
        Recovery.init(this,recoveryConfig);

    }
}
