package com.bytedance.scenedemo.navigation.performance;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.bytedance.scene.Scene;

/**
 * Created by JiangQi on 8/21/18.
 */
public class EmptyScene extends Scene {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = new View(getActivity());
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                long a = System.currentTimeMillis() - PerformanceDemo.startTimestamp;
                Toast.makeText(getActivity(), "启动耗时 " + a + " ms", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        view.setBackgroundColor(Color.RED);
        return view;
    }
}
