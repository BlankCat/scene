package com.bytedance.scene;

import com.bytedance.scene.navigation.NavigationScene;

public interface SceneDelegate {
    boolean onBackPressed();

    NavigationScene getNavigationScene();

    //第一次绑定到Fragment和Fragment销毁恢复，创建NavigationScene的时机是不一样的，这个接口可以保证什么时候NavigationScene对象创建
    //但是这个对象并没有走玩生命周期，只是个最原始的Java对象
    void setNavigationSceneAvailableCallback(NavigationSceneAvailableCallback callback);
}