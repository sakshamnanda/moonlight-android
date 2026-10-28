package com.limelight.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import java.util.HashMap;
import java.util.Map;

/** PC key events, including held keys and independent multi-touch presses. */
public class GamingKeyboard extends LinearLayout {
    public interface Listener { void key(int keyCode, boolean down); }
    private final Listener listener;
    private final Map<Button, Integer> held = new HashMap<>();

    public GamingKeyboard(Context context, Listener listener) {
        super(context);
        this.listener = listener;
        setOrientation(VERTICAL);
        setBackgroundColor(Color.rgb(16, 16, 16));
        setMotionEventSplittingEnabled(true);
        row(new String[]{"Esc","F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","F11","F12"},
                new int[]{111,131,132,133,134,135,136,137,138,139,140,141,142});
        row(new String[]{"`","1","2","3","4","5","6","7","8","9","0","-","=","⌫"},
                new int[]{68,8,9,10,11,12,13,14,15,16,7,69,70,67});
        row(new String[]{"Tab","Q","W","E","R","T","Y","U","I","O","P","[","]","\\"},
                new int[]{61,45,51,33,46,48,53,49,37,43,44,71,72,73});
        row(new String[]{"Ctrl","A","S","D","F","G","H","J","K","L",";","'","Enter"},
                new int[]{113,29,47,32,34,35,36,38,39,40,74,75,66});
        row(new String[]{"Shift","Z","X","C","V","B","N","M",",",".","/","↑","Shift"},
                new int[]{59,54,52,31,50,30,42,41,55,56,76,19,60});
        row(new String[]{"Ctrl","Alt","Win","Space","←","↓","→"},
                new int[]{113,57,117,62,21,20,22});
    }

    private void row(String[] labels, int[] codes) {
        LinearLayout row = new LinearLayout(getContext());
        row.setMotionEventSplittingEnabled(true);
        addView(row, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        for (int i = 0; i < labels.length; i++) {
            final int code = codes[i];
            Button button = new Button(getContext());
            button.setText(labels[i]);
            button.setTextSize(12);
            button.setTextColor(Color.WHITE);
            button.setBackgroundTintList(new android.content.res.ColorStateList(
                    new int[][] { new int[] { android.R.attr.state_pressed }, new int[] {} },
                    new int[] { Color.rgb(65, 100, 125), Color.rgb(48, 48, 48) }));
            button.setAllCaps(false);
            button.setPadding(0, 0, 0, 0);
            button.setMinWidth(0);
            button.setMinimumWidth(0);
            button.setMinHeight(0);
            button.setMinimumHeight(0);
            button.setFocusable(false);
            button.setContentDescription(labels[i]);
            LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT,
                    code == KeyEvent.KEYCODE_SPACE ? 5 : 1);
            params.setMargins(1, 1, 1, 1);
            row.addView(button, params);
            button.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!held.containsKey(button)) {
                            boolean alreadyDown = held.containsValue(code);
                            held.put(button, code);
                            if (!alreadyDown) listener.key(code, true);
                        }
                        button.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        Integer released = held.remove(button);
                        if (released != null && !held.containsValue(released)) listener.key(released, false);
                        button.setPressed(false);
                        break;
                }
                return true;
            });
        }
    }

    public void releaseAll() {
        for (int code : new java.util.HashSet<>(held.values())) listener.key(code, false);
        for (Button button : held.keySet()) button.setPressed(false);
        held.clear();
    }

    @Override protected void onDetachedFromWindow() {
        releaseAll();
        super.onDetachedFromWindow();
    }
}
