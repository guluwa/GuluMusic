package cn.guluwa.gulumusic.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Environment
import android.preference.PreferenceManager
import android.support.v7.graphics.Palette
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageView

import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

import cn.guluwa.gulumusic.data.bean.AlBean
import cn.guluwa.gulumusic.data.bean.ArBean
import cn.guluwa.gulumusic.data.bean.BaseSongBean
import cn.guluwa.gulumusic.data.bean.LocalSongBean
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.listener.OnColorListener
import cn.guluwa.gulumusic.manage.MyApplication
import okhttp3.ResponseBody


/**
 * Created by guluwa on 2017/12/11.
 */

object AppUtils {

    /**
     * 检测网络是否连接
     */
    val isNetConnected: Boolean
        get() {
            val cm = MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            if (networkInfo != null) {
                return true
            }
            return false
        }

    /**
     * 对颜色进行加深处理
     *
     * @return
     */
    fun deepenColor(RGBValues: Int): Int {
        val alpha = RGBValues shr 24
        var red = RGBValues shr 16 and 0xFF
        var green = RGBValues shr 8 and 0xFF
        var blue = RGBValues and 0xFF
        red = Math.floor(red * (1 - 0.1)).toInt()
        green = Math.floor(green * (1 - 0.1)).toInt()
        blue = Math.floor(blue * (1 - 0.1)).toInt()
        return Color.rgb(red, green, blue)
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    /**
     * 根据图片设定背景颜色和文字颜色
     */
    fun getBackGroundAndTextColor(imageView: ImageView, listener: OnColorListener) {
        val colors = IntArray(3)
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val builder = Palette.from(bitmap)
        builder.generate { palette ->
            val vibrantSwatch: Palette.Swatch?
            when {
                palette.mutedSwatch != null -> {
                    vibrantSwatch = palette.mutedSwatch
                    colors[0] = vibrantSwatch!!.rgb
                    colors[1] = vibrantSwatch.bodyTextColor
                    colors[2] = vibrantSwatch.titleTextColor
                    listener.success(colors)
                }
                palette.lightMutedSwatch != null -> {
                    vibrantSwatch = palette.lightMutedSwatch
                    colors[0] = vibrantSwatch!!.rgb
                    colors[1] = vibrantSwatch.bodyTextColor
                    colors[2] = vibrantSwatch.titleTextColor
                    listener.success(colors)
                }
                palette.darkMutedSwatch != null -> {
                    vibrantSwatch = palette.darkMutedSwatch
                    colors[0] = vibrantSwatch!!.rgb
                    colors[1] = vibrantSwatch.bodyTextColor
                    colors[2] = vibrantSwatch.titleTextColor
                    listener.success(colors)
                }
                palette.vibrantSwatch != null -> {
                    vibrantSwatch = palette.vibrantSwatch
                    colors[0] = deepenMoreColor(vibrantSwatch!!.rgb)
                    colors[1] = deepenColor(vibrantSwatch.bodyTextColor)
                    colors[2] = deepenColor(vibrantSwatch.titleTextColor)
                    listener.success(colors)
                }
            }
        }
    }

    /**
     * 对颜色进行加深处理
     *
     * @return
     */
    private fun deepenMoreColor(RGBValues: Int): Int {
        val alpha = RGBValues shr 24
        var red = RGBValues shr 16 and 0xFF
        var green = RGBValues shr 8 and 0xFF
        var blue = RGBValues and 0xFF
        red = Math.floor(red * (1 + 0.9)).toInt()
        green = Math.floor(green * (1 + 0.9)).toInt()
        blue = Math.floor(blue * (1 + 0.9)).toInt()
        return Color.rgb(red, green, blue)
    }

    /**
     * 对颜色进行加深处理
     *
     * @return
     */
    fun deepenLittleColor(RGBValues: Int): Int {
        val alpha = RGBValues shr 24
        var red = RGBValues shr 16 and 0xFF
        var green = RGBValues shr 8 and 0xFF
        var blue = RGBValues and 0xFF
        red = Math.floor(red * (1 + 0.5)).toInt()
        green = Math.floor(green * (1 + 0.5)).toInt()
        blue = Math.floor(blue * (1 + 0.5)).toInt()
        return Color.rgb(red, green, blue)
    }

    //手机屏幕宽高
    fun getDisplayMetrics(context: Context): DisplayMetrics {
        val metric = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(metric)
        return metric
    }

    //创建文件夹
    private fun createFile(filename: String, type: Int): File {
        val file: File
        val file1: File
        if (type == 1) {
            file1 = File(Environment.getExternalStorageDirectory().absolutePath + "/gulu_music/song")
        } else {
            file1 = File(Environment.getExternalStorageDirectory().absolutePath + "/gulu_music/word")
        }
        if (!file1.exists())
            file1.mkdirs()
        file = File(file1.absolutePath + "/" + filename)
        return file
    }

    //保存歌曲文件到本地
    fun writeSong2Disk(responseBody: ResponseBody, filename: String): File? {

        val file = createFile(filename, 1)
        var outputStream: OutputStream? = null
        val inputStream = responseBody.byteStream()

        try {
            outputStream = FileOutputStream(file)
            var len: Int
            val buff = ByteArray(1024)
            while (true) {
                len = inputStream!!.read(buff)
                if (len != -1)
                    outputStream.write(buff, 0, len)
                else
                    break
            }
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    //保存歌词文本到本地
    fun writeWord2Disk(str: String, filename: String) {
        try {
            val file = createFile(filename, 2)
            val fw = FileWriter(file.absolutePath)//SD卡中的路径
            fw.flush()
            fw.write(str)
            fw.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    //检测文件是否存在
    fun isExistFile(name: String, type: Int): String {
        val file: File
        if (type == 1) {
            file = File(Environment.getExternalStorageDirectory().absolutePath + "/gulu_music/song/" + name)
        } else {
            file = File(Environment.getExternalStorageDirectory().absolutePath + "/gulu_music/word/" + name)
        }
        return if (file.exists()) file.absolutePath else ""
    }

    //删除本地文件
    fun deleteFile(name: String, type: Int) {
        val file: File
        if (type == 1) {
            file = File(Environment.getExternalStorageDirectory().absolutePath + "/gulu_music/song/" + name)
        } else {
            file = File(Environment.getExternalStorageDirectory().absolutePath + "/gulu_music/word/" + name)
        }
        if (file.exists()) file.delete()
    }

    //获取sharePreference Integer类型的值
    fun getInteger(key: String, defaultValue: Int): Int {
        val settings = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getContext())
        return settings.getInt(key, defaultValue)
    }

    //设置sharePreference Integer类型的值
    fun setInteger(key: String, value: Int) {
        val settings = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getContext())
        settings.edit().putInt(key, value).apply()
    }

    //获取sharePreference String类型的值
    fun getString(key: String, defaultValue: String): String {
        val settings = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getContext())
        return settings.getString(key, defaultValue)
    }

    //设置sharePreference String类型的值
    fun setString(key: String, value: String) {
        val settings = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getContext())
        settings.edit().putString(key, value).apply()
    }


    //分钟转化成秒
    fun getSeconds(time: String): Int {
        val minute = time.substring(0, 2)
        val second = time.substring(3, 5)
        return Integer.valueOf(minute)!! * 60 + Integer.valueOf(second)!!
    }

    //本地歌曲转热门歌曲
    fun getSongBean(localSongBean: LocalSongBean): TracksBean {
        val tracksBean = TracksBean()
        tracksBean.id = localSongBean.id
        tracksBean.name = localSongBean.name
        tracksBean.al = localSongBean.al
        tracksBean.singer = localSongBean.singer
        tracksBean.tag = localSongBean.tag
        tracksBean.source = localSongBean.source
        return tracksBean
    }

    //搜索歌曲转热门歌曲
    fun getSongBean(songBean: SearchResultSongBean): TracksBean {
        val tracksBean = TracksBean()
        tracksBean.id = songBean.id
        tracksBean.name = songBean.name
        val singer = ArBean()
        singer.name = if (songBean.artist!!.size != 0) songBean.artist!![0] else ""
        tracksBean.singer = singer
        val alBean = AlBean()
        alBean.name = songBean.name
        tracksBean.al = alBean
        tracksBean.tag = songBean.album
        tracksBean.source = songBean.source
        tracksBean.pic_id = songBean.pic_id
        tracksBean.url_id = songBean.url_id
        tracksBean.lyric_id = songBean.lyric_id
        return tracksBean
    }

    //热门歌曲转本地歌曲
    fun getLocalSongBean(tracksBean: TracksBean): LocalSongBean {
        val localSongBean = LocalSongBean()
        localSongBean.id = tracksBean.id
        localSongBean.name = tracksBean.name
        localSongBean.al = tracksBean.al
        localSongBean.singer = tracksBean.singer
        localSongBean.tag = tracksBean.tag
        localSongBean.source = tracksBean.source
        return localSongBean
    }

    //热门歌曲转基础歌曲
    fun getBaseSongBean(tracksBean: TracksBean): BaseSongBean {
        val baseSongBean = BaseSongBean()
        baseSongBean.name = tracksBean.name
        baseSongBean.al = tracksBean.al
        baseSongBean.singer = tracksBean.singer
        baseSongBean.tag = tracksBean.tag
        baseSongBean.source = tracksBean.source
        return baseSongBean
    }

    //计算播放模式
    fun getPlayMode(mode: Int): Int {
        var ans = mode
        return if (mode < 2) {
            ++ans
        } else {
            0
        }
    }

    //判断网络状态
    fun getNetworkType(context: Context): String {
        var strNetworkType = ""

        val networkInfo = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                strNetworkType = "WIFI"
            } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                val _strSubTypeName = networkInfo.subtypeName

                Log.e("gulu", "Network getSubtypeName : " + _strSubTypeName)

                // TD-SCDMA   networkType is 17
                val networkType = networkInfo.subtype
                when (networkType) {
                    TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN //api<8 : replace by 11
                    -> strNetworkType = "2G"
                    TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B //api<9 : replace by 14
                        , TelephonyManager.NETWORK_TYPE_EHRPD  //api<11 : replace by 12
                        , TelephonyManager.NETWORK_TYPE_HSPAP  //api<13 : replace by 15
                    -> strNetworkType = "3G"
                    TelephonyManager.NETWORK_TYPE_LTE    //api<11 : replace by 13
                    -> strNetworkType = "4G"
                    else ->
                        // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName.equals("TD-SCDMA", ignoreCase = true) || _strSubTypeName.equals("WCDMA", ignoreCase = true) || _strSubTypeName.equals("CDMA2000", ignoreCase = true)) {
                            strNetworkType = "3G"
                        } else {
                            strNetworkType = _strSubTypeName
                        }
                }

                Log.e("gulu", "Network getSubtype : " + Integer.valueOf(networkType)!!.toString())
            }
        }

        Log.e("gulu", "Network Type : " + strNetworkType)

        return strNetworkType
    }
}
