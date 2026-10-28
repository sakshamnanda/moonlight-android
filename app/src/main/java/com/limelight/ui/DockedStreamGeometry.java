package com.limelight.ui;

/** Android-independent sizing shared by the dock layout and its regression check. */
public final class DockedStreamGeometry {
    private DockedStreamGeometry() {}

    public static int videoHeight(int width, int height, int toolbarHeight, double aspectRatio) {
        // Use the full stream width whenever possible, reserving at least one third
        // of available height for usable controls on narrow/cover displays.
        int available = Math.max(1, height - toolbarHeight);
        return Math.max(1, Math.min((int) (width / aspectRatio), (int) (available * 0.67)));
    }
}
