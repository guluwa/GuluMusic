package cn.guluwa.gulumusic.ui.anim

import android.animation.Animator
import android.view.View

/**
 * Created by guluwa on 2018/3/13.
 */
interface BaseAnimation {

    fun getAnimators(view: View): Array<Animator>
}