package com.bytedance.scene.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;
import android.util.TypedValue;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 7/30/18.
 */
/** @hide */
@RestrictTo(LIBRARY_GROUP)
public class SceneBackgroundLayout extends NoneTouchFrameLayout {
    public SceneBackgroundLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public SceneBackgroundLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SceneBackgroundLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SceneBackgroundLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        TypedValue a = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            int color = a.data;
            setBackgroundDrawable(new ColorDrawable(color));
        } else {
            // windowBackground is not a color, probably a drawable
            Drawable d = getResources().getDrawable(a.resourceId);
            setBackgroundDrawable(d);
        }
    }
}
