package cn.guluwa.gulumusic.data.remote.retrofit

import cn.guluwa.gulumusic.data.bean.PlayListBean
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean
import cn.guluwa.gulumusic.data.bean.SongPathBean
import cn.guluwa.gulumusic.data.bean.SongWordBean
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * Created by guluwa on 2018/1/11.
 */

interface ApiService {

    /**
     * 网易云热门
     *
     * @param map
     * @return
     */
    @POST("api.php/{callback}")
    @Headers("Host: lab.mkblog.cn", "Referer:http://qlab.mkblog.cn/music", "Cookie:UM_distinctid=160d9bcc565f8-05e85a9bbc6c66-35437c5c-1fa400-160d9bcc5662ee; Hm_lvt_6e8dac14399b608f633394093523542e=1515480945; wafenterurl=L211c2ljL2pzL2pxdWVyeS5taW4ubWFw; __utmt=1; __utma=90872199.51606438.1516347964.1516347964.1516347964.1; __utmb=90872199.1.10.1516347964; __utmc=90872199; __utmz=90872199.1516347964.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); wafcookie=a34cd89337d684080ade303438690bd3; wafverify=0b293a0aafe394d7feb4e23c300d618b; CNZZDATA1257545628=640992366-1516343148-http%253A%252F%252Flab.mkblog.cn%252F%7C1516343148; CNZZDATA1256443137=375579253-1516347691-http%253A%252F%252Flab.mkblog.cn%252F%7C1516347691; CNZZDATA1261525999=126801200-1515476375-%7C1516344357; Hm_lvt_ea4269d8a00e95fdb9ee61e3041a8f98=1516347982; Hm_lpvt_ea4269d8a00e95fdb9ee61e3041a8f98=1516348199")
    @FormUrlEncoded
    fun obtainNetCloudHot(@Path("callback") callback: String, @FieldMap map: Map<String, String>): Observable<PlayListBean>

    /**
     * 网易云歌曲歌词
     *
     * @param callback
     * @param map
     * @return
     */
    @POST("api.php/{callback}")
    @Headers("Host: lab.mkblog.cn", "Referer:http://qlab.mkblog.cn/music", "Cookie:UM_distinctid=160d9bcc565f8-05e85a9bbc6c66-35437c5c-1fa400-160d9bcc5662ee; Hm_lvt_6e8dac14399b608f633394093523542e=1515480945; wafenterurl=L211c2ljL2pzL2pxdWVyeS5taW4ubWFw; __utmt=1; __utma=90872199.51606438.1516347964.1516347964.1516347964.1; __utmb=90872199.1.10.1516347964; __utmc=90872199; __utmz=90872199.1516347964.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); wafcookie=a34cd89337d684080ade303438690bd3; wafverify=0b293a0aafe394d7feb4e23c300d618b; CNZZDATA1257545628=640992366-1516343148-http%253A%252F%252Flab.mkblog.cn%252F%7C1516343148; CNZZDATA1256443137=375579253-1516347691-http%253A%252F%252Flab.mkblog.cn%252F%7C1516347691; CNZZDATA1261525999=126801200-1515476375-%7C1516344357; Hm_lvt_ea4269d8a00e95fdb9ee61e3041a8f98=1516347982; Hm_lpvt_ea4269d8a00e95fdb9ee61e3041a8f98=1516348199")
    @FormUrlEncoded
    fun obtainSongWord(@Path("callback") callback: String, @FieldMap map: Map<String, String>): Observable<SongWordBean>

