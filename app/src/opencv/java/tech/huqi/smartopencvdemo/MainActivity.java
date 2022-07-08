package tech.huqi.smartopencvdemo;

import static tech.huqi.smartopencvdemo.utils.PermissionHelper.RequestListener;
import static tech.huqi.smartopencvdemo.utils.PermissionHelper.requestPermissionResult;
import static tech.huqi.smartopencvdemo.utils.PermissionHelper.with;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutManagerCompat;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.Collections;
import java.util.List;

import tech.huqi.smartopencvdemo.db.DatabaseHelper;
import tech.huqi.smartopencvdemo.db.UserInfo;
import tech.huqi.smartopencvdemo.gauss.BgBlurUtils;
import tech.huqi.smartopencvdemo.gauss.DeviceKeyMonitor;
import tech.huqi.smartopencvdemo.opencv.FdActivity;
import tech.huqi.smartopencvdemo.opencv.ViewDataActivity;
import tech.huqi.smartopencvdemo.utils.RuntimeSettingPage;
import tech.huqi.smartopencvdemo.utils.ShortcutManage;
import tech.huqi.smartopencvdemo.utils.ToastUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DeviceKeyMonitor.OnKeyListener {
    private DeviceKeyMonitor deviceKeyMonitor;
    private Bitmap bitmap;
    private Button appWindowShortcuts;

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
        Button viewSequel = (Button) findViewById(R.id.view_sequel);
        Button viewNfc = (Button) findViewById(R.id.view_nfc);
        Button viewOcr = (Button) findViewById(R.id.view_ocr);
        Button appRemove = (Button) findViewById(R.id.app_remove);
        Button viewOta = (Button) findViewById(R.id.view_ota);
        Button viewService = (Button) findViewById(R.id.view_service);
        appWindowShortcuts = (Button) findViewById(R.id.app_window_shortcuts);

        registerButton.setOnClickListener(this);
        viewDataButton.setOnClickListener(this);
        verifyButton.setOnClickListener(this);
        viewFingerprint.setOnClickListener(this);
        viewPwdlock.setOnClickListener(this);
        viewSequel.setOnClickListener(this);
        viewNfc.setOnClickListener(this);
        viewNfc.setOnClickListener(this);
        viewOcr.setOnClickListener(this);
        appRemove.setOnClickListener(this);
        viewOta.setOnClickListener(this);
        viewService.setOnClickListener(this);
        appWindowShortcuts.setOnClickListener(this);
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
            case R.id.view_sequel:
                startActivity(new Intent(AppUtils.getAppPackageName() + ".hs.act.slbapp.DownloaderActivity"));
                break;
            case R.id.view_nfc:
                ToastUtils.showShort("开发中....");
//                startActivity(new Intent(AppUtils.getAppPackageName() + ".hs.act.slbapp.NfcActivity"));
                break;
            case R.id.view_ocr:
                startActivity(new Intent(AppUtils.getAppPackageName() + ".hs.act.slbapp.ScannerAct2"));
                break;
            case R.id.app_remove:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutManagerCompat.removeDynamicShortcuts(this, Collections.singletonList("test_0"));//唯一标识id);
                }
                break;
            case R.id.view_ota:
                startActivity(new Intent(AppUtils.getAppPackageName() + ".hs.act.slbapp.OTAAct"));
                break;
            case R.id.view_service:
                startActivity(new Intent(AppUtils.getAppPackageName() + ".hs.act.slbapp.ServiceActivity"));
                break;
            case R.id.app_window_shortcuts:
                if ("已同意".equals(getPermission())|| "未知".equals(getPermission())) {
                    if (ShortcutManage.shortcutHigh(MainActivity.this, "OCR识别") || ShortcutManage.hasShortcutLow(MainActivity.this, "OCR识别")) {
                        ToastUtils.showShort("已经有桌面快捷方式了");
                        return;
                    }
                    String[] permissions = {Manifest.permission.INSTALL_SHORTCUT, Manifest.permission.READ_SYNC_SETTINGS};
                    if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_REQUEST);
                    }
                    ShortcutManage.addShortcut(getApplicationContext(), R.drawable.user_defaut, AppUtils.getAppPackageName() + ".hs.act.slbapp.ScannerAct2", "OCR识别");
                }else{
                    RuntimeSettingPage runtimeSettingPage = new RuntimeSettingPage(MainActivity.this);
                    runtimeSettingPage.start();
                    return;
                }

                break;
            default:
                break;
        }
    }

    private static final int PERMISSION_REQUEST = 1;


    private String getPermission() {
        int check = ShortcutManage.check(this);
        String state = "未知";
        switch (check) {
            case ShortcutManage.PERMISSION_DENIED:
                state = "已禁止";
                break;
            case ShortcutManage.PERMISSION_GRANTED:
                state = "已同意";
                break;
            case ShortcutManage.PERMISSION_ASK:
                state = "询问";
                break;
            case ShortcutManage.PERMISSION_UNKNOWN:
                state = "未知";
                break;
        }
        return state;
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
        if (requestCode == 0) {
            try {
                appWindowShortcuts.setText(getString(R.string.app_window_shortcuts) + getPermission());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        appWindowShortcuts.setText(getString(R.string.app_window_shortcuts) + getPermission());
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
