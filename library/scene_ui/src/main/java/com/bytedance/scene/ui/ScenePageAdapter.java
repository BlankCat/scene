package com.bytedance.scene.ui;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.group.UserVisibleHintGroupScene;

/**
 * Created by JiangQi on 7/30/18.
 */
public abstract class ScenePageAdapter extends PagerAdapter {
    private GroupScene mGroupScene;
    private UserVisibleHintGroupScene mCurrentScene;

    public ScenePageAdapter(GroupScene scene) {
        this.mGroupScene = scene;
    }

    public abstract UserVisibleHintGroupScene getItem(int position);

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        long itemId = getItemId(position);
        String name = makeFragmentName(itemId);

        ViewPager viewPager = (ViewPager) container;
        UserVisibleHintGroupScene scene = mGroupScene.findSceneByTag(name);
        if (scene != null) {
            //因为show是直接执行，所以对setUserVisibleHint的操作必须之前执行
            configSceneUserVisibleHint(viewPager, scene, position);
            mGroupScene.show(scene);
        } else {
            scene = getItem(position);
            configSceneUserVisibleHint(viewPager, scene, position);
            mGroupScene.add(container.getId(), scene, name);
        }
        return scene;
    }

    private void configSceneUserVisibleHint(ViewPager viewPager, UserVisibleHintGroupScene scene, int position) {
        if (mCurrentScene == null) {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem == position) {
                mCurrentScene = scene;
            }
        }

        boolean visible = scene == mCurrentScene;
        if (scene.getUserVisibleHint() != visible) {
            scene.setUserVisibleHint(visible);
        }
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
        UserVisibleHintGroupScene scene = (UserVisibleHintGroupScene) object;
        if (scene != mCurrentScene) {
            if (mCurrentScene != null) {
                mCurrentScene.setUserVisibleHint(false);
            }
            if (scene != null) {
                scene.setUserVisibleHint(true);
            }
            mCurrentScene = scene;
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Scene scene = (Scene) object;
        mGroupScene.remove(scene);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return ((Scene) object).getView() == view;
    }

    public long getItemId(int position) {
        return position;
    }

    private static String makeFragmentName(long id) {
        return "android:switcher:" + id;
    }
}
