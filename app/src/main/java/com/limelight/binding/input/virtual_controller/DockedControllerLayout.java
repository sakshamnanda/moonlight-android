package com.limelight.binding.input.virtual_controller;

import java.util.EnumMap;
import java.util.Map;

/** Geometry for the controller panel, independent of display resolution and video size. */
public final class DockedControllerLayout {
    public enum Slot { LT, LB, BACK, GUIDE, START, RT, RB, DPAD, LEFT_STICK, RIGHT_STICK, A, B, X, Y, SWAP }

    public static final class Bounds {
        public final int x, y, width, height;
        private Bounds(float x, float y, float width, float height) {
            this.x = Math.round(x);
            this.y = Math.round(y);
            this.width = Math.max(1, Math.round(width));
            this.height = Math.max(1, Math.round(height));
        }
    }

    private DockedControllerLayout() {}

    public static Map<Slot, Bounds> calculate(int width, int height, float density) {
        return calculate(width, height, density, false);
    }

    public static Map<Slot, Bounds> calculate(int width, int height, float density, boolean dpadOutside) {
        Map<Slot, Bounds> slots = new EnumMap<>(Slot.class);
        float edge = Math.min(12 * density, Math.min(width, height) * 0.035f);
        float gap = Math.min(10 * density, Math.min(width * 0.01f, height * 0.025f));
        float rowY = Math.min(40 * density, height * 0.12f);
        float rowHeight = Math.min(44 * density, height * 0.16f);
        float rowWidth = Math.min(rowHeight * 1.5f, (width - 2 * edge - 6 * gap) / 7);
        Slot[] row = {Slot.LT, Slot.LB, Slot.BACK, Slot.GUIDE, Slot.START, Slot.RT, Slot.RB};
        float step = (width - 2 * edge - rowWidth) / 6;
        for (int i = 0; i < row.length; i++) {
            slots.put(row[i], new Bounds(edge + i * step, rowY, rowWidth, rowHeight));
        }

        float bodyTop = rowY + rowHeight + gap;
        float bodyHeight = Math.max(1, height - edge - bodyTop);
        float centerY = bodyTop + bodyHeight / 2;
        float dpadSize = Math.min(112 * density, Math.min(bodyHeight * 0.70f, width * 0.16f));
        float stickSize = Math.min(134 * density, Math.min(bodyHeight * 0.84f, width * 0.185f));
        circle(slots, Slot.DPAD, width * (dpadOutside ? 0.11f : 0.32f), centerY, dpadSize);
        circle(slots, Slot.LEFT_STICK, width * (dpadOutside ? 0.32f : 0.11f), centerY, stickSize);
        circle(slots, Slot.RIGHT_STICK, width * 0.64f, centerY, stickSize);
        float swapWidth = Math.min(82 * density, width * 0.11f);
        float swapHeight = Math.min(36 * density, bodyHeight * 0.24f);
        slots.put(Slot.SWAP, new Bounds(width * .48f - swapWidth / 2,
                centerY - swapHeight / 2, swapWidth, swapHeight));

        float faceSize = Math.min(48 * density, Math.min(bodyHeight * 0.26f, width * 0.065f));
        float spread = Math.min(bodyHeight * 0.31f, width * 0.073f);
        float faceX = width * 0.86f;
        circle(slots, Slot.Y, faceX, centerY - spread, faceSize);
        circle(slots, Slot.X, faceX - spread, centerY, faceSize);
        circle(slots, Slot.B, faceX + spread, centerY, faceSize);
        circle(slots, Slot.A, faceX, centerY + spread, faceSize);
        return slots;
    }

    private static void circle(Map<Slot, Bounds> slots, Slot slot, float x, float y, float size) {
        slots.put(slot, new Bounds(x - size / 2, y - size / 2, size, size));
    }
}
