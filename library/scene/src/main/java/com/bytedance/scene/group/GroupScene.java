package com.bytedance.scene.group;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.interfaces.SceneGroup;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.NonNullPair;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.ThreadUtility;

import java.util.ArrayList;
import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 8/1/18.
 */
//todo 一定要支持tranaction，这样可以做到add又hide，那么不触发onResume，不然ViewPager很难办
public abstract class GroupScene extends Scene implements SceneGroup {
    private final GroupSceneManager mGroupSceneManager = new GroupSceneManager();
    private final List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> mLifecycleCallbacks = new ArrayList<>();

    public GroupSceneManager getGroupSceneManager() {
        return mGroupSceneManager;
    }

    @NonNull
    @Override
    public final List<Scene> getSceneList() {
        return this.mGroupSceneManager.getChildSceneList();
    }

    @Override
    public final void beginTransaction() {
        this.mGroupSceneManager.beginTransaction();
    }

    @Override
    public final void commitTransaction() {
        this.mGroupSceneManager.commitTransaction();
    }

    @Override
    public final void add(@IdRes final int viewId, @NonNull final Scene scene, @NonNull final String tag) {
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("tag can't be empty");
        }

        //todo 问题，在GroupScene的时候执行了2次find+add，那么其实操作都是缓存的，所以每次find都找不到，以为没有，add进来
        //等真正执行add的时候发现冲突了？怎么办？Fragment也经常有这个问题

        if (findSceneByTag(tag) != null) {
            throw new IllegalArgumentException("already have a Scene with tag " + tag);
        }

        if (getNavigationScene() != null
                && ((NavigationScene) getNavigationScene()).isSupportRestore()
                && !SceneInstanceUtility.isSupportRestore(scene)) {
            throw new IllegalArgumentException("Scene must have only empty argument constructor when support restore");
        }

