package sharedtrip.composite;

// สร้าง id ไม่ซ้ำให้ object ต่าง ๆ
public final class Ids {
    private static int counter = 0;

    private Ids() {}

    public static synchronized String next() {
        return Long.toString(System.currentTimeMillis(), 36) + "-" + (counter++);
    }
}
