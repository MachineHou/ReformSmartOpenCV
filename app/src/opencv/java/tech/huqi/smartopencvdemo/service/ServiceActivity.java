package tech.huqi.smartopencvdemo.service;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.xdandroid.hellodaemon.DaemonEnv;
import com.xdandroid.hellodaemon.IntentWrapper;

import tech.huqi.smartopencvdemo.R;

public class ServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_service);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                TraceServiceImpl.sShouldStopService = false;
                DaemonEnv.startServiceMayBind(TraceServiceImpl.class);
                break;
            case R.id.btn_white:
                IntentWrapper.whiteListMatters(this, "轨迹跟踪服务的持续运行");
                break;
            case R.id.btn_stop:
                TraceServiceImpl.stopService();
                break;
        }
    }

    //防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀
//    @Override
//    public void onBackPressed() {
//        IntentWrapper.onBackPressed(this);
//    }
}
