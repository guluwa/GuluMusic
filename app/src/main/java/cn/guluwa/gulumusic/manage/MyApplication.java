package cn.guluwa.gulumusic.manage;

import android.app.Application;
import android.content.Context;

import cn.guluwa.gulumusic.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by guluwa on 2018/1/9.
 */

public class MyApplication extends Application{

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().
                setDefaultFontPath("fonts/Roboto-Monospace-Regular.ttf").setFontAttrId(R.attr.fontPath).build());
    }

    public static Context getContext() {
        return mContext;
    }
}
