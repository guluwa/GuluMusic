package cn.guluwa.gulumusic.manage;

import android.app.Application;
import android.content.Context;

/**
 * Created by guluwa on 2018/1/9.
 */

public class MyApplication extends Application{

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }
}
