package cn.guluwa.gulumusic.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.graphics.Palette;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.guluwa.gulumusic.data.bean.BaseSongBean;
import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource;
import cn.guluwa.gulumusic.listener.OnColorListener;
import cn.guluwa.gulumusic.manage.MyApplication;
import okhttp3.ResponseBody;


/**
 * Created by guluwa on 2017/12/11.
 */

public class AppUtils {

    /**
     * 对颜色进行加深处理
     *
     * @return
     */
    public static int deepenColor(int RGBValues) {
        int alpha = RGBValues >> 24;
        int red = RGBValues >> 16 & 0xFF;
        int green = RGBValues >> 8 & 0xFF;
        int blue = RGBValues & 0xFF;
        red = (int) Math.floor(red * (1 - 0.1));
        green = (int) Math.floor(green * (1 - 0.1));
        blue = (int) Math.floor(blue * (1 - 0.1));
        return Color.rgb(red, green, blue);
    }

    /**
     * 检测网络是否连接
     */
    public static boolean isNetConnected() {
        ConnectivityManager cm = (ConnectivityManager) MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 根据图片设定背景颜色和文字颜色
     */
    public static void getBackGroundAndTextColor(ImageView imageView, OnColorListener listener) {
        int[] colors = new int[3];
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        Palette.Builder builder = Palette.from(bitmap);
        builder.generate(palette -> {
            Palette.Swatch vibrantSwatch;
            if ((vibrantSwatch = palette.getMutedSwatch()) != null) {
                colors[0] = vibrantSwatch.getRgb();
                colors[1] = vibrantSwatch.getBodyTextColor();
                colors[2] = vibrantSwatch.getTitleTextColor();
                listener.success(colors);
            } else if ((vibrantSwatch = palette.getLightMutedSwatch()) != null) {
                colors[0] = vibrantSwatch.getRgb();
                colors[1] = vibrantSwatch.getBodyTextColor();
                colors[2] = vibrantSwatch.getTitleTextColor();
                listener.success(colors);
            } else if ((vibrantSwatch = palette.getDarkMutedSwatch()) != null) {
                colors[0] = vibrantSwatch.getRgb();
                colors[1] = vibrantSwatch.getBodyTextColor();
                colors[2] = vibrantSwatch.getTitleTextColor();
                listener.success(colors);
            } else if ((vibrantSwatch = palette.getVibrantSwatch()) != null) {
                colors[0] = deepenMoreColor(vibrantSwatch.getRgb());
                colors[1] = deepenColor(vibrantSwatch.getBodyTextColor());
                colors[2] = deepenColor(vibrantSwatch.getTitleTextColor());
                listener.success(colors);
            }
        });
    }

    /**
     * 对颜色进行加深处理
     *
     * @return
     */
    public static int deepenMoreColor(int RGBValues) {
        int alpha = RGBValues >> 24;
        int red = RGBValues >> 16 & 0xFF;
        int green = RGBValues >> 8 & 0xFF;
        int blue = RGBValues & 0xFF;
        red = (int) Math.floor(red * (1 + 0.9));
        green = (int) Math.floor(green * (1 + 0.9));
        blue = (int) Math.floor(blue * (1 + 0.9));
        return Color.rgb(red, green, blue);
    }

    /**
     * 对颜色进行加深处理
     *
     * @return
     */
    public static int deepenLittleColor(int RGBValues) {
        int alpha = RGBValues >> 24;
        int red = RGBValues >> 16 & 0xFF;
        int green = RGBValues >> 8 & 0xFF;
        int blue = RGBValues & 0xFF;
        red = (int) Math.floor(red * (1 + 0.5));
        green = (int) Math.floor(green * (1 + 0.5));
        blue = (int) Math.floor(blue * (1 + 0.5));
        return Color.rgb(red, green, blue);
    }

    //手机屏幕宽高
    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric;
    }

    //创建文件夹
    private static File createFile(String filename, int type) {
        File file;
        File file1;
        if (type == 1) {
            file1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gulu_music/song");
        } else {
            file1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gulu_music/word");
        }
        if (!file1.exists())
            file1.mkdirs();
        file = new File(file1.getAbsolutePath() + "/" + filename);
        return file;
    }

