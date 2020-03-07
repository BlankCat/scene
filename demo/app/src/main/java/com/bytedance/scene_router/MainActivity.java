package com.bytedance.scene_router;

import android.support.annotation.NonNull;

import com.bytedance.scene.Scene;
import com.bytedance.scene.ui.SceneActivity;

public class MainActivity extends SceneActivity {
    @NonNull
    @Override
    protected Class<? extends Scene> getHomeSceneClass() {
        return MainScene.class;
    }

    @Override
    protected boolean supportRestore() {
        return false;
    }
}
