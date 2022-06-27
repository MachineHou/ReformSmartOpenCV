package tech.huqi.smartopencvdemo.gauss;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

public class DeviceKeyMonitor {
    private static final String SYSTEM_REASON = "reason";
    private static final String SYSTEM_HOME_RECENT_APPS = "recentapps";
    private static final String SYSTEM_HOME_KEY = "homekey";
    private Context mContext;
    private BroadcastReceiver mDeviceKeyReceiver;
    private OnKeyListener mListener;

    public DeviceKeyMonitor(Context context, final OnKeyListener listener) {
        mContext = context;
        mListener = listener;
        mDeviceKeyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (!TextUtils.isEmpty(action)) {

                    if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                        String reason = intent.getStringExtra(SYSTEM_REASON);
                        if (reason == null) {
                            return;
                        }
                        if (SYSTEM_HOME_RECENT_APPS.equals(reason) || SYSTEM_HOME_KEY.equals(reason)) {
                            mListener.onRecentClick();

                        }
                    }
                }
            }
        };
        mContext.registerReceiver(mDeviceKeyReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    public interface OnKeyListener {
        void onRecentClick();
    }

    public void unregister() {
        if (mDeviceKeyReceiver != null) {
            mContext.unregisterReceiver(mDeviceKeyReceiver);
            mDeviceKeyReceiver = null;
        }
    }
}
