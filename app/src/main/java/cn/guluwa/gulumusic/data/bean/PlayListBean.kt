package cn.guluwa.gulumusic.data.bean

/**
 * 热门歌曲集合类
 *
 * Created by guluwa on 2018/1/11.
 */

class PlayListBean {

    /**
     * playlist : {"tracks":[{"name":"说散就散","id":523251118,"ar":[{"id":10473,"name":"袁娅维"}],"alia":["电影《前任3：再见前任》主题曲"],"al":{"id":36957040,"name":"说散就散","picUrl":"https://p1.music.126.net/e50cdn6BVUCFFHpN9RIidA==/109951163081271235.jpg"}}]}
     */

    var playlist: PlaylistBean? = null

    class PlaylistBean {
        var tracks: List<TracksBean>? = null
    }
}
