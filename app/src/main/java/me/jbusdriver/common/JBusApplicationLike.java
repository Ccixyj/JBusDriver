package me.jbusdriver.common;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.multidex.MultiDex;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.tinker.entry.DefaultApplicationLike;

import java.io.File;

import me.jbusdriver.base.JBusManager;

@SuppressWarnings("unused")
public class JBusApplicationLike extends DefaultApplicationLike {

    public static final String TAG = "Tinker.JBusApplicationLike";

    public JBusApplicationLike(Application application, int tinkerFlags,
                               boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime,
                               long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (getApplication() instanceof AppContext) {
            AppContextKt.setJBus(((AppContext) getApplication()));
        }
        JBusManager.INSTANCE.setContext(getApplication());

        // 这里实现SDK初始化，appId替换成你的在Bugly平台申请的appId
        // 调试时，将第三个参数改为true
        boolean isDebug = false;
        try {
            isDebug = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                    getApplication().getPackageName()
                    + File.separator + "debug"
                    + File.separator + "buuugly"

            ).exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        CrashReport.setIsDevelopmentDevice(getApplication(), isDebug);
        Bugly.init(getApplication(), "26dd49f158", isDebug);
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        // 安装tinker
        // TinkerManager.installTinker(this); 替换成下面Bugly提供的方法
        Beta.installTinker(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallback(Application.ActivityLifecycleCallbacks callbacks) {
        getApplication().registerActivityLifecycleCallbacks(callbacks);
    }

}