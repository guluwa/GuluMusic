package cn.guluwa.gulumusic.data.bean;

/**
 * Created by guluwa on 2018/1/30.
 */

public class LrcBean {

    private long time;
    private String word;

    public LrcBean(long time, String word) {
        this.time = time;
        this.word = word;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
