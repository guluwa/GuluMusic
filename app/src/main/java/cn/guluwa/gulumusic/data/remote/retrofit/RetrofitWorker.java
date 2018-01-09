package cn.guluwa.gulumusic.data.remote.retrofit;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;


import com.readystatesoftware.chuck.ChuckInterceptor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.guluwa.gulumusic.utils.NetWorkUtil;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitWorker {

    private Map<Class, Object> apis = new HashMap<>();

    private Retrofit retrofit;

    public RetrofitWorker(Context context) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new ChuckInterceptor(context))//打印
                .addInterceptor(sLoggingInterceptor)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        boolean connected = NetWorkUtil.isNetConnected();
                        if (connected) {
                            return chain.proceed(chain.request());
                        } else {
                            throw new NoNetworkException();
                        }
                    }
                })
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response proceed = chain.proceed(chain.request());
                        if (proceed.code() == 404) {
                            throw new ServiceException();
                        } else {
                            return proceed;
                        }
                    }
                })
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        retrofit = new Retrofit.Builder()
                .baseUrl(Contacts.BASEURL)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public <T> T createApi(Class<T> service) {
        if (!apis.containsKey(service)) {
            T instance = retrofit.create(service);
            apis.put(service, instance);
        }

        return (T) apis.get(service);
    }

    /**
     * 打印返回的json数据拦截器
     */
    private static final Interceptor sLoggingInterceptor = new Interceptor() {

        @Override
        public Response intercept(Chain chain) throws IOException {
            final Request request = chain.request();
            Buffer requestBuffer = new Buffer();
            if (request.body() != null) {
                request.body().writeTo(requestBuffer);
            } else {
                Log.d("LogTAG", "request.body() == null");
            }
            //打印url信息
            Log.w("LogTAG", request.url() + (request.body() != null ? "?" + _parseParams(request.body(), requestBuffer) : ""));
            return chain.proceed(request);
        }
    };

    @NonNull
    private static String _parseParams(RequestBody body, Buffer requestBuffer) throws UnsupportedEncodingException {
        if (body.contentType() != null && !body.contentType().toString().contains("multipart")) {
            return URLDecoder.decode(requestBuffer.readUtf8(), "UTF-8");
        }
        return "null";
    }
}
