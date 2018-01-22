package cn.guluwa.gulumusic.ui.play;

import android.arch.lifecycle.ViewModel;

import cn.guluwa.gulumusic.data.total.SongsRepository;

/**
 * Created by guluwa on 2018/1/13.
 */

public class PlayViewModel extends ViewModel{

    private SongsRepository songsRepository = SongsRepository.getInstance();
}
