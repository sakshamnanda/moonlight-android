import com.limelight.binding.input.virtual_controller.DockedControllerLayout;
import com.limelight.binding.input.virtual_controller.DockedControllerLayout.Bounds;
import com.limelight.binding.input.virtual_controller.DockedControllerLayout.Slot;
import java.util.ArrayList;
import java.util.Map;

public class DockedControllerLayoutCheck {
    public static void main(String[] args) {
        int[][] sizes = {{1536,520,2}, {2160,753,3}, {2520,356,3}, {800,240,1}, {320,180,1}};
        for (int[] size : sizes) {
            for (boolean swapped : new boolean[]{false, true}) {
            Map<Slot, Bounds> slots = DockedControllerLayout.calculate(size[0], size[1], size[2], swapped);
            ArrayList<Bounds> bounds = new ArrayList<>(slots.values());
            for (Bounds a : bounds) {
                if (a.width <= 0 || a.height <= 0 || a.x < 0 || a.y < 0 ||
                        a.x + a.width > size[0] || a.y + a.height > size[1])
                    throw new AssertionError("Clipped control at " + size[0] + "x" + size[1]);
                for (Bounds b : bounds) {
                    if (a != b && a.x < b.x + b.width && a.x + a.width > b.x &&
                            a.y < b.y + b.height && a.y + a.height > b.y)
                        throw new AssertionError("Overlapping controls");
                }
            }
            if (!(slots.get(Slot.LT).x < slots.get(Slot.LB).x &&
                    slots.get(Slot.LEFT_STICK).x < slots.get(Slot.RIGHT_STICK).x &&
                    slots.get(Slot.Y).y < slots.get(Slot.X).y &&
                    slots.get(Slot.X).y < slots.get(Slot.A).y))
                throw new AssertionError("Reference arrangement changed");
            if ((slots.get(Slot.LEFT_STICK).x < slots.get(Slot.DPAD).x) == swapped)
                throw new AssertionError("Wrong D-pad/stick order");
            if (slots.get(Slot.LEFT_STICK).width <= slots.get(Slot.DPAD).width)
                throw new AssertionError("Sticks must be larger than the original D-pad size");
            }
        }
        System.out.println("PASS: controller bounds and separation across five panel sizes");
    }
}
