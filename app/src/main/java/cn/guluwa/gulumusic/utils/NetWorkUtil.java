package cn.guluwa.gulumusic.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import cn.guluwa.gulumusic.manage.MyApplication;


/**
 * Created by Administrator on 2017/12/11.
 */

public class NetWorkUtil {

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
}
