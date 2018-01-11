package cn.guluwa.gulumusic.data.remote.retrofit.exception;

import java.io.IOException;

/**
 * Created by 俊康 on 2017/9/27.
 */

public class BaseException extends IOException {

    String msg;
    int code;
    String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
