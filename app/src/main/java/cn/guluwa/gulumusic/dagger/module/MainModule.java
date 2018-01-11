package cn.guluwa.gulumusic.dagger.module;

import cn.guluwa.gulumusic.ui.MainActivity;
import dagger.Module;
import dagger.Provides;

/**
 * Created by guluwa on 2018/1/11.
 */

@Module
public class MainModule {
    private final MainActivity mView;

    public MainModule(MainActivity view) {
        mView = view;
    }

    @Provides
    MainActivity provideMainView() {
        return mView;
    }
}
