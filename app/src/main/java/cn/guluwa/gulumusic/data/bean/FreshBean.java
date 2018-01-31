package cn.guluwa.gulumusic.data.bean;

/**
 * Created by guluwa on 2018/1/27.
 */
public class FreshBean {
    public boolean isFresh;
    public BaseSongBean song ;

    public FreshBean(BaseSongBean song, boolean isFresh) {
        this.song= song;
        this.isFresh = isFresh;
    }
}
