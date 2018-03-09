package cn.guluwa.gulumusic.utils.listener

/**
 * Created by guluwa on 2018/1/19.
 */

interface OnResultListener<T> {

    fun success(result: T)

    fun failed(error: String)
}
