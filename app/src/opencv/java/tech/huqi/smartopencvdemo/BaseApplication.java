package tech.huqi.smartopencvdemo;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.xdandroid.hellodaemon.DaemonEnv;

import java.util.Collections;

import tech.huqi.smartopencvdemo.opencv.FdActivity;
import tech.huqi.smartopencvdemo.service.TraceServiceImpl;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutInfoCompat info = new ShortcutInfoCompat.Builder(this, "test_0")
                    .setShortLabel(getString(R.string.app_test_0))
                    .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher))
                    //跳转的目标，定义Activity
                    .setIntent(new Intent(Intent.ACTION_MAIN, null, this, FdActivity.class))
                    .build();
            //执行添加操作
            ShortcutManagerCompat.addDynamicShortcuts(this, Collections.singletonList(info));

            ShortcutInfoCompat info1 = new ShortcutInfoCompat.Builder(this, "test_1")
                    .setShortLabel(getString(R.string.app_test_1))
                    .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher))
                    //跳转的目标，定义Activity
                    .setIntent(new Intent(Intent.ACTION_MAIN, null, this, MainActivity.class))
                    .build();
            //执行添加操作
            ShortcutManagerCompat.addDynamicShortcuts(this, Collections.singletonList(info1));
        }
        /**保活任务*/
        showService();
    }

    private void showService() {
        //需要在 Application 的 onCreate() 中调用一次 DaemonEnv.initialize()
        DaemonEnv.initialize(this, TraceServiceImpl.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        TraceServiceImpl.sShouldStopService = false;
        DaemonEnv.startServiceMayBind(TraceServiceImpl.class);
    }
}
