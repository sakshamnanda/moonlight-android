package com.limelight.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Reference-counted PC keys with tap-to-latch and hold-to-chord modifiers. */
public final class KeyboardKeyState {
    public interface Output { void key(int code, boolean down); }
    private final Output output;
    private final Map<Integer, Integer> held = new HashMap<>();
    private final Set<Integer> latched = new HashSet<>(), used = new HashSet<>(), unlocking = new HashSet<>();

    public KeyboardKeyState(Output output) { this.output = output; }
    public static boolean isModifier(int code) {
        return code == 59 || code == 60 || code == 57 || code == 58 ||
                code == 113 || code == 114 || code == 117 || code == 118;
    }
    public boolean isDown(int code) { return held.containsKey(code) || latched.contains(code); }
    public void press(int code) {
        boolean down = isDown(code);
        int count = held.containsKey(code) ? held.get(code) : 0;
        held.put(code, count + 1);
        if (isModifier(code) && count == 0) {
            used.remove(code);
            if (latched.contains(code)) unlocking.add(code);
            for (int other : held.keySet()) if (!isModifier(other)) used.add(code);
        }
        if (!isModifier(code)) {
            for (int other : held.keySet()) if (isModifier(other)) used.add(other);
        }
        if (!down) output.key(code, true);
    }
    public void release(int code, boolean cancelled) {
        Integer count = held.get(code);
        if (count == null) return;
        if (count > 1) { held.put(code, count - 1); return; }
        held.remove(code);
        if (isModifier(code)) {
            if (unlocking.remove(code) || cancelled || used.remove(code)) {
                latched.remove(code);
                output.key(code, false);
            } else latched.add(code);
        } else {
            output.key(code, false);
            boolean chordHeld = false;
            for (int other : held.keySet()) if (!isModifier(other)) chordHeld = true;
            if (!chordHeld) {
                for (int modifier : latched) {
                    if (!held.containsKey(modifier)) output.key(modifier, false);
                }
                latched.clear();
            }
        }
    }
    public void releaseAll() {
        Set<Integer> down = new HashSet<>(held.keySet());
        down.addAll(latched);
        // Ordinary keys go up before their modifiers.
        for (int code : down) if (!isModifier(code)) output.key(code, false);
        for (int code : down) if (isModifier(code)) output.key(code, false);
        held.clear(); latched.clear(); used.clear(); unlocking.clear();
    }
}
