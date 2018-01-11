package cn.guluwa.gulumusic.data.remote.retrofit;

import java.util.Map;

import cn.guluwa.gulumusic.data.bean.PlayListBean;
import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by guluwa on 2018/1/11.
 */

public interface ApiService {

    /**
     * 网易云热门
     *
     * @param map
     * @return
     */
    @POST("api.php/{callback}")
    @Headers({"Host: lab.mkblog.cn","Referer: http://lab.mkblog.cn/music/","Cookie: UM_distinctid=160d9bd40bf339-052d1aa48a2631-42564130-1fa400-160d9bd40c05d6; CNZZDATA1261525999=1628019787-1515476375-%7C1515636330"})
    @FormUrlEncoded
    Observable<PlayListBean> obtainNetCloudHot(@Path("callback") String callback, @FieldMap Map<String, Object> map);
}
