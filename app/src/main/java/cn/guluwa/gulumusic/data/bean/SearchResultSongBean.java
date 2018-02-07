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

    private String id;
    private String name;
    private String album;
    private String pic_id;
    private String url_id;
    private String lyric_id;
    private String source;
    private List<String> artist;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
