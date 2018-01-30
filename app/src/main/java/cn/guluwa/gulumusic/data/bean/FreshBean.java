package cn.guluwa.gulumusic.data.bean;

/**
 * Created by guluwa on 2018/1/27.
 */
public class FreshBean {
    public int id;
    public boolean isFresh;
    public String name;

    public FreshBean(int id, String name, boolean isFresh) {
        this.id = id;
        this.name = name;
        this.isFresh = isFresh;
    }
}
