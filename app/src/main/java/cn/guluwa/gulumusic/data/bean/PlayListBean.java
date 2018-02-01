package cn.guluwa.gulumusic.data.bean;

import java.util.List;

/**
 * 热门歌曲集合类
 *
 * Created by guluwa on 2018/1/11.
 */

public class PlayListBean {

    /**
     * playlist : {"tracks":[{"name":"说散就散","id":523251118,"ar":[{"id":10473,"name":"袁娅维"}],"alia":["电影《前任3：再见前任》主题曲"],"al":{"id":36957040,"name":"说散就散","picUrl":"https://p1.music.126.net/e50cdn6BVUCFFHpN9RIidA==/109951163081271235.jpg"}}]}
     */

    private PlaylistBean playlist;

    public PlaylistBean getPlaylist() {
        return playlist;
    }

    public void setPlaylist(PlaylistBean playlist) {
        this.playlist = playlist;
    }

    public static class PlaylistBean {
        private List<TracksBean> tracks;

        public List<TracksBean> getTracks() {
            return tracks;
        }

        public void setTracks(List<TracksBean> tracks) {
            this.tracks = tracks;
        }

    }

}
