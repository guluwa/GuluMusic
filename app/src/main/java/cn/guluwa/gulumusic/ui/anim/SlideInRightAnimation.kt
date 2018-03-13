package cn.guluwa.gulumusic.ui.anim

import android.animation.Animator
import android.opengl.ETC1.getWidth
import android.animation.ObjectAnimator
import android.view.View


/**
 * Created by guluwa on 2018/3/13.
 */

class SlideInRightAnimation : BaseAnimation {

    override fun getAnimators(view: View): Array<Animator> {
        return arrayOf(ObjectAnimator.ofFloat(view, "translationX", view.rootView.width.toFloat(), 0f))
    }
}