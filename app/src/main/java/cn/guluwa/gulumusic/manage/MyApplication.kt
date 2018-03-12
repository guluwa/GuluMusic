package cn.guluwa.gulumusic.manage

import android.app.Application
import android.content.Context

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.data.local.db.DBHelper
import cn.guluwa.gulumusic.utils.AppUtils
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

/**
 * Created by guluwa on 2018/1/9.
 */

class MyApplication : Application() {

    companion object {

        private lateinit var mContext: Context

        fun getContext() = mContext
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this

        initCalligraphy()
        initPlayInfo()
        initDataBase()
    }

    private fun initCalligraphy() {
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/SL-Simplified-Regular.ttf")
                .setFontAttrId(R.attr.fontPath).build())
    }

    private fun initPlayInfo() {
        AppManager.getInstance().playMode = AppUtils.getInteger(Contacts.PLAY_MODE, 2)
        AppManager.getInstance().playStatus = AppUtils.getString(Contacts.PLAY_STATUS, "hot")
        AppManager.getInstance().searchPlatform = AppUtils.getString(Contacts.SEARCH_PLATFORM, Contacts.TYPE_NETEASE)
    }


    private fun initDataBase() {
        DBHelper.getInstance().initDataBase(mContext)
    }
}