        mGroupSceneManager.add(viewId, scene, tag);
    }

    @Override
    public final <T extends Scene> T findSceneByTag(@NonNull String tag) {
        if (tag == null) {
            return null;
        }

        GroupRecord record = this.mGroupSceneManager.findByTag(tag);
        if (record != null) {
            return (T) record.scene;
        } else {
            return null;
        }
    }

    @Override
    public final void remove(@NonNull Scene scene) {
        this.mGroupSceneManager.remove(scene);
    }

    @Override
    public final void hide(@NonNull Scene scene) {
        if (scene.getState() == State.STOPPED) {
            return;
        }
        this.mGroupSceneManager.hide(scene);
    }

    @Override
    public final void show(@NonNull Scene scene) {
        if (scene.getState() == State.RESUMED) {
            return;
        }
        this.mGroupSceneManager.show(scene);
    }

    @Override
    public final boolean isAdded(@NonNull Scene scene) {
        return this.mGroupSceneManager.findByScene(scene) != null;
    }

    @Override
    public final boolean isShow(@NonNull Scene scene) {
        GroupRecord record = this.mGroupSceneManager.findByScene(scene);
        if (record == null) {
            return false;
        }
        return !record.isHidden;
    }

    @NonNull
    public final ViewGroup findContainerById(int viewId) {
        ViewGroup viewGroup = (ViewGroup) getView().findViewById(viewId);
        if (viewGroup == null) {
            try {
                String viewIdName = getResources().getResourceName(viewId);
                throw new IllegalArgumentException(" " + viewIdName + " view not found");
            } catch (Resources.NotFoundException exception) {
                throw new IllegalArgumentException(" " + viewId + " view not found");
            }
        }
        return viewGroup;
    }

    public GroupScene() {
        mGroupSceneManager.setGroupScene(this);
    }

    @Override
    public final void dispatchCreateView(@Nullable Bundle savedInstanceState, @NonNull ViewGroup container) {
        super.dispatchCreateView(savedInstanceState, container);
        View view = getView();
        if (!(view instanceof ViewGroup)) {
            throw new IllegalArgumentException("GroupScene onCreateView view must be ViewGroup");
        }
        this.mGroupSceneManager.setView((ViewGroup) getView());
    }

    @NonNull
    public abstract ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container,
                                           @Nullable Bundle savedInstanceState);

    //todo 把restore提早到dispatchCreate
    @Override
    public void dispatchActivityCreated(@Nullable Bundle savedInstanceState) {
        super.dispatchActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            this.mGroupSceneManager.restoreFromBundle(requireActivity(), savedInstanceState);
        } else {
            dispatchChildrenState(State.STOPPED, null);
        }
    }

    @CallSuper
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        List<Scene> childSceneList = this.mGroupSceneManager.getChildSceneList();
        for (int i = 0; i <= childSceneList.size() - 1; i++) {
            Scene scene = childSceneList.get(i);
            scene.onViewStateRestored(savedInstanceState);
        }
    }

    @CallSuper
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mGroupSceneManager.saveToBundle(outState);
    }

    @Override
    public void dispatchStart() {
        super.dispatchStart();
        dispatchVisibleChildrenState(State.STARTED);
    }

    @Override
    public void dispatchResume() {
        super.dispatchResume();
        dispatchVisibleChildrenState(State.RESUMED);
    }

    @Override
    public void dispatchPause() {
        dispatchVisibleChildrenState(State.STARTED);
        super.dispatchPause();
    }

    @Override
    public void dispatchStop() {
        dispatchVisibleChildrenState(State.STOPPED);
        super.dispatchStop();
    }

    @Override
    public void dispatchDestroyView() {
        dispatchChildrenState(State.NONE, null);
        super.dispatchDestroyView();
    }

    private void dispatchChildrenState(State state, @Nullable Bundle savedInstanceState) {
        this.mGroupSceneManager.dispatchChildrenState(state, savedInstanceState);
    }

    private void dispatchVisibleChildrenState(State state) {
        this.mGroupSceneManager.dispatchVisibleChildrenState(state);
    }

    public <T extends Scene> T createOrReuse(String tag, Creator<T> creator) {
        Scene scene = findSceneByTag(tag);
        if (scene == null) {
            scene = creator.call();
        }
        return (T) scene;
    }

    @Override
    public void registerChildSceneLifecycleCallbacks(@NonNull ChildSceneLifecycleCallbacks cb, boolean recursive) {
        ThreadUtility.checkUIThread();
        this.mLifecycleCallbacks.add(NonNullPair.create(cb, recursive));
    }

    @Override
    public void unregisterChildSceneLifecycleCallbacks(@NonNull ChildSceneLifecycleCallbacks cb) {
        ThreadUtility.checkUIThread();
        NonNullPair<ChildSceneLifecycleCallbacks, Boolean> target = null;
        for (int i = 0, N = this.mLifecycleCallbacks.size(); i < N; i++) {
            if (this.mLifecycleCallbacks.get(i).first == cb) {
                target = this.mLifecycleCallbacks.get(i);
                break;
            }
        }
        if (target != null) {
            this.mLifecycleCallbacks.remove(target);
        }
    }

    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnSceneCreated(@NonNull Scene scene, Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
                if (directChild || pair.second) {
                    pair.first.onSceneCreated(scene, savedInstanceState);
                }
            }
        }

        super.dispatchOnSceneCreated(scene, savedInstanceState, directChild);
    }

    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnSceneStarted(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
                if (directChild || pair.second) {
                    pair.first.onSceneStarted(scene);
                }
            }
        }

        super.dispatchOnSceneStarted(scene, directChild);
    }

    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnSceneResumed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
                if (directChild || pair.second) {
                    pair.first.onSceneResumed(scene);
                }
            }
        }

        super.dispatchOnSceneResumed(scene, directChild);
    }

    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnSceneStopped(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
                if (directChild || pair.second) {
                    pair.first.onSceneStopped(scene);
                }
            }
        }

        super.dispatchOnSceneStopped(scene, directChild);
    }

    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnScenePaused(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
                if (directChild || pair.second) {
                    pair.first.onScenePaused(scene);
                }
            }
        }

        super.dispatchOnScenePaused(scene, directChild);
    }

    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnSceneSaveInstanceState(@NonNull Scene scene, Bundle outState, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
                if (directChild || pair.second) {
                    pair.first.onSceneSaveInstanceState(scene, outState);
                }
            }
        }

        super.dispatchOnSceneSaveInstanceState(scene, outState, directChild);
    }

    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnSceneDestroyed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
                if (directChild || pair.second) {
                    pair.first.onSceneDestroyed(scene);
                }
            }
        }

        super.dispatchOnSceneDestroyed(scene, directChild);
    }
}
