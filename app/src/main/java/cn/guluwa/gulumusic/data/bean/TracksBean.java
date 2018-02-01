package cn.guluwa.gulumusic.data.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

/**
 * 单曲信息类
 *
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

    @PrimaryKey
    private int id;
    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
