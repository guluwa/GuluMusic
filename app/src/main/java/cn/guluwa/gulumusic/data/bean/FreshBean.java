package cn.guluwa.gulumusic.data.bean;

/**
 * 数据刷新类
 *
 * Created by guluwa on 2018/1/27.
 */
public class FreshBean {
    public boolean isFresh;
    public boolean isFirstComing;
    public TracksBean song;

    public FreshBean(boolean isFresh, boolean isFirstComing) {
        this.isFresh = isFresh;
        this.isFirstComing = isFirstComing;
    }

    public FreshBean(TracksBean song, boolean isFresh) {
        this.song = song;
        this.isFresh = isFresh;
    }
}
