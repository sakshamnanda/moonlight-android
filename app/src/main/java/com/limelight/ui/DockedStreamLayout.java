package com.limelight.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import com.limelight.R;

/** Keeps the surface in root coordinates so absolute mouse/touch mapping remains valid. */
public class DockedStreamLayout extends FrameLayout {
    private boolean controllerEnabled, keyboardEnabled, keyboardVisible, pictureInPicture;
    private double aspectRatio = 16.0 / 9.0;

    public DockedStreamLayout(Context context, AttributeSet attrs) { super(context, attrs); }

    public void configure(boolean controller, boolean keyboard, double ratio) {
        controllerEnabled = controller;
        keyboardEnabled = keyboard;
        aspectRatio = ratio;
        findViewById(R.id.dockKeyboardToggle).setVisibility(keyboard ? VISIBLE : GONE);
        updateVisibility();
    }

    public void toggleKeyboard() {
        if (!keyboardEnabled) return;
        keyboardVisible = !keyboardVisible;
        ((Button) findViewById(R.id.dockKeyboardToggle)).setText(keyboardVisible ?
                R.string.dock_close_keyboard : R.string.dock_keyboard);
        updateVisibility();
    }

    public boolean isKeyboardVisible() { return keyboardVisible; }

    public void setPictureInPicture(boolean enabled) {
        pictureInPicture = enabled;
        findViewById(R.id.dockKeyboardToggle).setVisibility(keyboardEnabled && !enabled ? VISIBLE : GONE);
        updateVisibility();
    }

    private void updateVisibility() {
        findViewById(R.id.inputDock).setVisibility(keyboardVisible && !pictureInPicture ? VISIBLE : GONE);
        findViewById(R.id.controllerDock).setVisibility(controllerEnabled && !keyboardVisible && !pictureInPicture ? VISIBLE : GONE);
        requestLayout();
    }

    private boolean isDocked() { return !pictureInPicture && (controllerEnabled || keyboardVisible); }
    private int toolbarHeight() { return keyboardEnabled ? Math.round(40 * getResources().getDisplayMetrics().density) : 0; }
    private int videoHeight(int width, int height) {
        return DockedStreamGeometry.videoHeight(width, height, toolbarHeight(), aspectRatio);
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (!isDocked()) return;
        int width = getMeasuredWidth(), height = getMeasuredHeight();
        int videoHeight = videoHeight(width, height);
        findViewById(R.id.surfaceView).measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(videoHeight, MeasureSpec.EXACTLY));
        int panelHeight = Math.max(1, height - videoHeight - toolbarHeight());
        for (int id : new int[] { R.id.controllerDock, R.id.inputDock }) {
            findViewById(id).measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(panelHeight, MeasureSpec.EXACTLY));
        }
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isDocked()) return;
        int width = right - left, height = bottom - top;
        View stream = findViewById(R.id.surfaceView);
        int streamLeft = (width - stream.getMeasuredWidth()) / 2;
        stream.layout(streamLeft, 0, streamLeft + stream.getMeasuredWidth(), stream.getMeasuredHeight());
        int panelTop = videoHeight(width, height);
        for (int id : new int[] { R.id.controllerDock, R.id.inputDock }) {
            findViewById(id).layout(0, panelTop, width, height - toolbarHeight());
        }
    }
}
