package cn.guluwa.gulumusic.data.bean;

/**
 * Created by guluwa on 2018/1/3.
 */

public class ViewDataBean<T> {

    public final PageStatus status;
    public final T data;
    public final Throwable throwable;

    public ViewDataBean(PageStatus status, T data, Throwable throwable) {
        this.status = status;
        this.data = data;
        this.throwable = throwable;
    }

    public static <T> ViewDataBean<T> content(T data) {
        return new ViewDataBean<>(PageStatus.Content, data, null);
    }

    public static <T> ViewDataBean<T> error(T data,Throwable throwable){
        return new ViewDataBean<>(PageStatus.Error,data,throwable);
    }

    public static  <T>ViewDataBean<T> error(Throwable throwable){
        return error(null,throwable);
    }

    public static <T> ViewDataBean<T> empty(T data){
        return new ViewDataBean<>(PageStatus.Empty,data,null);
    }

    public static  <T>ViewDataBean<T> empty(){
        return empty(null);
    }

    public static <T> ViewDataBean<T> loading(T data){
        return new ViewDataBean<>(PageStatus.Loading,data,null);
    }

    public static  <T>ViewDataBean<T> loading(){
        return loading(null);
    }
}