    //保存歌曲文件到本地
    public static File writeSong2Disk(ResponseBody responseBody, String filename) {

        File file = createFile(filename, 1);
        OutputStream os = null;
        InputStream is = responseBody.byteStream();

        try {
            os = new FileOutputStream(file);
            int len;
            byte[] buff = new byte[1024];
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
            }
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //保存歌词文本到本地
    public static void writeWord2Disk(String str, String filename) {
        try {
            File file = createFile(filename, 2);
            FileWriter fw = new FileWriter(file.getAbsolutePath());//SD卡中的路径
            fw.flush();
            fw.write(str);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //检测文件是否存在
    public static String isExistFile(String name, int type) {
        File file;
        if (type == 1) {
            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gulu_music/song/" + name);
        } else {
            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gulu_music/word/" + name);
        }
        return file.exists() ? file.getAbsolutePath() : "";
    }

    //获取sharePreference Integer类型的值
    public static int getInteger(String key, final int defaultValue) {
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getContext());
        return settings.getInt(key, defaultValue);
    }

    //设置sharePreference Integer类型的值
    public static void setInteger(final String key, final int value) {
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getContext());
        settings.edit().putInt(key, value).apply();
    }

    //获取sharePreference String类型的值
    public static String getString(String key, final String defaultValue) {
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getContext());
        return settings.getString(key, defaultValue);
    }

    //设置sharePreference String类型的值
    public static void setString(final String key, final String value) {
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getContext());
        settings.edit().putString(key, value).apply();
    }


    //分钟转化成秒
    public static int getSeconds(String time) {
        String minute = time.substring(0, 2);
        String second = time.substring(3, 5);
        return Integer.valueOf(minute) * 60 + Integer.valueOf(second);
    }

    //本地歌曲转热门歌曲
    public static TracksBean getSongBean(LocalSongBean localSongBean) {
        TracksBean tracksBean = new TracksBean();
        tracksBean.setId(localSongBean.getId());
        tracksBean.setName(localSongBean.getName());
        tracksBean.setAl(localSongBean.getAl());
        tracksBean.setSinger(localSongBean.getSinger());
        tracksBean.setTag(localSongBean.getTag());
        tracksBean.setSource(localSongBean.getSource());
        return tracksBean;
    }

    //搜索歌曲转热门歌曲
    public static TracksBean getSongBean(SearchResultSongBean songBean) {
        TracksBean tracksBean = new TracksBean();
        tracksBean.setId(songBean.getId());
        tracksBean.setName(songBean.getName());
        TracksBean.ArBean singer = new BaseSongBean.ArBean();
        singer.setName(songBean.getArtist().size() != 0 ? songBean.getArtist().get(0) : "");
        tracksBean.setSinger(singer);
        TracksBean.AlBean alBean = new BaseSongBean.AlBean();
        alBean.setName(songBean.getName());
        tracksBean.setAl(alBean);
        tracksBean.setTag(songBean.getAlbum());
        tracksBean.setSource(songBean.getSource());
        tracksBean.setPic_id(songBean.getPic_id());
        tracksBean.setUrl_id(songBean.getUrl_id());
        tracksBean.setLyric_id(songBean.getLyric_id());
        return tracksBean;
    }

    //热门歌曲转本地歌曲
    public static LocalSongBean getLocalSongBean(TracksBean tracksBean) {
        LocalSongBean localSongBean = new LocalSongBean();
        localSongBean.setId(tracksBean.getId());
        localSongBean.setName(tracksBean.getName());
        localSongBean.setAl(tracksBean.getAl());
        localSongBean.setSinger(tracksBean.getSinger());
        localSongBean.setTag(tracksBean.getTag());
        localSongBean.setSource(tracksBean.getSource());
        return localSongBean;
    }

    //热门歌曲转基础歌曲
    public static BaseSongBean getBaseSongBean(TracksBean tracksBean) {
        BaseSongBean baseSongBean = new BaseSongBean();
        baseSongBean.setName(tracksBean.getName());
        baseSongBean.setAl(tracksBean.getAl());
        baseSongBean.setSinger(tracksBean.getSinger());
        baseSongBean.setTag(tracksBean.getTag());
        baseSongBean.setSource(tracksBean.getSource());
        return baseSongBean;
    }

    //计算播放模式
    public static int getPlayMode(int mode) {
        if (mode < 2) {
            return ++mode;
        } else {
            return 0;
        }
    }

    //判断网络状态
    public static String getNetworkType(Context context) {
        String strNetworkType = "";

        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                strNetworkType = "WIFI";
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String _strSubTypeName = networkInfo.getSubtypeName();

                Log.e("gulu", "Network getSubtypeName : " + _strSubTypeName);

                // TD-SCDMA   networkType is 17
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        strNetworkType = "2G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        strNetworkType = "3G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        strNetworkType = "4G";
                        break;
                    default:
                        // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            strNetworkType = "3G";
                        } else {
                            strNetworkType = _strSubTypeName;
                        }
                        break;
                }

                Log.e("gulu", "Network getSubtype : " + Integer.valueOf(networkType).toString());
            }
        }

        Log.e("gulu", "Network Type : " + strNetworkType);

        return strNetworkType;
    }
}
