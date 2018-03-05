package cn.guluwa.gulumusic.data.bean

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

import java.io.Serializable

/**
 * 歌词类
 *
 *
 * Created by guluwa on 2018/1/19.
 */

@Entity(tableName = "songs_words")
class SongWordBean : Serializable {

    @PrimaryKey
    var id: String=""
    var lyric: String? = null
    @Ignore
    var tlyric: String? = null
    @Ignore
    var song: TracksBean? = null

    companion object {

        /**
         * lyric :
         *
         *
         * [00:00.00] 作曲 : 伍乐城
         * [00:00.193] 作词 : 张楚翘
         * [00:00.580]编曲：伍乐城
         * [00:08.120]抱一抱就当作从没有在一起
         * [00:14.550]好不好要解释都已经来不及
         * [00:21.450]算了吧我付出过什么没关系
         * [00:26.890]我忽略自己就因为遇见你
         * [00:33.830]没办法好可怕那个我不像话
         * [00:40.540]一直奋不顾身是我太傻
         * [00:46.260]说不上爱别说谎就一点喜欢
         * [00:53.580]说不上恨别纠缠别装作感叹
         * [01:00.400]就当作我太麻烦不停让自己受伤
         * [01:04.290]我告诉我自己感情就是这样
         * [01:10.310]怎么一不小心太疯狂
         * [01:23.460]抱一抱再好好觉悟不能长久
         * [01:30.100]好不好有亏欠我们都别追究
         * [01:36.890]算了吧我付出再多都不足够
         * [01:42.380]我终于得救我不想再献丑
         * [01:49.600]没办法不好吗大家都不留下
         * [01:55.980]一直勉强相处总会累垮
         * [02:01.910]说不上爱别说谎就一点喜欢
         * [02:08.960]说不上恨别纠缠别装作感叹
         * [02:15.540]就当作我太麻烦不停让自己受伤
         * [02:19.770]我告诉我自己感情就是这样
         * [02:25.800]怎么一不小心太疯狂
         * [02:31.950]别后悔就算错过
         * [02:35.290]在以后你少不免想起我
         * [02:41.250]还算不错
         * [02:44.800]当我不在你会不会难过
         * [02:48.420]你够不够我这样洒脱
         * [02:53.570]说不上爱别说谎就一点喜欢
         * [03:00.270]说不上恨别纠缠别装作感叹
         * [03:06.100]将一切都体谅将一切都原谅
         * [03:09.200]我尝试找答案而答案很简单
         * [03:12.800]简单得很遗憾
         * [03:15.060]因为成长我们逼不得已要习惯
         * [03:21.670]因为成长我们忽尔间说散就散
         *
         *
         * tlyric :
         */

        private const val serialVersionUID = -1442257143570149636L
    }
}