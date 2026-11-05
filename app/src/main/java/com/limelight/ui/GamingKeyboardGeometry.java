package com.limelight.ui;

/** Responsive cell grid; all rows span the same width without margin-weight drift. */
public final class GamingKeyboardGeometry {
    private GamingKeyboardGeometry() {}
    public static int[][] cells(int width, int height, float density, float[][] weights) {
        int count = 0;
        for (float[] row : weights) count += row.length;
        int[][] result = new int[count][4];
        int padding = Math.max(1, Math.round(Math.min(5 * density, Math.min(width * .006f, height * .015f))));
        int gap = Math.max(2, Math.round(Math.min(4 * density, Math.min(width * .004f, height * .014f))));
        int index = 0;
        for (int r = 0; r < weights.length; r++) {
            float total = 0;
            for (float weight : weights[r]) total += weight;
            int usable = width - 2 * padding - gap * (weights[r].length - 1);
            float cumulative = 0;
            int top = padding + Math.round((height - 2f * padding) * r / weights.length);
            int bottom = padding + Math.round((height - 2f * padding) * (r + 1) / weights.length);
            for (int k = 0; k < weights[r].length; k++) {
                int left = padding + gap * k + Math.round(usable * cumulative / total);
                cumulative += weights[r][k];
                int right = padding + gap * k + Math.round(usable * cumulative / total);
                result[index++] = new int[] {left, top + gap / 2, right, bottom - (gap + 1) / 2};
            }
        }
        return result;
    }
}
