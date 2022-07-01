package tech.huqi.smartopencvdemo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.blankj.utilcode.util.AppUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import tech.huqi.smartopencvdemo.R;

public class MyService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MsgBinder();
    }

    public class MsgBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

    public static Disposable sDisposable;

    @Override
    public void onCreate() {
        super.onCreate();
        sDisposable = Observable
                .interval(3, TimeUnit.SECONDS)
                //取消任务时取消定时唤醒
                .doOnDispose(() -> {
                    Log.e("aaaaaaaaa", "onCreate取消任务");
                })
                .subscribe(count -> {
                    Log.e("aaaaaaaaa", "onCreate每 3 秒采集一次数据... count = " + count);
                });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        set_services2();
        return START_STICKY;
    }

    public static final String CHANNEL_ID = "XC_ForegroundServiceChannel1";

    private void set_services2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "XC Foreground Service Channel1",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
        Intent notificationIntent = new Intent(AppUtils.getAppPackageName() + ".hs.act.slbapp.MainActivity");
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 123, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 123, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                //设置通知出现在手机顶部的小图标
                .setSmallIcon(R.drawable.icon2_conew1)
                .setContentTitle("应用正在使用卫星定位服务222")
                //设置通知栏中的标题
                .setContentText("点击查看更多222")
                //设置通知栏中的内容
                .setWhen(System.currentTimeMillis())
                //设置通知栏中的大图标
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon2_conew1))
                //将PendingIntent对象传入该方法中，表明点击通知后进入到NotificationActivity.class页面
                .setContentIntent(pendingIntent)
                //点击通知后，通知自动消失
                .setAutoCancel(false)
                //默认选项，根据手机当前的环境来决定通知发出时播放的铃声，震动，以及闪光灯
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                //设置通知的权重
                .setPriority(NotificationCompat.PRIORITY_MAX)

                .build();
        startForeground(1, notification);
//        stopForeground(true);
//        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //取消对任务的订阅
        if (sDisposable != null) {
            sDisposable.dispose();
        }
    }
}
