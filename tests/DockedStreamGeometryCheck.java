import com.limelight.ui.DockedStreamGeometry;

public class DockedStreamGeometryCheck {
    public static void main(String[] args) {
        if (DockedStreamGeometry.videoHeight(1536, 1384, 60, 16.0 / 9) != 864)
            throw new AssertionError("Reference screenshot must retain full-width 16:9 video");
        int[][] sizes = {{2160,1968,100}, {1968,2160,100}, {2520,1080,100},
                {1080,2520,100}, {800,600,40}, {320,180,40}};
        for (int[] size : sizes) {
            int video = DockedStreamGeometry.videoHeight(size[0], size[1], size[2], 16.0 / 9);
            int panel = size[1] - size[2] - video;
            if (video <= 0 || panel <= 0 || video * 16.0 / 9 > size[0] + 1)
                throw new AssertionError("Stream or controls clipped");
            if (panel < (size[1] - size[2]) * 0.33 - 1)
                throw new AssertionError("Controls too small");
        }
        System.out.println("PASS: reference layout and six fold/cover/window sizes");
    }
}