    /**
     * 网易云歌曲下载地址
     *
     * @param callback
     * @param map
     * @return
     */
    @POST("api.php/{callback}")
    @Headers("Host: lab.mkblog.cn", "Referer:http://qlab.mkblog.cn/music", "Cookie:UM_distinctid=160d9bcc565f8-05e85a9bbc6c66-35437c5c-1fa400-160d9bcc5662ee; Hm_lvt_6e8dac14399b608f633394093523542e=1515480945; wafenterurl=L211c2ljL2pzL2pxdWVyeS5taW4ubWFw; __utmt=1; __utma=90872199.51606438.1516347964.1516347964.1516347964.1; __utmb=90872199.1.10.1516347964; __utmc=90872199; __utmz=90872199.1516347964.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); wafcookie=a34cd89337d684080ade303438690bd3; wafverify=0b293a0aafe394d7feb4e23c300d618b; CNZZDATA1257545628=640992366-1516343148-http%253A%252F%252Flab.mkblog.cn%252F%7C1516343148; CNZZDATA1256443137=375579253-1516347691-http%253A%252F%252Flab.mkblog.cn%252F%7C1516347691; CNZZDATA1261525999=126801200-1515476375-%7C1516344357; Hm_lvt_ea4269d8a00e95fdb9ee61e3041a8f98=1516347982; Hm_lpvt_ea4269d8a00e95fdb9ee61e3041a8f98=1516348199")
    @FormUrlEncoded
    fun obtainSongPath(@Path("callback") callback: String, @FieldMap map: Map<String, String>): Observable<SongPathBean>

    /**
     * 歌曲封面图地址
     *
     * @param callback
     * @param map
     * @return
     */
    @POST("api.php/{callback}")
    @Headers("Host: lab.mkblog.cn", "Referer:http://qlab.mkblog.cn/music", "Cookie:UM_distinctid=160d9bcc565f8-05e85a9bbc6c66-35437c5c-1fa400-160d9bcc5662ee; Hm_lvt_6e8dac14399b608f633394093523542e=1515480945; wafenterurl=L211c2ljL2pzL2pxdWVyeS5taW4ubWFw; __utmt=1; __utma=90872199.51606438.1516347964.1516347964.1516347964.1; __utmb=90872199.1.10.1516347964; __utmc=90872199; __utmz=90872199.1516347964.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); wafcookie=a34cd89337d684080ade303438690bd3; wafverify=0b293a0aafe394d7feb4e23c300d618b; CNZZDATA1257545628=640992366-1516343148-http%253A%252F%252Flab.mkblog.cn%252F%7C1516343148; CNZZDATA1256443137=375579253-1516347691-http%253A%252F%252Flab.mkblog.cn%252F%7C1516347691; CNZZDATA1261525999=126801200-1515476375-%7C1516344357; Hm_lvt_ea4269d8a00e95fdb9ee61e3041a8f98=1516347982; Hm_lpvt_ea4269d8a00e95fdb9ee61e3041a8f98=1516348199")
    @FormUrlEncoded
    fun obtainSongPic(@Path("callback") callback: String, @FieldMap map: Map<String, String>): Observable<List<SearchResultSongBean>>

    /**
     * 歌曲搜索
     *
     * @param callback
     * @param map
     * @return
     */
    @POST("api.php/{callback}")
    @Headers("Host: lab.mkblog.cn", "Referer:http://qlab.mkblog.cn/music", "Cookie:UM_distinctid=160d9bcc565f8-05e85a9bbc6c66-35437c5c-1fa400-160d9bcc5662ee; Hm_lvt_6e8dac14399b608f633394093523542e=1515480945; wafenterurl=L211c2ljL2pzL2pxdWVyeS5taW4ubWFw; __utmt=1; __utma=90872199.51606438.1516347964.1516347964.1516347964.1; __utmb=90872199.1.10.1516347964; __utmc=90872199; __utmz=90872199.1516347964.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); wafcookie=a34cd89337d684080ade303438690bd3; wafverify=0b293a0aafe394d7feb4e23c300d618b; CNZZDATA1257545628=640992366-1516343148-http%253A%252F%252Flab.mkblog.cn%252F%7C1516343148; CNZZDATA1256443137=375579253-1516347691-http%253A%252F%252Flab.mkblog.cn%252F%7C1516347691; CNZZDATA1261525999=126801200-1515476375-%7C1516344357; Hm_lvt_ea4269d8a00e95fdb9ee61e3041a8f98=1516347982; Hm_lpvt_ea4269d8a00e95fdb9ee61e3041a8f98=1516348199")
    @FormUrlEncoded
    fun searchSongByKeyWord(@Path("callback") callback: String, @FieldMap map: Map<String, String>): Observable<List<SearchResultSongBean>>


    /**
     * 歌曲文件下载
     *
     * @param fileUrl
     * @return
     */
    @Streaming //大文件时要加不然会OOM
    @GET
    fun downloadSongFile(@Url fileUrl: String): Observable<ResponseBody>
}
