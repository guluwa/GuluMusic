package cn.guluwa.gulumusic.listener;

public interface OnResultListener<T> {
    void success(T result);
    void failed(String error);
}
