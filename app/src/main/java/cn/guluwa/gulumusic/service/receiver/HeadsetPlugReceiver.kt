package cn.guluwa.gulumusic.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.guluwa.gulumusic.manage.AppManager
import android.media.AudioManager




/**
 * Created by guluwa on 2018/3/9.
 */

class HeadsetPlugReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.hasExtra("state")) {
//            if (intent.getIntExtra("state", 0) == 0) {
//                //disconnect
//                if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
//                    AppManager.getInstance().musicAutoService!!.binder.isPrepare = true
//                    AppManager.getInstance().musicAutoService!!.binder.playOrPauseSong(-1)
//                }
//            } else if (intent.getIntExtra("state", 0) == 1) {
//                //connect
//            }
//        }

        val action = intent.action
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == action) {
            if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                AppManager.getInstance().musicAutoService!!.binder.isPrepare = true
                AppManager.getInstance().musicAutoService!!.binder.playOrPauseSong(-1)
            }
        }
    }
}