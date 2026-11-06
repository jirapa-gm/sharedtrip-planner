package sharedtrip.strategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// ส่วนที่ทุก strategy ใช้ร่วมกัน คำนวณเป็น "สตางค์" (จำนวนเต็ม) เศษจากการปัดกระจายทีละ 1 สตางค์
public abstract class AbstractSplit implements SplitStrategy {

    static long toSatang(double baht) {
        return Math.round(baht * 100);
    }

    static double toBaht(long satang) {
        return satang / 100.0;
    }

    protected static double valueOf(Map<String, Double> values, String p) {
        Double v = values == null ? null : values.get(p);
        return v == null ? 0 : v;
    }

    @Override
    public String validate(double amount, List<String> participants, Map<String, Double> values) {
        if (!(amount > 0)) return "จำนวนเงินต้องมากกว่า 0";
        if (participants.isEmpty()) return "เลือกคนที่ร่วมจ่ายอย่างน้อย 1 คน";
        return null;
    }

    // แจกยอดตามสัดส่วน weights ให้ยอดรวมตรงพอดี
    protected static Map<String, Double> distribute(long total, List<String> people, double[] weights) {
        double sumW = 0;
        for (double w : weights) sumW += w;
        if (sumW <= 0) { weights = new double[people.size()]; java.util.Arrays.fill(weights, 1); sumW = people.size(); }
        double[] raw = new double[people.size()];
        long[] floored = new long[people.size()];
        long used = 0;
        for (int i = 0; i < raw.length; i++) {
            raw[i] = total * weights[i] / sumW;
            floored[i] = (long) Math.floor(raw[i]);
            used += floored[i];
        }
        long remainder = total - used;
        Integer[] order = new Integer[raw.length];
        for (int i = 0; i < order.length; i++) order[i] = i;
        final double[] r = raw;
        java.util.Arrays.sort(order, (a, b) -> Double.compare(r[b] - Math.floor(r[b]), r[a] - Math.floor(r[a])));
        for (int k = 0; k < order.length && remainder > 0; k++, remainder--) floored[order[k]]++;
        Map<String, Double> out = new LinkedHashMap<>();
        for (int i = 0; i < people.size(); i++) out.put(people.get(i), toBaht(floored[i]));
        return out;
    }
}
