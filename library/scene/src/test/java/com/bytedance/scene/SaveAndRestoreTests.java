package com.bytedance.scene;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SaveAndRestoreTests {

    @Test
    public void test() {
        Bundle bundle = new Bundle();
        TestScene previousRootScene = null;

        {
            SceneLifecycleManager sceneLifecycleManager = new SceneLifecycleManager();
            NavigationScene navigationScene = new NavigationScene();
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();
            NavigationSceneOptions options = new NavigationSceneOptions(TestScene.class);
            navigationScene.setArguments(options.toBundle());

            NavigationScene.NavigationSceneHost navigationSceneHost = new NavigationScene.NavigationSceneHost() {
                @Override
                public boolean isSupportRestore() {
                    return true;
                }

                @Override
                public void startActivityForResult(@NonNull Intent intent, int requestCode) {

                }

                @Override
                public void requestPermissions(@NonNull String[] permissions, int requestCode) {

                }
            };

            Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                }
            };

            navigationScene.setDefaultNavigationAnimationExecutor(new NoAnimationExecutor());

            sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                    navigationScene, navigationSceneHost, rootScopeFactory,
                    null, null);

            sceneLifecycleManager.onStart();
            sceneLifecycleManager.onResume();

            previousRootScene = (TestScene) navigationScene.getCurrentScene();
            previousRootScene.setValue("Test");
            previousRootScene.getCheckBox().setChecked(true);

            sceneLifecycleManager.onSaveInstanceState(bundle);
            sceneLifecycleManager.onPause();
            sceneLifecycleManager.onStop();
            sceneLifecycleManager.onDestroyView();
        }

        assertTrue(bundle.size() > 0);

        TestScene newRootScene = null;

        {
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();
            NavigationSceneOptions options = new NavigationSceneOptions(TestScene.class);
            NavigationScene navigationScene = new NavigationScene();
            navigationScene.setArguments(options.toBundle());

            NavigationScene.NavigationSceneHost navigationSceneHost = new NavigationScene.NavigationSceneHost() {
                @Override
                public boolean isSupportRestore() {
                    return true;
                }

                @Override
                public void startActivityForResult(@NonNull Intent intent, int requestCode) {

                }

                @Override
                public void requestPermissions(@NonNull String[] permissions, int requestCode) {

                }
            };

            Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                }
            };

            navigationScene.setDefaultNavigationAnimationExecutor(new NoAnimationExecutor());
            SceneLifecycleManager sceneLifecycleManager = new SceneLifecycleManager();
            sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                    navigationScene, navigationSceneHost, rootScopeFactory,
                    null, bundle);
            newRootScene = (TestScene) navigationScene.getCurrentScene();
        }

        assertNotNull(previousRootScene);
        assertNotNull(newRootScene);
        assertNotSame(newRootScene, previousRootScene);
        assertTrue(newRootScene.getCheckBox().isChecked());//check View state restore
        assertEquals("Test", newRootScene.mValue);//check onSaveInstanceState and onViewStateRestored
    }

    public static NavigationScene createNavigationScene(Scene rootScene) {
        SceneLifecycleManager sceneLifecycleManager = new SceneLifecycleManager();
        NavigationScene navigationScene = new NavigationScene();
        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();
        NavigationSceneOptions options = new NavigationSceneOptions(TestScene.class);
        navigationScene.setArguments(options.toBundle());

        NavigationScene.NavigationSceneHost navigationSceneHost = new NavigationScene.NavigationSceneHost() {
            @Override
            public boolean isSupportRestore() {
                return true;
            }

            @Override
            public void startActivityForResult(@NonNull Intent intent, int requestCode) {

            }

            @Override
            public void requestPermissions(@NonNull String[] permissions, int requestCode) {

            }
        };

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };

        navigationScene.setDefaultNavigationAnimationExecutor(null);

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, navigationSceneHost, rootScopeFactory,
                null, null);

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        return navigationScene;
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashAnonymousClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        });//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashLocalClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);

        class LocalClass extends Scene {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        }

        navigationScene.push(new LocalClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPrivateMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PrivateMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPackageMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PackageMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashProtectedMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new ProtectedMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPublicMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PublicMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPackageStaticMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PackageStaticMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashProtectedStaticMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new ProtectedStaticMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPrivateStaticMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PrivateStaticMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPackageClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PackageClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPublicStaticMemberNotEmptyParamsConstructorClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PublicStaticMemberNotEmptyParamsConstructorClass(1));//crash
    }

    @Test
    public void testPublicStaticMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PublicStaticMemberClass());//ok
    }

    @Test
    public void testPublicClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PublicClassScene());//ok
    }

    class PackageMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    protected class ProtectedMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    private class PrivateMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public class PublicMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    static class PackageStaticMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    protected static class ProtectedStaticMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    private static class PrivateStaticMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public static class PublicStaticMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public static class PublicStaticMemberNotEmptyParamsConstructorClass extends Scene {
        public PublicStaticMemberNotEmptyParamsConstructorClass(int value) {

        }

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public static class TestScene extends Scene {
        public final int mId;
        private String mValue;

        public TestScene() {
            mId = 1;
        }

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new CheckBox(requireSceneContext());
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            view.setId(mId);
        }

        public void setValue(String value) {
            this.mValue = value;
        }

        public CheckBox getCheckBox() {
            return (CheckBox) getView();
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString("value", mValue);
        }

        @Override
        public void onViewStateRestored(@NonNull Bundle savedInstanceState) {
            super.onViewStateRestored(savedInstanceState);
            this.mValue = savedInstanceState.getString("value");
        }
    }
}

class PackageClass extends Scene {
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireSceneContext());
    }
}