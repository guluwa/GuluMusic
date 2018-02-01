package cn.guluwa.gulumusic.listener;

/**
 * Created by guluwa on 2018/1/19.
 */

public interface OnResultListener<T> {
    void success(T result);

    void failed(String error);
}
