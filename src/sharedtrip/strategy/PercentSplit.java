package sharedtrip.strategy;

import java.util.List;
import java.util.Map;

public class PercentSplit extends AbstractSplit {
    @Override public String getType() { return "percent"; }
    @Override public String getLabel() { return "ตามเปอร์เซ็นต์"; }

    @Override
    public String validate(double amount, List<String> participants, Map<String, Double> values) {
        String base = super.validate(amount, participants, values);
        if (base != null) return base;
        double total = 0;
        for (String p : participants) {
            if (valueOf(values, p) < 0) return "เปอร์เซ็นต์ต้องไม่ติดลบ";
            total += valueOf(values, p);
        }
        if (Math.abs(total - 100) > 0.01)
            return "เปอร์เซ็นต์รวมต้องเท่ากับ 100 (ตอนนี้ " + Math.round(total * 100) / 100.0 + ")";
        return null;
    }

    @Override
    public Map<String, Double> split(double amount, List<String> participants, Map<String, Double> values) {
        double[] w = new double[participants.size()];
        for (int i = 0; i < w.length; i++) w[i] = valueOf(values, participants.get(i));
        return distribute(toSatang(amount), participants, w);
    }
}
