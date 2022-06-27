package tech.huqi.smartopencvdemo;

import static tech.huqi.smartopencvdemo.utils.PermissionHelper.RequestListener;
import static tech.huqi.smartopencvdemo.utils.PermissionHelper.requestPermissionResult;
import static tech.huqi.smartopencvdemo.utils.PermissionHelper.with;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.AppUtils;

import tech.huqi.smartopencvdemo.db.DatabaseHelper;
import tech.huqi.smartopencvdemo.db.UserInfo;
import tech.huqi.smartopencvdemo.gauss.BgBlurUtils;
import tech.huqi.smartopencvdemo.gauss.DeviceKeyMonitor;
import tech.huqi.smartopencvdemo.opencv.FdActivity;
import tech.huqi.smartopencvdemo.opencv.ViewDataActivity;
import tech.huqi.smartopencvdemo.utils.ToastUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DeviceKeyMonitor.OnKeyListener {
    private DeviceKeyMonitor deviceKeyMonitor;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceKeyMonitor = new DeviceKeyMonitor(this, this);//注册
        bitmap = BgBlurUtils.rsBlur(this, BgBlurUtils.activityShot(this), 20);//初始图片
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);
        Button registerButton = (Button) findViewById(R.id.register);
        Button verifyButton = (Button) findViewById(R.id.verify);
        Button viewDataButton = (Button) findViewById(R.id.view_data);
        Button viewFingerprint = (Button) findViewById(R.id.view_fingerprint);
        Button viewPwdlock = (Button) findViewById(R.id.view_pwdlock);

        registerButton.setOnClickListener(this);
        viewDataButton.setOnClickListener(this);
        verifyButton.setOnClickListener(this);
        viewFingerprint.setOnClickListener(this);
        viewPwdlock.setOnClickListener(this);
        initDatabase();
    }

    // 初始化数据库
    private void initDatabase() {
        DatabaseHelper helper = new DatabaseHelper(this);
        if (helper.query().size() == 0) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.user_defaut);
            String path = helper.saveBitmapToLocal(bitmap);
            UserInfo user = new UserInfo("默认用户", "男", 25, path);
            helper.insert(user);
        }
        helper.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                requestCameraPermission(new RequestListener() {
                    @Override
                    public void onGranted() {
                        Intent intent = new Intent(MainActivity.this,
                                FdActivity.class);
                        intent.putExtra("flag", FdActivity.FLAG_REGISTER);
                        startActivityForResult(intent,
                                FdActivity.FLAG_REGISTER);
                    }

                    @Override
                    public void onDenied() {
                        ToastUtil.showToast(MainActivity.this, "权限拒绝", 0);
                    }
                });
                break;
            case R.id.verify:
                requestCameraPermission(new RequestListener() {
                    @Override
                    public void onGranted() {
                        Intent intent = new Intent(MainActivity.this,
                                FdActivity.class);
                        intent.putExtra("flag", FdActivity.FLAG_VERIFY);
                        startActivityForResult(intent,
                                FdActivity.FLAG_VERIFY);
                    }

                    @Override
                    public void onDenied() {
                        ToastUtil.showToast(MainActivity.this, "权限拒绝", 0);
                    }
                });
                break;
            case R.id.view_data:
                startActivity(new Intent(MainActivity.this, ViewDataActivity.class));
                break;
            case R.id.view_fingerprint:
                startActivity(new Intent(AppUtils.getAppPackageName() + ".hs.act.slbapp.ZhiwenAct1"));
                break;
            case R.id.view_pwdlock:
                startActivity(new Intent(AppUtils.getAppPackageName() + ".hs.act.slbapp.GesturePswMainActivity"));
                break;
            default:
                break;
        }
    }

    private void requestCameraPermission(RequestListener listener) {
        with(MainActivity.this)
                .requestPermission(Manifest.permission.CAMERA)
                .requestCode(1)
                .setListener(listener)
                .request();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FdActivity.FLAG_REGISTER:
                if (resultCode == RESULT_OK) {
                    ToastUtil.showToast(this, "已注册过", 1);
                }
                break;
            case FdActivity.FLAG_VERIFY:
                if (resultCode == RESULT_OK) {
                    int index = data.getIntExtra("USER_ID", -1);
                    DatabaseHelper helper = new DatabaseHelper(this);
                    UserInfo user = helper.query().get(index);
                    helper.close();
                    ToastUtil.showToast(this, "验证通过: " + user.getName(), 1);
                } else {
                    ToastUtil.showToast(this, "验证失败", 1);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        requestPermissionResult(requestCode, grantResults);
    }

    private ViewGroup group;
    private ImageView bgImage;

    @Override
    public void onRecentClick() {
        group = (ViewGroup) getWindow().getDecorView();
        group.removeView(bgImage);
        bgImage = new ImageView(this);
        Rect rect = new Rect();
        group.getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        WindowManager windowManager = getWindowManager();
        //获取屏幕宽和高
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        int height = outMetrics.heightPixels;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height + statusBarHeight);
        bgImage.setLayoutParams(params);
        bgImage.setBackgroundColor(getResources().getColor(R.color.white));
        if (bitmap != null) {
            bgImage.setImageBitmap(bitmap);
            group.addView(bgImage);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (group == null || group.getChildCount() <= 0) {
            return;
        }
        for (int i = 0; i < group.getChildCount(); i++) {
            group.removeView(bgImage);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceKeyMonitor.unregister();
    }
}
