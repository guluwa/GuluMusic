package cn.guluwa.gulumusic.data.remote.retrofit.exception;

/**
 * Created by 俊康 on 2017/8/8.
 */

public class NoNetworkException extends BaseException {

    private static final long serialVersionUID = -347636838706746243L;

    public NoNetworkException(String msg) {
        this.msg = msg;
    }
}
