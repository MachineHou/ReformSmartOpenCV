package tech.huqi.smartopencvdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

public class BgBlurUtils {
    public static Bitmap activityShot(Activity activity) {
        /*获取windows中最顶层的view*/
        View view = activity.getWindow().getDecorView();

        //允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        //获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;

        WindowManager windowManager = activity.getWindowManager();

        //获取屏幕宽和高
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        int height = outMetrics.heightPixels;
        Bitmap blurBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.user_defaut);


        //去掉状态栏
//        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, width, height);
//        Bitmap bitmap = Bitmap.createBitmap(bitmap1, 0, 0, width, height);
//        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(),R.drawable.splash_blur);

        //销毁缓存信息
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);


        return blurBitmap;
    }

    public static Bitmap rsBlur(Context context, Bitmap source, int radius) {
        Bitmap inputBmp = source;
//        //(1)
//        //初始化一个RenderScript Context
//        RenderScript renderScript = RenderScript.create(context);
//
//        // Allocate memory for Renderscript to work with
//        //(2)
//        //创建输入输出的allocation
//        final Allocation input = Allocation.createFromBitmap(renderScript, inputBmp);
//        final Allocation output = Allocation.createTyped(renderScript, input.getType());
//        //(3)
//        // Load up an instance of the specific script that we want to use.
//        //创建ScriptIntrinsic
//        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
//        //(4)
//        //填充数据
//        scriptIntrinsicBlur.setInput(input);
//        //(5)
//        // Set the blur radius
//        //设置模糊半径
//        scriptIntrinsicBlur.setRadius(radius);
//        //(6)
//        // Start the ScriptIntrinisicBlur
//        //启动内核
//        scriptIntrinsicBlur.forEach(output);
//        //(7)
//        // Copy the output to the blurred bitmap
//        //copy数据
//        output.copyTo(inputBmp);
//        //(8)
//        //销毁renderScript
//        renderScript.destroy();
        return inputBmp;

    }
}
