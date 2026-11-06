package sharedtrip.strategy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EqualSplit extends AbstractSplit {
    @Override public String getType() { return "equal"; }
    @Override public String getLabel() { return "หารเท่ากัน"; }

    @Override
    public Map<String, Double> split(double amount, List<String> participants, Map<String, Double> values) {
        double[] w = new double[participants.size()];
        Arrays.fill(w, 1);
        return distribute(toSatang(amount), participants, w);
    }
}
