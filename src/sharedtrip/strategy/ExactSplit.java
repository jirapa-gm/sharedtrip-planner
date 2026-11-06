package sharedtrip.strategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExactSplit extends AbstractSplit {
    @Override public String getType() { return "exact"; }
    @Override public String getLabel() { return "ระบุเอง"; }

    @Override
    public String validate(double amount, List<String> participants, Map<String, Double> values) {
        String base = super.validate(amount, participants, values);
        if (base != null) return base;
        long total = 0;
        for (String p : participants) {
            if (valueOf(values, p) < 0) return "จำนวนเงินต้องไม่ติดลบ";
            total += toSatang(valueOf(values, p));
        }
        if (total != toSatang(amount))
            return String.format("ยอดที่ระบุรวม ฿%,.2f ไม่เท่ากับ ฿%,.2f", toBaht(total), amount);
        return null;
    }

    @Override
    public Map<String, Double> split(double amount, List<String> participants, Map<String, Double> values) {
        Map<String, Double> out = new LinkedHashMap<>();
        for (String p : participants) out.put(p, toBaht(toSatang(valueOf(values, p))));
        return out;
    }
}
