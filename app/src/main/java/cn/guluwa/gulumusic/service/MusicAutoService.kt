package cn.guluwa.gulumusic.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

import cn.guluwa.gulumusic.manage.AppManager


/**
 * Created by guluwa on 2018/1/26.
 */

class MusicAutoService : Service() {

    /**
     * 播放器焦点管理器
     */
    var audioFocusManager: AudioFocusManager? = null
        private set

    //  通过 Binder 来保持 Activity 和 Service 的通信
    var binder = MusicBinder(this)

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        audioFocusManager = AudioFocusManager(this)
        Log.w(TAG, "MusicAutoService in onCreate")
    }

    override fun onDestroy() {
        binder.unbindProgressQuery()
        audioFocusManager!!.abandonAudioFocus()
        binder.mediaPlayer!!.reset()
        binder.mediaPlayer!!.release()
        binder.mediaPlayer=null
        AppManager.getInstance().musicAutoService = null
        super.onDestroy()
        Log.w(TAG, "MusicAutoService in onDestroy")
    }

    /**
     * 主动结束服务
     */
    fun quit() {
        stopSelf()
    }

    companion object {

        const val TAG = "MusicAutoService"
    }
}
