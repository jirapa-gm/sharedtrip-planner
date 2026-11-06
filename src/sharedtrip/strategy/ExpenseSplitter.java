package sharedtrip.strategy;

import java.util.List;
import java.util.Map;

import sharedtrip.composite.Expense;

// Context ของ Strategy: ถือ strategy ไว้หนึ่งตัว เปลี่ยนได้ตอนรันด้วย setStrategy()
public class ExpenseSplitter {
    public static final SplitStrategy[] STRATEGIES = { new EqualSplit(), new PercentSplit(), new ExactSplit() };

    private SplitStrategy strategy;

    public ExpenseSplitter(SplitStrategy strategy) {
        this.strategy = strategy;
    }

    public ExpenseSplitter setStrategy(SplitStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public SplitStrategy getStrategy() { return strategy; }

    public String validate(double amount, List<String> participants, Map<String, Double> values) {
        return strategy.validate(amount, participants, values);
    }

    public Map<String, Double> split(double amount, List<String> participants, Map<String, Double> values) {
        return strategy.split(amount, participants, values);
    }

    public static SplitStrategy getStrategy(String type) {
        for (SplitStrategy s : STRATEGIES) if (s.getType().equals(type)) return s;
        return STRATEGIES[0];
    }

    // ส่วนแบ่งของ expense ที่บันทึกไว้แล้ว
    public static Map<String, Double> sharesOf(Expense e) {
        return new ExpenseSplitter(getStrategy(e.getSplitType()))
                .split(e.getAmount(), e.getParticipants(), e.getValues());
    }
}
