import com.limelight.ui.GamingKeyboardGeometry;
import com.limelight.ui.DockedStreamGeometry;

public class GamingKeyboardGeometryCheck {
    public static void main(String[] args) {
        float[][] rows = {{.64f,1,1,1,1,1,1,1,1,1,1,1,1,1.36f},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1}, {1.5f,1,1,1,1,1,1,1,1,1,1,1,1.5f},
                {2,1,1,1,1,1,1,1,1,1,1,1,1}, {1.5f,1,1,1,4.5f,1,1,1,1,1}};
        for (int[] size : new int[][] {{1536,1384,2},{2160,1856,3},{2520,1080,3},{800,600,1},{640,360,1}}) {
            int video = DockedStreamGeometry.keyboardVideoHeight(size[0],size[1],16.0/9,size[2]);
            int height = size[1] - video;
            int[][] cells = GamingKeyboardGeometry.cells(size[0],height,size[2],rows);
            for (int i=0;i<cells.length;i++) {
                int[] a=cells[i];
                if (a[0]<0 || a[1]<0 || a[2]>size[0] || a[3]>height || a[2]-a[0]<20 || a[3]-a[1]<24)
                    throw new AssertionError("Unusable key bounds");
                for (int j=0;j<i;j++) {
                    int[] b=cells[j];
                    if (a[0]<b[2] && b[0]<a[2] && a[1]<b[3] && b[1]<a[3]) throw new AssertionError("Overlapping keys");
                }
            }
        }
        System.out.println("Responsive keyboard geometry checks passed");
    }
}
