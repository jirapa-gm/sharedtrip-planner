package sharedtrip.settlement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import sharedtrip.composite.Expense;
import sharedtrip.composite.Trip;
import sharedtrip.strategy.ExpenseSplitter;

// คำนวณยอดคงเหลือของแต่ละคน และสรุปว่าใครต้องโอนให้ใคร
public final class Settlement {
    private Settlement() {}

    public static class Balance {
        public final String name;
        public final double paid, owed, net; // net > 0 ได้เงินคืน, net < 0 ต้องจ่ายเพิ่ม

        Balance(String name, long paid, long owed) {
            this.name = name;
            this.paid = paid / 100.0;
            this.owed = owed / 100.0;
            this.net = (paid - owed) / 100.0;
        }
    }

    public static class Transfer {
        public final String from, to;
        public final double amount;

        Transfer(String from, String to, long amount) {
            this.from = from;
            this.to = to;
            this.amount = amount / 100.0;
        }
    }

    private static long satang(double baht) {
        return Math.round(baht * 100);
    }

    public static List<Balance> getBalances(Trip trip) {
        Map<String, long[]> rows = new LinkedHashMap<>(); // [paid, owed]
        for (String m : trip.getMembers()) rows.put(m, new long[2]);
        for (Expense e : trip.getExpenses()) {
            rows.computeIfAbsent(e.getPaidBy(), k -> new long[2])[0] += satang(e.getAmount());
            for (Map.Entry<String, Double> s : ExpenseSplitter.sharesOf(e).entrySet())
                rows.computeIfAbsent(s.getKey(), k -> new long[2])[1] += satang(s.getValue());
        }
        List<Balance> out = new ArrayList<>();
        for (Map.Entry<String, long[]> r : rows.entrySet())
            out.add(new Balance(r.getKey(), r.getValue()[0], r.getValue()[1]));
        return out;
    }

    // Greedy: จับคู่คนที่ติดเงินมากที่สุดกับคนที่ต้องได้คืนมากที่สุด โอนไม่เกิน (จำนวนคน - 1) ครั้ง
    public static List<Transfer> getTransfers(List<Balance> balances) {
        List<Object[]> debtors = new ArrayList<>(), creditors = new ArrayList<>();
        for (Balance b : balances) {
            if (b.net < 0) debtors.add(new Object[] { b.name, satang(-b.net) });
            if (b.net > 0) creditors.add(new Object[] { b.name, satang(b.net) });
        }
        debtors.sort((a, b) -> Long.compare((long) b[1], (long) a[1]));
        creditors.sort((a, b) -> Long.compare((long) b[1], (long) a[1]));
        List<Transfer> out = new ArrayList<>();
        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            long pay = Math.min((long) debtors.get(i)[1], (long) creditors.get(j)[1]);
            if (pay > 0) out.add(new Transfer((String) debtors.get(i)[0], (String) creditors.get(j)[0], pay));
            debtors.get(i)[1] = (long) debtors.get(i)[1] - pay;
            creditors.get(j)[1] = (long) creditors.get(j)[1] - pay;
            if ((long) debtors.get(i)[1] == 0) i++;
            if ((long) creditors.get(j)[1] == 0) j++;
        }
        return out;
    }
}
