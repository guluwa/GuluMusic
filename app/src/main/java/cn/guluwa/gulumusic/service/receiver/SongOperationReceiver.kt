package cn.guluwa.gulumusic.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.guluwa.gulumusic.manage.AppManager

/**
 * Created by guluwa on 2018/3/9.
 */
class SongOperationReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        when (p1!!.action) {
            "play" -> AppManager.getInstance().musicAutoService!!.binder.playOrPauseSong(-1)
            "previous" -> {
                AppManager.getInstance().musicAutoService!!.binder.stop()
                AppManager.getInstance().musicAutoService!!.binder.getLastSong(AppManager.getInstance().musicAutoService!!.binder.currentSong!!)
                AppManager.getInstance().musicAutoService!!.binder.isPrepare = false
                AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(AppManager.getInstance().musicAutoService!!.binder.currentSong!!, 0, false)
            }
            "next" -> {
                AppManager.getInstance().musicAutoService!!.binder.stop()
                AppManager.getInstance().musicAutoService!!.binder.getNextSong(AppManager.getInstance().musicAutoService!!.binder.currentSong!!)
                AppManager.getInstance().musicAutoService!!.binder.isPrepare = false
                AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(AppManager.getInstance().musicAutoService!!.binder.currentSong!!, 0, false)
            }
        }
    }
}