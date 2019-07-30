package com.bytedance.scenedemo.case0;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimatorExecutor;
import com.bytedance.scene.animation.TransitionUtils;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 9/5/18.
 */
public class Case2Scene extends GroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        Button button = new Button(getActivity());
        button.setText("Push 1个，Pop 100次，超过总数");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(EmptyScene.class, null, new PushOptions.Builder().build());
                for (int i = 0; i < 100; i++) {
                    getNavigationScene().pop();
                }
            }
        });

        button = new Button(getActivity());
        button.setText("Push 1个，Pop 100个，直接超过总数");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(EmptyScene.class, null, new PushOptions.Builder().build());
                getNavigationScene().pop(new PopOptions.Builder().setPopUtilPredicate(new PopOptions.CountUtilPredicate(100)).build());
            }
        });

        button = new Button(getActivity());
        button.setText("onStop 的时候 Pop 100个超过总数再Push1个（还没试过）");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "按Home切到后台，5秒后切回来", Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 100; i++) {
                            getNavigationScene().pop();
                        }
                        getNavigationScene().push(EmptyScene.class, null, new PushOptions.Builder().build());
                        Toast.makeText(getActivity(), "可以切回来", Toast.LENGTH_SHORT).show();
                    }
                }, 3000);
            }
        });


        layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));
        layout.setFitsSystemWindows(true);
        return layout;
    }

    private static class AAA extends NavigationAnimatorExecutor {
        @Override
        public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
            return true;
        }

        @Override
        protected boolean disableConfigAnimationDuration() {
            return true;
        }

        @NonNull
        @Override
        protected Animator onPushAnimator(AnimationInfo from, final AnimationInfo to) {
            final View fromView = from.mSceneView;
            final View toView = to.mSceneView;

            ValueAnimator fromAlphaAnimator = ObjectAnimator.ofFloat(fromView, View.ALPHA, 1.0f, 1.0f);//之前是0.7，但是动画后面会露出NavigationScene的背景色白色很怪异
            fromAlphaAnimator.setInterpolator(new FastOutSlowInInterpolator());
            fromAlphaAnimator.setDuration(120 * 20);

            ValueAnimator toAlphaAnimator = ObjectAnimator.ofFloat(toView, View.ALPHA, 0.0f, 1.0f);
            toAlphaAnimator.setInterpolator(new DecelerateInterpolator(2));
            toAlphaAnimator.setDuration(120 * 20);

            ValueAnimator toTranslateAnimator = ObjectAnimator.ofFloat(toView, View.TRANSLATION_Y, 0.08f * toView.getHeight(), 0);
            toTranslateAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
            toTranslateAnimator.setDuration(200 * 20);
            return TransitionUtils.mergeAnimators(fromAlphaAnimator, toAlphaAnimator, toTranslateAnimator);
        }

        @NonNull
        @Override
        protected Animator onPopAnimator(final AnimationInfo fromInfo, final AnimationInfo toInfo) {
            final View toView = toInfo.mSceneView;
            final View fromView = fromInfo.mSceneView;

            ValueAnimator fromAlphaAnimator = ObjectAnimator.ofFloat(fromView, View.ALPHA, 1.0f, 0.0f);
            fromAlphaAnimator.setInterpolator(new LinearInterpolator());
            fromAlphaAnimator.setDuration(150 * 20);
            fromAlphaAnimator.setStartDelay(50 * 20);

            ValueAnimator fromTranslateAnimator = ObjectAnimator.ofFloat(fromView, View.TRANSLATION_Y, 0, 0.08f * toView.getHeight());
            fromTranslateAnimator.setInterpolator(new AccelerateInterpolator(2));
            fromTranslateAnimator.setDuration(200 * 20);

            ValueAnimator toAlphaAnimator = ObjectAnimator.ofFloat(toView, View.ALPHA, 0.7f, 1.0f);
            toAlphaAnimator.setInterpolator(new LinearOutSlowInInterpolator());
            toAlphaAnimator.setDuration(20 * 20);
            return TransitionUtils.mergeAnimators(fromAlphaAnimator, fromTranslateAnimator, toAlphaAnimator);
        }
    }

    public static class EmptyScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);

            TextView textView = new TextView(getActivity());
            textView.setText(getNavigationScene().getStackHistory());
            layout.addView(textView);

            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));

            layout.setFitsSystemWindows(true);

            return layout;
        }
    }
}
