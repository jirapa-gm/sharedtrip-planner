package sharedtrip.ui;

import static sharedtrip.ui.Theme.*;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import javax.swing.*;

import sharedtrip.composite.Activity;
import sharedtrip.composite.Day;
import sharedtrip.composite.Trip;
import sharedtrip.observer.TripObserver;
import sharedtrip.observer.TripStore;

// แท็บแผนการเดินทาง: เพิ่มกิจกรรมรายวัน แสดงเวลา/ค่าใช้จ่ายรวม และเตือนเวลาชนกัน
public class ItineraryPanel extends JPanel implements TripObserver {
    private final TripStore store = TripStore.getInstance();
    private final JPanel dayBar = row(8);
    private final JPanel list = vbox();
    private final JLabel daySummary = label(" ", Font.BOLD, 15);
    private final JTextField name = field(14), start = field(5), end = field(5), cost = field(7), place = field(10);
    private final JLabel error = label(" ", Font.BOLD, 14);
    private String dayId;

    public ItineraryPanel() {
        super(new BorderLayout(0, 16));
        setOpaque(false);
        error.setForeground(RED_TEXT);

        Card form = new Card(PINK, new BorderLayout(0, 8));
        JPanel fields = vbox();
        fields.add(left(label("เพิ่มกิจกรรม", Font.BOLD, 19)));
        fields.add(left(dayBar));
        JPanel inputs = new JPanel(new GridLayout(2, 5, 10, 2));
        inputs.setOpaque(false);
        for (String h : new String[] { "ทำอะไร", "เริ่ม (HH:mm)", "ถึง (HH:mm)", "ค่าใช้จ่าย (บาท)", "ที่ไหน" })
            inputs.add(label(h, Font.BOLD, 14));
        inputs.add(name); inputs.add(start); inputs.add(end); inputs.add(cost); inputs.add(place);
        fields.add(left(inputs));
        PillButton add = new PillButton("เพิ่มกิจกรรม", PINK_STRONG);
        add.addActionListener(e -> addActivity());
        fields.add(left(row(12, add, error)));
        form.add(fields);

        Card listCard = new Card(PAPER, new BorderLayout(0, 8));
        listCard.add(daySummary, BorderLayout.NORTH);
        listCard.add(list, BorderLayout.CENTER);

        add(form, BorderLayout.NORTH);
        add(listCard, BorderLayout.CENTER);
        store.subscribe(this);
    }

    private static String thaiDate(String iso) {
        LocalDate d = LocalDate.parse(iso);
        Locale th = Locale.forLanguageTag("th-TH");
        return d.getDayOfWeek().getDisplayName(TextStyle.SHORT, th) + " " + d.getDayOfMonth() + " "
                + d.getMonth().getDisplayName(TextStyle.SHORT, th);
    }

    private void addActivity() {
        try {
            String s = start.getText().trim(), e = end.getText().trim();
            if (name.getText().isBlank()) { error.setText("กรอกชื่อกิจกรรม"); return; }
            if (!s.matches("\\d{1,2}:\\d{2}") || !e.matches("\\d{1,2}:\\d{2}")) { error.setText("เวลาต้องเป็น HH:mm เช่น 09:00"); return; }
            Activity probe = new Activity("x", s, e, 0, "");
            if (probe.getDuration() <= 0) { error.setText("เวลาจบต้องหลังเวลาเริ่ม"); return; }
            double c = cost.getText().isBlank() ? 0 : Double.parseDouble(cost.getText().trim().replace(",", ""));
            if (c < 0) { error.setText("ค่าใช้จ่ายต้องไม่ติดลบ"); return; }
            String n = name.getText().trim(), p = place.getText().trim(), id = dayId;
            store.update(t -> t.findDay(id).add(new Activity(n, s, e, c, p)));
            for (JTextField f : new JTextField[] { name, start, end, cost, place }) f.setText("");
            error.setText(" ");
        } catch (NumberFormatException ex) {
            error.setText("ค่าใช้จ่ายต้องเป็นตัวเลข");
        }
    }

    @Override
    public void onTripChanged(Trip trip) {
        dayBar.removeAll();
        list.removeAll();
        if (trip == null) { revalidate(); repaint(); return; }
        if (dayId == null || trip.findDay(dayId) == null) dayId = trip.getDays().get(0).getId();

        int n = 1;
        for (Day d : trip.getDays()) {
            PillButton b = new PillButton("วันที่ " + (n++) + " · " + thaiDate(d.getDate()), d.getId().equals(dayId) ? YELLOW : PAPER);
            b.setSelectedLook(d.getId().equals(dayId));
            b.addActionListener(e -> { dayId = d.getId(); onTripChanged(store.getTrip()); });
            dayBar.add(b);
        }

        Day day = trip.findDay(dayId);
        int mins = day.getDuration();
        daySummary.setText("กิจกรรมวันนี้ " + day.getChildren().size() + " รายการ · "
                + (mins / 60) + " ชม. " + (mins % 60) + " นาที · " + baht(day.getCost()));

        List<Activity[]> conflicts = day.getConflicts();
        for (Activity[] pair : conflicts) {
            JLabel w = label("เวลาชนกัน! " + pair[0].getName() + " (" + pair[0].getStart() + "–" + pair[0].getEnd()
                    + ") กับ " + pair[1].getName() + " (" + pair[1].getStart() + "–" + pair[1].getEnd() + ")", Font.BOLD, 14);
            w.setForeground(RED_TEXT);
            list.add(left(w));
        }
        if (!conflicts.isEmpty()) list.add(Box.createVerticalStrut(8));

        java.util.List<Activity> sorted = new java.util.ArrayList<>(day.getChildren());
        sorted.sort((a, b) -> a.getStart().compareTo(b.getStart()));
        for (Activity a : sorted) {
            JPanel r = new JPanel(new BorderLayout(12, 0));
            r.setOpaque(false);
            r.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0xd9cfe6)), BorderFactory.createEmptyBorder(8, 0, 8, 0)));
            JLabel time = label(a.getStart() + "–" + a.getEnd(), Font.BOLD, 15);
            time.setPreferredSize(new Dimension(110, 24));
            JPanel mid = vbox();
            mid.add(left(label(a.getName(), Font.BOLD, 15)));
            if (!a.getPlace().isEmpty()) mid.add(left(hint(a.getPlace())));
            JButton del = new PillButton("ลบ", PAPER);
            del.setFont(font(Font.PLAIN, 13));
            String id = a.getId();
            del.addActionListener(e -> store.update(t -> t.findDay(dayId).remove(id)));
            JPanel right = row(10, label(baht(a.getCost()), Font.BOLD, 15), del);
            r.add(time, BorderLayout.WEST);
            r.add(mid, BorderLayout.CENTER);
            r.add(right, BorderLayout.EAST);
            r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            list.add(left(r));
        }
        if (sorted.isEmpty()) list.add(left(hint("ยังไม่มีกิจกรรมในวันนี้ เพิ่มจากฟอร์มด้านบน")));
        revalidate();
        repaint();
    }
}
