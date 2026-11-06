package sharedtrip.ui;

import static sharedtrip.ui.Theme.*;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import sharedtrip.composite.Expense;
import sharedtrip.composite.Trip;
import sharedtrip.observer.TripObserver;
import sharedtrip.observer.TripStore;
import sharedtrip.strategy.ExpenseSplitter;
import sharedtrip.strategy.SplitStrategy;

// แท็บค่าใช้จ่าย: จดค่าใช้จ่ายแล้วเลือกวิธีหาร (Strategy)
public class ExpensePanel extends JPanel implements TripObserver {
    private final TripStore store = TripStore.getInstance();
    private final ExpenseSplitter splitter = new ExpenseSplitter(ExpenseSplitter.STRATEGIES[0]);

    private final JTextField title = field(16), amount = field(8);
    private final JComboBox<String> paidBy = new JComboBox<>();
    private final JPanel strategyBar = row(8);
    private final JPanel chips = row(10);
    private final JLabel preview = label(" ", Font.PLAIN, 14);
    private final JLabel error = label(" ", Font.BOLD, 14);
    private final JPanel list = vbox();
    private final JLabel total = label(" ", Font.BOLD, 17);

    private final Map<String, JCheckBox> checks = new LinkedHashMap<>();
    private final Map<String, JTextField> weights = new LinkedHashMap<>();
    private List<String> members = new ArrayList<>();

    public ExpensePanel() {
        super(new BorderLayout(0, 16));
        setOpaque(false);
        error.setForeground(RED_TEXT);
        paidBy.setBackground(PAPER);

        Card form = new Card(PINK, new BorderLayout());
        JPanel f = vbox();
        f.add(left(label("จดค่าใช้จ่าย", Font.BOLD, 19)));
        JPanel top = new JPanel(new GridLayout(2, 3, 10, 2));
        top.setOpaque(false);
        top.add(label("จ่ายค่าอะไร", Font.BOLD, 14));
        top.add(label("เท่าไร (บาท)", Font.BOLD, 14));
        top.add(label("ใครออกเงิน", Font.BOLD, 14));
        top.add(title); top.add(amount); top.add(paidBy);
        f.add(left(top));
        f.add(Box.createVerticalStrut(8));
        f.add(left(label("หารแบบไหน", Font.BOLD, 14)));
        for (SplitStrategy s : ExpenseSplitter.STRATEGIES) {
            PillButton b = new PillButton(s.getLabel(), PAPER);
            b.addActionListener(e -> { splitter.setStrategy(s); refreshForm(); });
            strategyBar.add(b);
        }
        f.add(left(strategyBar));
        f.add(Box.createVerticalStrut(6));
        f.add(left(label("หารกับใครบ้าง", Font.BOLD, 14)));
        f.add(left(chips));
        f.add(Box.createVerticalStrut(6));
        f.add(left(preview));
        PillButton save = new PillButton("บันทึกค่าใช้จ่าย", PINK_STRONG);
        save.addActionListener(e -> save());
        f.add(left(row(12, save, error)));
        form.add(f);

        Card listCard = new Card(PAPER, new BorderLayout(0, 10));
        listCard.add(label("รายการค่าใช้จ่าย", Font.BOLD, 19), BorderLayout.NORTH);
        listCard.add(list, BorderLayout.CENTER);
        listCard.add(total, BorderLayout.SOUTH);

        add(form, BorderLayout.NORTH);
        add(listCard, BorderLayout.CENTER);

        amount.getDocument().addDocumentListener(onChange(this::updatePreview));
        store.subscribe(this);
    }

