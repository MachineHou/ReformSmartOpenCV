package tech.huqi.smartopencvdemo;

import static tech.huqi.smartopencvdemo.PermissionHelper.RequestListener;
import static tech.huqi.smartopencvdemo.PermissionHelper.requestPermissionResult;
import static tech.huqi.smartopencvdemo.PermissionHelper.with;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import tech.huqi.smartopencvdemo.db.DatabaseHelper;
import tech.huqi.smartopencvdemo.db.UserInfo;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button registerButton = (Button) findViewById(R.id.register);
        Button verifyButton = (Button) findViewById(R.id.verify);
        Button viewDataButton = (Button) findViewById(R.id.view_data);
        Button viewCleardata = (Button) findViewById(R.id.view_cleardata);

        registerButton.setOnClickListener(this);
        viewDataButton.setOnClickListener(this);
        verifyButton.setOnClickListener(this);
        viewCleardata.setOnClickListener(this);
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
            case R.id.view_cleardata:
                DatabaseHelper helper = new DatabaseHelper(MainActivity.this);
                helper.close();
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
}
