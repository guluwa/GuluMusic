package cn.guluwa.gulumusic.data.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by guluwa on 2018/1/19.
 */

@Entity(tableName = "songs_path")
public class SongPathBean implements Serializable {

    /**
     * url : https://m7c.music.126.net/20180119171008/de4c0224042f824bbe7d421a1196202f/ymusic/4afa/0216/a89f/c9941d4ebd3f829a9a3b3a52a8d738ce.mp3
     * br : 128
     */

    private static final long serialVersionUID = -6267004480939769470L;
    @PrimaryKey
    private int id;
    private String url;
    @Ignore
    private int br;
    @Ignore
    private TracksBean song;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getBr() {
        return br;
    }

    public void setBr(int br) {
        this.br = br;
    }

    public TracksBean getSong() {
        return song;
    }

    public void setSong(TracksBean song) {
        this.song = song;
    }
}
