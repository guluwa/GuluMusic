package cn.guluwa.gulumusic.data.bean;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;

/**
 * Created by guluwa on 2018/1/12.
 */
@Entity(tableName = "net_cloud_hot_songs")
public class TracksBean {
    /**
     * name : 说散就散
     * id : 523251118
     * ar : [{"id":10473,"name":"袁娅维"}]
     * alia : ["电影《前任3：再见前任》主题曲"]
     * al : {"id":36957040,"name":"说散就散","picUrl":"https://p1.music.126.net/e50cdn6BVUCFFHpN9RIidA==/109951163081271235.jpg"}
     */

    private int index;
    private String name;
    @PrimaryKey
    private int id;
    @Embedded
    private AlBean al;
    @Embedded
    private ArBean singer;
    private String tag;

    @Ignore
    private List<ArBean> ar;
    @Ignore
    private List<String> alia;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AlBean getAl() {
        return al;
    }

    public void setAl(AlBean al) {
        this.al = al;
    }

    public ArBean getSinger() {
        return singer;
    }

    public void setSinger(ArBean singer) {
        this.singer = singer;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<ArBean> getAr() {
        return ar;
    }

    public void setAr(List<ArBean> ar) {
        this.ar = ar;
    }

    public List<String> getAlia() {
        return alia;
    }

    public void setAlia(List<String> alia) {
        this.alia = alia;
    }

    public static class AlBean {
        /**
         * id : 36957040
         * name : 说散就散
         * picUrl : https://p1.music.126.net/e50cdn6BVUCFFHpN9RIidA==/109951163081271235.jpg
         */

        @ColumnInfo(name = "al_id")
        private int id;
        @ColumnInfo(name = "al_name")
        private String name;
        private String picUrl;

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

    public static class ArBean {
        /**
         * id : 10473
         * name : 袁娅维
         */

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
