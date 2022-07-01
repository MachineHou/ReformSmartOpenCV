package tech.huqi.smartopencvdemo.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.Utils;
import com.xdandroid.hellodaemon.DaemonEnv;
import com.xdandroid.hellodaemon.IntentWrapper;

import tech.huqi.smartopencvdemo.BaseApplication;
import tech.huqi.smartopencvdemo.R;

public class ServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_service);
    }
    public void showService() {
        //需要在 Application 的 onCreate() 中调用一次 DaemonEnv.initialize()
        DaemonEnv.initialize(ServiceActivity.this, TraceServiceImpl.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        TraceServiceImpl.sShouldStopService = false;
        DaemonEnv.startServiceMayBind(TraceServiceImpl.class);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                showService();
//                TraceServiceImpl.sShouldStopService = false;
//                DaemonEnv.startServiceMayBind(TraceServiceImpl.class);
                break;
            case R.id.btn_white:
                IntentWrapper.whiteListMatters(this, "轨迹跟踪服务的持续运行");
                break;
            case R.id.btn_stop:
                TraceServiceImpl.stopService();
                break;
            case R.id.btn_service:
                initForgetOrBack();
                break;
        }
    }

    private void initForgetOrBack() {
        if (!ServiceUtils.isServiceRunning(MyService.class)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(ServiceActivity.this, MyService.class));
            } else {
                startService(new Intent(ServiceActivity.this, MyService.class));
            }
        }
    }

    //防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀
//    @Override
//    public void onBackPressed() {
//        IntentWrapper.onBackPressed(this);
//    }
}
