package cn.guluwa.gulumusic.listener;

import cn.guluwa.gulumusic.data.bean.TracksBean;

/**
 * Created by guluwa on 2018/2/2.
 */

public interface OnSongStatusListener {

    /**
     * 加载
     */
    void loading();

    /**
     * 开始
     */
    void start();

    /**
     * 暂停
     */
    void pause();

    /**
     * 结束
     *
     * @param tracksBean
     */
    void end(TracksBean tracksBean);

    /**
     * 错误
     *
     * @param msg
     */
    void error(String msg);

    /**
     * 进度
     *
     * @param progress
     */
    void progress(int progress, int duration);
}