    private static DocumentListener onChange(Runnable r) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        };
    }

    private double amountValue() {
        try { return Double.parseDouble(amount.getText().trim().replace(",", "")); } catch (NumberFormatException e) { return 0; }
    }

    private List<String> participants() {
        List<String> out = new ArrayList<>();
        for (String m : members) if (checks.get(m).isSelected()) out.add(m);
        return out;
    }

    private Map<String, Double> values() {
        Map<String, Double> out = new LinkedHashMap<>();
        for (String p : participants()) {
            try { out.put(p, Double.parseDouble(weights.get(p).getText().trim().replace(",", ""))); }
            catch (NumberFormatException e) { out.put(p, 0.0); }
        }
        return out;
    }

    private boolean needsValue() {
        return !splitter.getStrategy().getType().equals("equal");
    }

    // สร้างฟอร์มใหม่เมื่อรายชื่อสมาชิกเปลี่ยน
    private void rebuildMembers(List<String> m) {
        members = new ArrayList<>(m);
        paidBy.removeAllItems();
        for (String x : members) paidBy.addItem(x);
        checks.clear();
        weights.clear();
        for (String x : members) {
            JCheckBox c = new JCheckBox(x, true);
            c.setOpaque(false);
            c.setForeground(INK);
            c.setFont(font(Font.BOLD, 15));
            c.addActionListener(e -> refreshForm());
            checks.put(x, c);
            JTextField w = field(4);
            w.getDocument().addDocumentListener(onChange(this::updatePreview));
            weights.put(x, w);
        }
        refreshForm();
    }

    private void refreshForm() {
        Component[] bs = strategyBar.getComponents();
        for (int i = 0; i < bs.length; i++) {
            boolean on = ExpenseSplitter.STRATEGIES[i] == splitter.getStrategy();
            bs[i].setBackground(on ? YELLOW : PAPER);
            ((PillButton) bs[i]).setSelectedLook(on);
        }
        chips.removeAll();
        for (String m : members) {
            Card chip = new Card(checks.get(m).isSelected() ? MINT : PAPER, new FlowLayout(FlowLayout.LEFT, 6, 0));
            chip.setBorder(new CardBorder(28, 3, 4));
            chip.add(new Avatar(m, members));
            chip.add(checks.get(m));
            if (needsValue() && checks.get(m).isSelected()) {
                chip.add(weights.get(m));
                chip.add(hint(splitter.getStrategy().getType().equals("percent") ? "%" : "บาท"));
            }
            chips.add(chip);
        }
        updatePreview();
        chips.revalidate();
        chips.repaint();
    }

    private void updatePreview() {
        double amt = amountValue();
        String problem = splitter.validate(amt, participants(), values());
        if (problem == null) {
            StringBuilder sb = new StringBuilder("<html>แต่ละคนจ่าย: ");
            for (Map.Entry<String, Double> e : splitter.split(amt, participants(), values()).entrySet())
                sb.append(e.getKey()).append(" <b>").append(baht(e.getValue())).append("</b> &nbsp; ");
            preview.setText(sb.append("</html>").toString());
            preview.setForeground(INK);
        } else {
            preview.setText(amount.getText().isBlank() ? " " : problem);
            preview.setForeground(RED_TEXT);
        }
    }

    private void save() {
        if (title.getText().isBlank()) { error.setText("กรอกชื่อรายการ เช่น ค่าที่พัก"); return; }
        double amt = amountValue();
        List<String> ps = participants();
        Map<String, Double> vs = needsValue() ? values() : new LinkedHashMap<>();
        String problem = splitter.validate(amt, ps, vs);
        if (problem != null) { error.setText(problem); return; }
        Expense e = new Expense(null, title.getText().trim(), amt, (String) paidBy.getSelectedItem(), ps,
                splitter.getStrategy().getType(), vs);
        store.update(t -> t.addExpense(e));
        title.setText("");
        amount.setText("");
        for (JTextField w : weights.values()) w.setText("");
        error.setText(" ");
    }

    @Override
    public void onTripChanged(Trip trip) {
        list.removeAll();
        if (trip == null) { revalidate(); repaint(); return; }
        if (!trip.getMembers().equals(members)) rebuildMembers(trip.getMembers());

        List<Expense> exps = new ArrayList<>(trip.getExpenses());
        Collections.reverse(exps);
        for (Expense e : exps) {
            JPanel r = new JPanel(new BorderLayout(12, 0));
            r.setOpaque(false);
            r.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0xd9cfe6)), BorderFactory.createEmptyBorder(8, 0, 8, 0)));
            r.add(new Avatar(e.getPaidBy(), trip.getMembers()), BorderLayout.WEST);
            JPanel mid = vbox();
            SplitStrategy s = ExpenseSplitter.getStrategy(e.getSplitType());
            mid.add(left(label("<html><b>" + e.getTitle() + "</b> &nbsp;<span style='background:#ddd2fb'>&nbsp;" + s.getLabel()
                    + "&nbsp;</span></html>", Font.PLAIN, 15)));
            StringBuilder sub = new StringBuilder(e.getPaidBy() + " ออกเงิน · ");
            boolean first = true;
            for (Map.Entry<String, Double> sh : ExpenseSplitter.sharesOf(e).entrySet()) {
                if (!first) sub.append(", ");
                first = false;
                sub.append(sh.getKey()).append(' ').append(baht(sh.getValue()));
            }
            mid.add(left(hint(sub.toString())));
            JButton del = new PillButton("ลบ", PAPER);
            del.setFont(font(Font.PLAIN, 13));
            String id = e.getId();
            del.addActionListener(ev -> store.update(t -> t.removeExpense(id)));
            r.add(mid, BorderLayout.CENTER);
            r.add(row(10, label(baht(e.getAmount()), Font.BOLD, 16), del), BorderLayout.EAST);
            r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            list.add(left(r));
        }
        if (exps.isEmpty()) list.add(left(hint("ยังไม่มีค่าใช้จ่าย จดรายการแรกจากฟอร์มด้านบน")));
        total.setText(exps.isEmpty() ? " " : "รวมทั้งหมด " + baht(trip.getExpenseTotal()));
        revalidate();
        repaint();
    }
}
