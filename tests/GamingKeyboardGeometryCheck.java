import com.limelight.ui.GamingKeyboardGeometry;
import com.limelight.ui.DockedStreamGeometry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class GamingKeyboardGeometryCheck {
    // Read the shipped assets: exercise the actual normal and Fn rows, including split keys.
    static float[][] rows(String name) throws Exception {
        String json = Files.readString(Path.of("app/src/main/assets/hackerskeyboard/" + name + ".json"));
        var rowMatcher = Pattern.compile("\\[\\s*\\{(.*?)\\}\\s*\\]", Pattern.DOTALL).matcher(json);
        var result = new ArrayList<float[]>();
        while (rowMatcher.find()) {
            var weights = new ArrayList<Float>();
            var matcher = Pattern.compile("\"weight\"\\s*:\\s*([0-9.]+)").matcher(rowMatcher.group());
            while (matcher.find()) weights.add(Float.parseFloat(matcher.group(1)));
            float[] row = new float[weights.size()];
            for (int i=0;i<row.length;i++) row[i]=weights.get(i);
            result.add(row);
        }
        if (result.size()!=5) throw new AssertionError("Expected five asset rows");
        return result.toArray(new float[0][]);
    }
    static void close(int a, int b) {
        if (Math.abs(a-b)>1) throw new AssertionError("Column drift: " + a + " versus " + b);
    }
    public static void main(String[] args) throws Exception {
        for (String name : new String[]{"kbd_full", "kbd_full_fn"}) {
            float[][] rows = rows(name);
            for (int[] size : new int[][] {{1536,1384,2},{2160,1856,3},{2520,1080,3},{800,600,1},{640,360,1}}) {
                int video = DockedStreamGeometry.keyboardVideoHeight(size[0],size[1],16.0/9,size[2]);
                int height = size[1] - video;
                int[][] cells = GamingKeyboardGeometry.cells(size[0],height,size[2],rows);
                int[] offsets = new int[5];
                for (int r=1;r<5;r++) offsets[r]=offsets[r-1]+rows[r-1].length;
                for (int i=0;i<cells.length;i++) {
                    int[] a=cells[i];
                    if (a[0]<0 || a[1]<0 || a[2]>size[0] || a[3]>height || a[2]-a[0]<20 || a[3]-a[1]<24)
                        throw new AssertionError("Unusable key bounds");
                    for (int j=0;j<i;j++) {
                        int[] b=cells[j];
                        if (a[0]<b[2] && b[0]<a[2] && a[1]<b[3] && b[1]<a[3]) throw new AssertionError("Overlapping keys");
                    }
                }
                if (name.endsWith("_fn")) {
                    // These columns must line up even where the last slot splits into Ins/Del.
                    for (int r=1;r<4;r++) for (int k=0;k<10;k++) {
                        close(cells[k][0],cells[offsets[r]+k][0]);
                        close(cells[k][2],cells[offsets[r]+k][2]);
                    }
                } else {
                    // Same-width character slots stay equal across differently sized rows.
                    int qWidth=cells[offsets[1]+1][2]-cells[offsets[1]+1][0];
                    for (int r=0;r<4;r++) for(int k=0;k<rows[r].length;k++) {
                        if (Math.abs(rows[r][k]-100f/14)<.001f)
                            close(qWidth,cells[offsets[r]+k][2]-cells[offsets[r]+k][0]);
                    }
                }
                // Up sits directly above Down on both pages, with matching key edges.
                int up=offsets[4]-2, down=cells.length-2;
                close(cells[up][0],cells[down][0]);
                close(cells[up][2],cells[down][2]);
            }
        }
        System.out.println("Normal and Fn asset alignment checks passed");
    }
}
