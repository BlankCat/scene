/*
 * Copyright (C) 2019 ByteDance Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytedance.scene.ui;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.group.UserVisibleHintGroupScene;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by JiangQi on 8/16/18.
 */
public class GroupSceneUIUtility {
    public static void setupWithBottomNavigationView(@NonNull final BottomNavigationView bottomNavigationView,
                                                     @NonNull final GroupScene groupScene,
                                                     @IdRes final int containerId,
                                                     @NonNull final SparseArrayCompat<Scene> children) {

        final List<String> menuIdList = new ArrayList<>();
        int menuSize = bottomNavigationView.getMenu().size();
        for (int i = 0; i < menuSize; i++) {
            menuIdList.add("" + bottomNavigationView.getMenu().getItem(i).getItemId());
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        item.setChecked(true);

                        String tag = "" + item.getItemId();

                        Scene scene = groupScene.findSceneByTag(tag);
                        if (scene == null) {
                            scene = children.get(item.getItemId());
                        }

                        if (!groupScene.isAdded(scene)) {
                            groupScene.add(containerId, scene, tag);
                        } else if (!groupScene.isShow(scene)) {
                            groupScene.show(scene);
                        }

                        for (int i = 0; i < menuIdList.size(); i++) {
                            Scene otherScene = groupScene.findSceneByTag(menuIdList.get(i));
                            if (otherScene != null && otherScene != scene && groupScene.isAdded(otherScene) && groupScene.isShow(otherScene)) {
                                groupScene.hide(otherScene);
                            }
                        }

                        return true;
                    }
                });

        String tag = "" + children.keyAt(0);
        Scene scene = groupScene.findSceneByTag(tag);
        if (scene == null) {
            scene = children.valueAt(0);
        }

        if (!groupScene.isAdded(scene)) {
            groupScene.add(containerId, scene, tag);
        } else if (!groupScene.isShow(scene)) {
            groupScene.show(scene);
        }

        bottomNavigationView.getMenu().findItem(children.keyAt(0)).setChecked(true);
    }

    public static void setupWithNavigationView(@NonNull final DrawerLayout drawerLayout,
                                               @NonNull final NavigationView navigationView,
                                               @NonNull final GroupScene groupScene,
                                               @IdRes final int containerId,
                                               @NonNull final SparseArrayCompat<Scene> children) {

        final List<String> menuIdList = new ArrayList<>();
        int menuSize = navigationView.getMenu().size();
        for (int i = 0; i < menuSize; i++) {
            menuIdList.add("" + navigationView.getMenu().getItem(i).getItemId());
        }

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        item.setChecked(true);
                        drawerLayout.closeDrawer(navigationView);
                        String tag = "" + item.getItemId();

                        Scene scene = groupScene.findSceneByTag(tag);
                        if (scene == null) {
                            scene = children.get(item.getItemId());
                        }

                        if (!groupScene.isAdded(scene)) {
                            groupScene.add(containerId, scene, tag);
                        } else if (!groupScene.isShow(scene)) {
                            groupScene.show(scene);
                        }

                        for (int i = 0; i < menuIdList.size(); i++) {
                            Scene otherScene = groupScene.findSceneByTag(menuIdList.get(i));
                            if (otherScene != null && otherScene != scene && groupScene.isAdded(otherScene) && groupScene.isShow(otherScene)) {
                                groupScene.hide(otherScene);
                            }
                        }
                        return true;
                    }
                });
        String tag = "" + children.keyAt(0);
        Scene scene = groupScene.findSceneByTag(tag);
        if (scene == null) {
            scene = children.valueAt(0);
        }

        if (!groupScene.isAdded(scene)) {
            groupScene.add(containerId, scene, tag);
        } else if (!groupScene.isShow(scene)) {
            groupScene.show(scene);
        }

        navigationView.getMenu().findItem(children.keyAt(0)).setChecked(true);
    }

    public static void setupWithViewPager(@NonNull final ViewPager viewPager,
                                          @NonNull final GroupScene groupScene,
                                          @NonNull final List<UserVisibleHintGroupScene> children) {
        if (viewPager.getAdapter() != null) {
            throw new IllegalArgumentException("ViewPager already have a adapter");
        }

        ScenePageAdapter scenePageAdapter = new ScenePageAdapter(groupScene) {

            @Override
            public int getCount() {
                return children.size();
            }

            @Override
            public UserVisibleHintGroupScene getItem(int position) {
                return children.get(position);
            }
        };
        viewPager.setAdapter(scenePageAdapter);
    }

    public static void setupWithViewPager(@NonNull final ViewPager viewPager,
                                          @NonNull final GroupScene groupScene,
                                          @NonNull final LinkedHashMap<String, UserVisibleHintGroupScene> children) {
        if (viewPager.getAdapter() != null) {
            throw new IllegalArgumentException("ViewPager already have a adapter");
        }
        final List<String> titleList = new ArrayList<>(children.keySet());
        final List<UserVisibleHintGroupScene> sceneList = new ArrayList<>();
        for (String key : titleList) {
            sceneList.add(children.get(key));
        }
        ScenePageAdapter scenePageAdapter = new ScenePageAdapter(groupScene) {

            @Override
            public int getCount() {
                return sceneList.size();
            }

            @Override
            public UserVisibleHintGroupScene getItem(int position) {
                return sceneList.get(position);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titleList.get(position);
            }
        };
        viewPager.setAdapter(scenePageAdapter);
    }
}
