package cn.guluwa.gulumusic.data.bean;

import java.util.List;

/**
 * Created by guluwa on 2018/2/3.
 */

public class SearchResultSongBean {

    /**
     * id : 481853040
     * name : 你就不要想起我
     * artist : ["于毅"]
     * album : 2017跨界歌王 第八期
     * pic_id : 19200771556014598
     * url_id : 481853040
     * lyric_id : 481853040
     * source : netease
     */

    private int id;
    private String name;
    private String album;
    private String pic_id;
    private int url_id;
    private int lyric_id;
    private String source;
    private List<String> artist;

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

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPic_id() {
        return pic_id;
    }

    public void setPic_id(String pic_id) {
        this.pic_id = pic_id;
    }

    public int getUrl_id() {
        return url_id;
    }

    public void setUrl_id(int url_id) {
        this.url_id = url_id;
    }

    public int getLyric_id() {
        return lyric_id;
    }

    public void setLyric_id(int lyric_id) {
        this.lyric_id = lyric_id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<String> getArtist() {
        return artist;
    }

    public void setArtist(List<String> artist) {
        this.artist = artist;
    }
}
