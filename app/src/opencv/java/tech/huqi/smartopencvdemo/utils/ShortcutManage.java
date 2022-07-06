package tech.huqi.smartopencvdemo.utils;

import static android.content.Context.SHORTCUT_SERVICE;
import static tech.huqi.smartopencvdemo.utils.ShortcutPermissionChecker.checkOnEMUI;
import static tech.huqi.smartopencvdemo.utils.ShortcutPermissionChecker.checkOnMIUI;
import static tech.huqi.smartopencvdemo.utils.ShortcutPermissionChecker.checkOnOPPO;
import static tech.huqi.smartopencvdemo.utils.ShortcutPermissionChecker.checkOnVIVO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.IntDef;

import com.blankj.utilcode.util.ToastUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.regex.Pattern;

public class ShortcutManage {

    private static final String TAG = "ShortcutPermission";

    @IntDef(value = {
            PERMISSION_GRANTED,
            PERMISSION_DENIED,
            PERMISSION_ASK,
            PERMISSION_UNKNOWN
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionResult {
    }

    public static final int PERMISSION_GRANTED = 0;

    public static final int PERMISSION_DENIED = -1;

    public static final int PERMISSION_ASK = 1;

    public static final int PERMISSION_UNKNOWN = 2;

    private static final String MARK = Build.MANUFACTURER.toLowerCase();

    @PermissionResult
    public static int check(Context context) {
        int result = PERMISSION_UNKNOWN;
        if (MARK.contains("huawei")) {
            result = checkOnEMUI(context);
        } else if (MARK.contains("xiaomi")) {
            result = checkOnMIUI(context);
        } else if (MARK.contains("oppo")) {
            result = checkOnOPPO(context);
        } else if (MARK.contains("vivo")) {
            result = checkOnVIVO(context);
        } else if (MARK.contains("samsung") || MARK.contains("meizu")) {
            result = PERMISSION_GRANTED;
        }
        return result;
    }

    public static void addShortcut(Context context, int drawableId, String route, String name) {
        //Android o以上的版本用ShortcutManager来管理快捷方式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager scm = (ShortcutManager) context.getSystemService(SHORTCUT_SERVICE);
            //设置路由（关联程序）
            Intent launcherIntent = new Intent(route);
            ShortcutInfo si = new ShortcutInfo.Builder(context, "tecentmap")
                    .setIcon(Icon.createWithResource(context, drawableId))
                    .setShortLabel(name)
                    .setIntent(launcherIntent)
                    .build();
            assert scm != null;
            boolean addSuccess = scm.requestPinShortcut(si, null);
            if (addSuccess) {
                Log.e(TAG, "addShortcut: " + "添加桌面快捷方式成功");
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        ToastUtils.showLong("添加桌面快捷方式成功");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                return;
            }
        } else {
            //"com.android.launcher.action.INSTALL_SHORTCUT"
            Intent addShortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            // 不允许重复创建
            addShortcutIntent.putExtra("tecentmap", false);
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            // 图标
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(context, drawableId));
            // 设置关联程序
            Intent launcherIntent = new Intent(route);
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);

            // 发送广播
            context.sendBroadcast(addShortcutIntent);
        }
    }

    public static boolean shortcutHigh(Context context, String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager scm = (ShortcutManager) context.getSystemService(SHORTCUT_SERVICE);
            List<ShortcutInfo> shortcutInfoList = scm.getPinnedShortcuts();
            for (ShortcutInfo pinnedShortcut : shortcutInfoList) {
                if (pinnedShortcut.getId().equals("tecentmap")) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static String getAuthorityFromPermission(Context context, String permission) {
        // 先得到默认的Launcher
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        PackageManager mPackageManager = context.getPackageManager();
        ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
        if (resolveInfo == null) {
            return null;
        }
        @SuppressLint("WrongConstant") List<ProviderInfo> info = mPackageManager.queryContentProviders(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.applicationInfo.uid, PackageManager.GET_PROVIDERS);
        if (info != null) {
            for (int j = 0; j < info.size(); j++) {
                ProviderInfo provider = info.get(j);
                if (provider.readPermission == null) {
                    continue;
                }
                if (Pattern.matches(".*launcher.*READ_SETTINGS", provider.readPermission)) {
                    return provider.authority;
                }
            }
        }
        return null;

    }

    public static boolean hasShortcutLow(Context context, String appName) {
        long start = System.currentTimeMillis();
        String authority = getAuthorityFromPermission(context, appName);
        if (authority == null) {
            return false;
        }
        long end = System.currentTimeMillis() - start;
        String url = "content://" + authority + "/favorites?notify=true";
        try {
            Uri CONTENT_URI = Uri.parse(url);
            Cursor c = context.getContentResolver().query(CONTENT_URI, null, " title= ? ", new String[]{appName}, null);
            if (c != null && c.moveToFirst()) {
                c.close();
                return true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

}
