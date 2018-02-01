package cn.guluwa.gulumusic.listener;

import cn.guluwa.gulumusic.data.bean.TracksBean;

/**
 * Created by guluwa on 2018/2/1.
 */

public interface OnSongFinishListener {

    void finish(TracksBean tracksBean);
}
