package com.limelight.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/** Hacker's Keyboard PC layouts, rendered for an in-app Moonlight input dock. */
public class GamingKeyboard extends LinearLayout {
    public interface Listener { void key(int keyCode, boolean down); }
    private final KeyboardKeyState state;
    private final List<KeyView> keys = new ArrayList<>();
    private final JSONArray normal, function;
    private boolean fn, caps;

    public GamingKeyboard(Context context, Listener listener) {
        super(context);
        state = new KeyboardKeyState(listener::key);
        normal = load("kbd_full");
        function = load("kbd_full_fn");
        setOrientation(VERTICAL);
        setBackgroundColor(Color.rgb(18, 19, 24));
        setPadding(dp(5), dp(5), dp(5), dp(5));
        setMotionEventSplittingEnabled(true);
        build();
    }

    private JSONArray load(String name) {
        try (InputStream in = getContext().getAssets().open("hackerskeyboard/" + name + ".json")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) != -1) out.write(buffer, 0, length);
            return new JSONArray(out.toString("UTF-8"));
        } catch (Exception e) { throw new IllegalStateException("Invalid bundled keyboard layout", e); }
    }

    private int dp(float value) { return Math.max(1, Math.round(value * getResources().getDisplayMetrics().density)); }

    private void build() {
        removeAllViews();
        keys.clear();
        JSONArray layout = fn ? function : normal;
        try {
            for (int r = 0; r < layout.length(); r++) {
                LinearLayout row = new LinearLayout(getContext());
                row.setMotionEventSplittingEnabled(true);
                addView(row, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
                JSONArray entries = layout.getJSONArray(r);
                for (int k = 0; k < entries.length(); k++) {
                    JSONObject entry = entries.getJSONObject(k);
                    KeyView key = new KeyView(entry);
                    LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, (float)entry.getDouble("weight"));
                    params.setMargins(dp(2), dp(3), dp(2), dp(3));
                    row.addView(key, params);
                    keys.add(key);
                }
            }
        } catch (Exception e) { throw new IllegalStateException("Invalid bundled keyboard key", e); }
    }

    private void refresh() { for (KeyView key : keys) key.invalidate(); }

    private final class KeyView extends View {
        final int code;
        final String label, hint;
        final boolean syntheticShift;
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boolean held;

        KeyView(JSONObject entry) throws Exception {
            super(GamingKeyboard.this.getContext());
            code = entry.getInt("code");
            label = entry.getString("label");
            hint = entry.getString("hint");
            syntheticShift = entry.getBoolean("shift");
            setContentDescription(code == -1 ? "Fn, toggle function keys" : label);
            setFocusable(false);
            setClickable(true);
        }

        private int accent() {
            if (code == -1 || (code >= 131 && code <= 143)) return Color.rgb(193, 167, 255);
            if (KeyboardKeyState.isModifier(code) || code == 115) return Color.rgb(132, 179, 255);
            if ((code >= 19 && code <= 23) || (code >= 92 && code <= 93) || (code >= 122 && code <= 124)) return Color.rgb(117, 223, 207);
            if (code == 66) return Color.rgb(156, 199, 255);
            return Color.rgb(241, 242, 247);
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            boolean active = held || state.isDown(code) || (code == -1 && fn) || (code == 115 && caps);
            boolean special = KeyboardKeyState.isModifier(code) || code == -1 || code == 115 || code == 66 || code == 111 || code == 67;
            paint.setColor(active ? Color.rgb(54, 81, 121) : special ? Color.rgb(31, 34, 43) : Color.rgb(54, 55, 62));
            float radius = Math.min(dp(7), getHeight() * .18f);
            canvas.drawRoundRect(0, 0, getWidth(), getHeight(), radius, radius, paint);
            if (active) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(1));
                paint.setColor(accent());
                canvas.drawRoundRect(dp(1), dp(1), getWidth()-dp(1), getHeight()-dp(1), radius, radius, paint);
                paint.setStyle(Paint.Style.FILL);
            }
            boolean shift = state.isDown(KeyEvent.KEYCODE_SHIFT_LEFT) || state.isDown(KeyEvent.KEYCODE_SHIFT_RIGHT);
            boolean letter = label.length() == 1 && Character.isLetter(label.charAt(0));
            String text = letter ? ((shift ^ caps) ? label.toUpperCase(java.util.Locale.US) : label.toLowerCase(java.util.Locale.US)) : shift && !hint.isEmpty() ? hint : label;
            paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(Math.min(dp(code == 62 ? 11 : text.length() > 2 ? 13 : 19), getHeight() * .40f));
            float available = Math.max(1, getWidth() - dp(6));
            if (paint.measureText(text) > available) paint.setTextSize(paint.getTextSize() * available / paint.measureText(text));
            paint.setColor(accent());
            Paint.FontMetrics metrics = paint.getFontMetrics();
            canvas.drawText(text, getWidth()/2f, (getHeight()-metrics.ascent-metrics.descent)/2f, paint);
            if (!hint.isEmpty() && !shift) {
                paint.setColor(Color.rgb(165, 170, 186));
                paint.setTextAlign(Paint.Align.RIGHT);
                paint.setTextSize(Math.min(dp(9), getHeight() * .20f));
                canvas.drawText(hint, getWidth()-dp(4), dp(2)-paint.getFontMetrics().ascent, paint);
            }
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    held = true;
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    if (code >= 0) {
                        // The second Shift is independent of a physically held left Shift.
                        if (syntheticShift) state.press(KeyEvent.KEYCODE_SHIFT_RIGHT);
                        state.press(code);
                    }
                    refresh();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (!held) return true;
                    held = false;
                    boolean cancelled = event.getActionMasked() == MotionEvent.ACTION_CANCEL;
                    if (code >= 0) {
                        state.release(code, cancelled);
                        if (syntheticShift) state.release(KeyEvent.KEYCODE_SHIFT_RIGHT, true);
                        if (code == 115 && !cancelled) caps = !caps;
                    } else if (!cancelled) {
                        releaseAll();
                        fn = !fn;
                        build();
                    }
                    if (!cancelled) performClick();
                    refresh();
                    return true;
                default: return true;
            }
        }
        @Override public boolean performClick() { super.performClick(); return true; }
    }

    public void releaseAll() {
        state.releaseAll();
        for (KeyView key : keys) key.held = false;
        refresh();
    }
    @Override protected void onDetachedFromWindow() {
        releaseAll();
        super.onDetachedFromWindow();
    }
}
