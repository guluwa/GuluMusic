package cn.guluwa.gulumusic.service.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.support.v4.media.app.NotificationCompat.MediaStyle
import android.widget.RemoteViews
import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.manage.MyApplication
import cn.guluwa.gulumusic.ui.main.MainActivity
import cn.guluwa.gulumusic.utils.AppUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


/**
 * Created by guluwa on 2018/3/9.
 */
class MyNotificationManager {

    private var notification: Notification? = null

    private var manager: NotificationManager? = null

    private var notificationLayout: RemoteViews? = null

    private var notificationLayoutBig: RemoteViews? = null

    private var builder: NotificationCompat.Builder? = null

    private var id = ""

    private var name = ""

    fun showNotification(context: Context) {

        if (notification == null) {
            builder = NotificationCompat.Builder(context)
            manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            notificationLayout = RemoteViews(context.packageName, R.layout.notification)
            notificationLayoutBig = RemoteViews(context.packageName, R.layout.notification_big)

            notificationLayout!!.setOnClickPendingIntent(R.id.action_prev,
                    PendingIntent.getBroadcast(context, 0, Intent("previous"), 0))
            notificationLayout!!.setOnClickPendingIntent(R.id.action_play_pause,
                    PendingIntent.getBroadcast(context, 0, Intent("play"), 0))
            notificationLayout!!.setOnClickPendingIntent(R.id.action_next,
                    PendingIntent.getBroadcast(context, 0, Intent("next"), 0))

            notificationLayoutBig!!.setOnClickPendingIntent(R.id.action_prev,
                    PendingIntent.getBroadcast(context, 0, Intent("previous"), 0))
            notificationLayoutBig!!.setOnClickPendingIntent(R.id.action_play_pause,
                    PendingIntent.getBroadcast(context, 0, Intent("play"), 0))
            notificationLayoutBig!!.setOnClickPendingIntent(R.id.action_next,
                    PendingIntent.getBroadcast(context, 0, Intent("next"), 0))

            val intent = Intent(context, MainActivity::class.java)
            val contentIntent = PendingIntent.getActivity(context, 0, intent, 0)

            builder!!.setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContent(notificationLayout)
                    .setCustomBigContentView(notificationLayoutBig)
                    .setOngoing(true).priority = NotificationCompat.PRIORITY_MAX
        }

        notificationLayout!!.setImageViewResource(R.id.action_play_pause, R.drawable.ic_pause_song)
        notificationLayoutBig!!.setImageViewResource(R.id.action_play_pause, R.drawable.ic_pause_song)

        if (id != AppManager.getInstance().musicAutoService!!.binder.currentSong!!.id &&
                name != AppManager.getInstance().musicAutoService!!.binder.currentSong!!.name) {
            id = AppManager.getInstance().musicAutoService!!.binder.currentSong!!.id
            name = AppManager.getInstance().musicAutoService!!.binder.currentSong!!.name
            notificationLayout!!.setTextViewText(R.id.title, AppManager.getInstance().musicAutoService!!.binder.currentSong!!.name)
            notificationLayout!!.setTextViewText(R.id.text, AppManager.getInstance().musicAutoService!!.binder.currentSong!!.singer!!.name +
                    " - " + AppManager.getInstance().musicAutoService!!.binder.currentSong!!.al!!.name)

            notificationLayoutBig!!.setTextViewText(R.id.title, AppManager.getInstance().musicAutoService!!.binder.currentSong!!.name)
            notificationLayoutBig!!.setTextViewText(R.id.text, AppManager.getInstance().musicAutoService!!.binder.currentSong!!.singer!!.name +
                    " - " + AppManager.getInstance().musicAutoService!!.binder.currentSong!!.al!!.name)
            Glide.with(MyApplication.getContext())
                    .asBitmap()
                    .load(AppManager.getInstance().musicAutoService!!.binder.currentSong!!.al!!.picUrl)
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                            notificationLayout!!.setImageViewBitmap(R.id.image, BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
                            notificationLayout!!.setImageViewBitmap(R.id.image, BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
                            notification = builder!!.build()
                            manager!!.notify(22, notification)
                            return true
                        }

                        override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                            notificationLayout!!.setImageViewBitmap(R.id.image, resource)
                            notificationLayoutBig!!.setImageViewBitmap(R.id.image, resource)
                            notification = builder!!.build()
                            manager!!.notify(22, notification)
                            return true
                        }
                    }).into(AppUtils.dp2px(context, 128f), AppUtils.dp2px(context, 128f))
        } else {
            notification = builder!!.build()
            manager!!.notify(22, notification)
        }
    }

    fun dismissNotification() {
        if (manager != null)
            manager!!.cancel(22)
    }

    fun setPauseStatus() {
        notificationLayout!!.setImageViewResource(R.id.action_play_pause, R.drawable.ic_play_song)
        notificationLayoutBig!!.setImageViewResource(R.id.action_play_pause, R.drawable.ic_play_song)
        notification = builder!!.build()
        manager!!.notify(22, notification)
    }

    object SingletonHolder {
        //单例（静态内部类）
        val instance = MyNotificationManager()
    }

    companion object {
        fun getInstance() = SingletonHolder.instance
    }
}