package cn.guluwa.gulumusic.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import cn.guluwa.gulumusic.listener.OnColorListener;
import cn.guluwa.gulumusic.manage.MyApplication;


/**
 * Created by guluwa on 2017/12/11.
 */

public class AppUtils {

    /**
     * 对颜色进行加深处理
     *
     * @return
     */
    public static int deepenColor(int RGBValues) {
        int alpha = RGBValues >> 24;
        int red = RGBValues >> 16 & 0xFF;
        int green = RGBValues >> 8 & 0xFF;
        int blue = RGBValues & 0xFF;
        red = (int) Math.floor(red * (1 - 0.1));
        green = (int) Math.floor(green * (1 - 0.1));
        blue = (int) Math.floor(blue * (1 - 0.1));
        return Color.rgb(red, green, blue);
    }

    /**
     * 检测网络是否连接
     */
    public static boolean isNetConnected() {
        ConnectivityManager cm = (ConnectivityManager) MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 根据图片设定背景颜色和文字颜色
     */
    public static void getBackGroundAndTextColor(ImageView imageView, OnColorListener listener) {
        int[] colors = new int[3];
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        Palette.Builder builder = Palette.from(bitmap);
        builder.generate(palette -> {
            Palette.Swatch vibrantSwatch;
            if ((vibrantSwatch = palette.getMutedSwatch()) != null) {
                colors[0] = vibrantSwatch.getRgb();
                colors[1] = vibrantSwatch.getBodyTextColor();
                colors[2] = vibrantSwatch.getTitleTextColor();
                listener.success(colors);
            } else if ((vibrantSwatch = palette.getLightMutedSwatch()) != null) {
                colors[0] = vibrantSwatch.getRgb();
                colors[1] = vibrantSwatch.getBodyTextColor();
                colors[2] = vibrantSwatch.getTitleTextColor();
                listener.success(colors);
            } else if ((vibrantSwatch = palette.getDarkMutedSwatch()) != null) {
                colors[0] = vibrantSwatch.getRgb();
                colors[1] = vibrantSwatch.getBodyTextColor();
                colors[2] = vibrantSwatch.getTitleTextColor();
                listener.success(colors);
            } else if ((vibrantSwatch = palette.getVibrantSwatch()) != null) {
                colors[0] = deepenMoreColor(vibrantSwatch.getRgb());
                colors[1] = deepenColor(vibrantSwatch.getBodyTextColor());
                colors[2] = deepenColor(vibrantSwatch.getTitleTextColor());
                listener.success(colors);
            }
        });
    }

    /**
     * 对颜色进行加深处理
     *
     * @return
     */
    public static int deepenMoreColor(int RGBValues) {
        int alpha = RGBValues >> 24;
        int red = RGBValues >> 16 & 0xFF;
        int green = RGBValues >> 8 & 0xFF;
        int blue = RGBValues & 0xFF;
        red = (int) Math.floor(red * (1 + 0.9));
        green = (int) Math.floor(green * (1 + 0.9));
        blue = (int) Math.floor(blue * (1 + 0.9));
        return Color.rgb(red, green, blue);
    }

    /**
     * 对颜色进行加深处理
     *
     * @return
     */
    public static int deepenLittleColor(int RGBValues) {
        int alpha = RGBValues >> 24;
        int red = RGBValues >> 16 & 0xFF;
        int green = RGBValues >> 8 & 0xFF;
        int blue = RGBValues & 0xFF;
        red = (int) Math.floor(red * (1 + 0.5));
        green = (int) Math.floor(green * (1 + 0.5));
        blue = (int) Math.floor(blue * (1 + 0.5));
        return Color.rgb(red, green, blue);
    }

    //手机屏幕宽高
    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric;
    }
}
