package cn.guluwa.gulumusic.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.ui.main.MainActivity

/**
 * Created by guluwa on 2018/3/9.
 */
class SongOperationReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        if (AppManager.getInstance().musicAutoService == null) {
            p0!!.startActivity(Intent(p0, MainActivity::class.java))
        } else {
            when (p1!!.action) {
                "play" -> AppManager.getInstance().musicAutoService!!.binder.playOrPauseSong()
                "previous" -> {
                    AppManager.getInstance().musicAutoService!!.binder.stop()
                    AppManager.getInstance().musicAutoService!!.binder.getLastSong()
                    AppManager.getInstance().musicAutoService!!.binder.isPrepare = false
                    AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(null, false)
                }
                "next" -> {
                    AppManager.getInstance().musicAutoService!!.binder.stop()
                    AppManager.getInstance().musicAutoService!!.binder.getNextSong()
                    AppManager.getInstance().musicAutoService!!.binder.isPrepare = false
                    AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(null, false)
                }
            }
        }
    }
}