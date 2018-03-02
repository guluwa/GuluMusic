package cn.guluwa.gulumusic.data.bean

/**
 * 页面数据类
 *
 * Created by guluwa on 2018/1/3.
 */

class ViewDataBean<out T>(val status: PageStatus, val data: T?, val throwable: Throwable?) {

    companion object {

        fun <T> content(data: T): ViewDataBean<T> {
            return ViewDataBean(PageStatus.Content, data, null)
        }

        fun <T> error(data: T?, throwable: Throwable): ViewDataBean<T> {
            return ViewDataBean<T>(PageStatus.Error, data, throwable)
        }

        fun <T> error(throwable: Throwable): ViewDataBean<T> {
            return error(null, throwable)
        }

        fun <T> empty(data: T?): ViewDataBean<T> {
            return ViewDataBean<T>(PageStatus.Empty, data, null)
        }

        fun <T> empty(): ViewDataBean<T> {
            return empty(null)
        }

        fun <T> loading(data: T?): ViewDataBean<T> {
            return ViewDataBean<T>(PageStatus.Loading, data, null)
        }

        fun <T> loading(): ViewDataBean<T> {
            return loading(null)
        }
    }
}
