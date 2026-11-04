import com.limelight.ui.KeyboardKeyState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class KeyboardKeyStateCheck {
    private static final List<String> events = new ArrayList<>();
    private static final KeyboardKeyState keys = new KeyboardKeyState((code, down) -> events.add(code + (down ? "+" : "-")));
    private static void expect(String... expected) {
        if (!events.equals(Arrays.asList(expected))) throw new AssertionError(events + " != " + Arrays.toString(expected));
        events.clear();
    }
    public static void main(String[] args) {
        // One-finger Ctrl+C, then an unmodified key.
        keys.press(113); keys.release(113, false);
        keys.press(31); keys.release(31, false);
        keys.press(29); keys.release(29, false);
        expect("113+", "31+", "31-", "113-", "29+", "29-");
        // A held modifier supports multiple keys and releases only after the finger lifts.
        keys.press(59); keys.press(29); keys.release(29, false);
        keys.press(30); keys.release(30, false); keys.release(59, false);
        expect("59+", "29+", "29-", "30+", "30-", "59-");
        // Either Shift button may be held without prematurely releasing the other.
        keys.press(59); keys.press(59); keys.press(29);
        keys.release(59, false); keys.release(29, false); keys.release(59, false);
        expect("59+", "29+", "29-", "59-");
        // Tap a latched modifier again to unlock it.
        keys.press(57); keys.release(57, false); keys.press(57); keys.release(57, false);
        expect("57+", "57-");
        // A cancelled touch never latches; hiding a keyboard releases everything.
        keys.press(113); keys.release(113, true); expect("113+", "113-");
        keys.press(113); keys.release(113, false); keys.press(29); keys.releaseAll();
        expect("113+", "29+", "29-", "113-");
        // Shift pressed after a held letter is still a chord, not a sticky tap.
        keys.press(29); keys.press(59); keys.release(59, false); keys.release(29, false);
        expect("29+", "59+", "59-", "29-");
        // Fn symbols synthesize right Shift without cancelling held left Shift.
        keys.press(59); keys.press(60); keys.press(15);
        keys.release(15, false); keys.release(60, true); keys.release(59, false);
        expect("59+", "60+", "15+", "15-", "60-", "59-");
        System.out.println("Keyboard modifier and release checks passed");
    }
}
