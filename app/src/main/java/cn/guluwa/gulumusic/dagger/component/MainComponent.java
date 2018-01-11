package cn.guluwa.gulumusic.dagger.component;

import cn.guluwa.gulumusic.dagger.module.MainModule;
import cn.guluwa.gulumusic.ui.MainActivity;
import dagger.Component;

/**
 * Created by guluwa on 2018/1/11.
 */

@Component(modules = MainModule.class)
public interface MainComponent {
    void inject(MainActivity mainActivity);
}
