package cn.guluwa.gulumusic.data.remote.retrofit.exception;

/**
 * Created by 俊康 on 2017/9/27.
 */

public class ServiceException extends BaseException {

    private static final long serialVersionUID = 2211853108336484888L;

    public ServiceException(String msg) {
        this.msg = msg;
    }
}
