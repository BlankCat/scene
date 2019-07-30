package com.bytedance.scenedemo.group.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 11/6/18.
 * 演示怎么手动管理Scene的生命周期，用普通的View托管整个Scene
 */
public class TestSceneToViewActivity extends Activity {
    private TestSceneDelegateToViewView viewView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewView = new TestSceneDelegateToViewView(this);
        setContentView(viewView);
        viewView.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewView.onDestroyView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        viewView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        NavigationScene navigationScene = viewView.getNavigationScene();
        if (navigationScene != null && navigationScene.onBackPressed()) {
            //empty
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        NavigationScene navigationScene = viewView.getNavigationScene();
        if (navigationScene != null) {
            navigationScene.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        NavigationScene navigationScene = viewView.getNavigationScene();
        if (navigationScene != null) {
            navigationScene.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
