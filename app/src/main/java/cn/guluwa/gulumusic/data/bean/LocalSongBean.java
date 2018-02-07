package cn.guluwa.gulumusic.data.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

/**
 * 本地歌曲类
 *
 * Created by guluwa on 2018/1/31.
 */

@Entity(tableName = "local_songs")
public class LocalSongBean extends BaseSongBean implements Serializable {

    private static final long serialVersionUID = 1377109839840074638L;

    @PrimaryKey(autoGenerate = true)
    private int index;

    private String id;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
