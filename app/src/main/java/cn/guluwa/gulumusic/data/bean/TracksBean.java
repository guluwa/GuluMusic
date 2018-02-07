package cn.guluwa.gulumusic.data.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * 单曲信息类
 * <p>
 * Created by guluwa on 2018/1/12.
 */
@Entity(tableName = "net_cloud_hot_songs")
public class TracksBean extends BaseSongBean implements Serializable {

    /**
     * name : 说散就散
     * id : 523251118
     * ar : [{"id":10473,"name":"袁娅维"}]
     * alia : ["电影《前任3：再见前任》主题曲"]
     * al : {"id":36957040,"name":"说散就散","picUrl":"https://p1.music.126.net/e50cdn6BVUCFFHpN9RIidA==/109951163081271235.jpg"}
     */

    private static final long serialVersionUID = -5588034621712529228L;

    @NonNull
    @PrimaryKey
    private String id;
    private int index;

    @Ignore
    private String pic_id = "";
    @Ignore
    private String url_id = "";
    @Ignore
    private String lyric_id = "";

    public String getPic_id() {
        return pic_id;
    }

    public void setPic_id(String pic_id) {
        this.pic_id = pic_id;
    }

    public String getUrl_id() {
        return url_id;
    }

    public void setUrl_id(String url_id) {
        this.url_id = url_id;
    }

    public String getLyric_id() {
        return lyric_id;
    }

    public void setLyric_id(String lyric_id) {
        this.lyric_id = lyric_id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }
}
