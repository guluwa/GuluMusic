package cn.guluwa.gulumusic.data.bean;

/**
 * Created by guluwa on 2018/1/27.
 */
public class FreshBean {
    public boolean isFresh;
    public TracksBean song ;

    public FreshBean(TracksBean song, boolean isFresh) {
        this.song= song;
        this.isFresh = isFresh;
    }
}
