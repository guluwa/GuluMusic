package cn.guluwa.gulumusic.data.bean;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Ignore;

import java.io.Serializable;
import java.util.List;

/**
 * 歌曲基础类
 * <p>
 * Created by guluwa on 2018/1/31.
 */

public class BaseSongBean implements Serializable {

    private static final long serialVersionUID = -3436929448594928827L;
    private String name;
    @Embedded
    private TracksBean.AlBean al;
    @Embedded
    private TracksBean.ArBean singer;
    private String tag;
    private String source;

    @Ignore
    private int currentTime = -1;
    @Ignore
    private int duration;
    @Ignore
    private List<TracksBean.ArBean> ar;
    @Ignore
    private List<String> alia;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TracksBean.AlBean getAl() {
        return al;
    }

    public void setAl(TracksBean.AlBean al) {
        this.al = al;
    }

    public TracksBean.ArBean getSinger() {
        return singer;
    }

    public void setSinger(TracksBean.ArBean singer) {
        this.singer = singer;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<TracksBean.ArBean> getAr() {
        return ar;
    }

    public void setAr(List<TracksBean.ArBean> ar) {
        this.ar = ar;
    }

    public List<String> getAlia() {
        return alia;
    }

    public void setAlia(List<String> alia) {
        this.alia = alia;
    }

    public static class AlBean implements Serializable {

        /**
         * id : 36957040
         * name : 说散就散
         * picUrl : https://p1.music.126.net/e50cdn6BVUCFFHpN9RIidA==/109951163081271235.jpg
         */

        private static final long serialVersionUID = 6802054785233923907L;
        @ColumnInfo(name = "al_id")
        private int id;
        @ColumnInfo(name = "al_name")
        private String name;
        private String picUrl = "";

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }
    }

    public static class ArBean implements Serializable {

        /**
         * id : 10473
         * name : 袁娅维
         */

        private static final long serialVersionUID = 1990373790504589909L;
        @ColumnInfo(name = "singer_id")
        private int id;
        @ColumnInfo(name = "singer_name")
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
