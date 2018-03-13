package cn.guluwa.gulumusic.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import cn.guluwa.gulumusic.ui.anim.BaseAnimation

/**
 * Created by guluwa on 2018/3/13.
 */
abstract class BaseListAdapter<K : RecyclerView.ViewHolder> : RecyclerView.Adapter<K>() {

    private var list= arrayListOf<Any>()

    private var mLastPosition = -1

    private var isFirstOnly = true

    private var mSelectAnimation: BaseAnimation? = null

    private var isOpenAnimation = true

    override fun onBindViewHolder(holder: K, position: Int) {
        convert(holder)
        if (isOpenAnimation)
            if (!isFirstOnly || holder.adapterPosition > mLastPosition) {
                var animation: BaseAnimation? = null
                if (mSelectAnimation != null) {
                    animation = mSelectAnimation
                }
                for (anim in animation!!.getAnimators(holder.itemView)) {
                    anim.setDuration(300).start()
                    anim.interpolator = LinearInterpolator()
                }
                mLastPosition = holder.adapterPosition
            }
    }

    abstract fun convert(holder: K)

    /**
     * 设置动画效果
     * @param animation
     */
    fun setAnimation(animation: BaseAnimation) {
        mSelectAnimation = animation
    }

    fun setFirstOnlyEnable(firstOnly:Boolean){
        isFirstOnly=firstOnly
    }
}