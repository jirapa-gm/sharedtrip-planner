package sharedtrip.ui;

import static sharedtrip.ui.Theme.*;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.*;

import sharedtrip.builder.TripBuilder;
import sharedtrip.observer.TripStore;

// หน้าสร้างทริปใหม่ ใช้ TripBuilder
public class CreateTripPanel extends JPanel {
    private final JTextField name = field(20), budget = field(10), start = field(10), end = field(10), members = field(28);
    private final JLabel error = label(" ", Font.BOLD, 14);

    public CreateTripPanel(Runnable onLoadFile) {
        super(new GridBagLayout());
        setBackground(BG);
        error.setForeground(RED_TEXT);

        Card card = new Card(PINK, new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(4, 6, 4, 6);
        card.add(label("วางแผนทริปใหม่", Font.BOLD, 26), c);
        c.gridy++;
        card.add(hint("กรอกข้อมูลทริป แล้วระบบจะสร้างวันให้ตามช่วงวันที่"), c);

        c.gridwidth = 1; c.fill = GridBagConstraints.HORIZONTAL;
        addRow(card, c, "ชื่อทริป", name, "เช่น เชียงใหม่");
        addRow(card, c, "งบประมาณ (บาท)", budget, "0 = ไม่กำหนด");
        addRow(card, c, "วันเริ่ม (ปปปป-ดด-วว)", start, "เช่น 2026-11-23");
        addRow(card, c, "วันสุดท้าย", end, "เว้นว่าง = ไปวันเดียว");
        addRow(card, c, "เพื่อนร่วมทริป", members, "คั่นด้วยจุลภาค เช่น ฟ้า, ฝน, เมฆ");

        c.gridx = 0; c.gridy++; c.gridwidth = 2;
        card.add(error, c);

        PillButton create = new PillButton("สร้างทริป", PINK_STRONG);
        PillButton load = new PillButton("เปิดไฟล์ทริป (.json)", PAPER);
        create.addActionListener(e -> create());
        load.addActionListener(e -> onLoadFile.run());
        c.gridy++;
        card.add(row(10, create, load), c);

        add(card);
    }

    private void addRow(JPanel p, GridBagConstraints c, String text, JTextField f, String hintText) {
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        p.add(label(text, Font.BOLD, 15), c);
        c.gridx = 1;
        c.weightx = 1;
        JPanel box = vbox();
        box.add(left(f));
        box.add(left(hint(hintText)));
        p.add(box, c);
    }

    private void create() {
        try {
            TripBuilder b = new TripBuilder()
                    .setName(name.getText())
                    .setBudget(budget.getText().isBlank() ? 0 : Double.parseDouble(budget.getText().trim().replace(",", "")))
                    .setDateRange(parseDate(start.getText()), end.getText().isBlank() ? null : parseDate(end.getText()));
            for (String m : members.getText().split(",")) b.addMember(m);
            TripStore.getInstance().setTrip(b.build());
            error.setText(" ");
        } catch (NumberFormatException ex) {
            error.setText("งบประมาณต้องเป็นตัวเลข");
        } catch (DateTimeParseException ex) {
            error.setText("วันที่ต้องอยู่ในรูปแบบ ปปปป-ดด-วว เช่น 2026-11-23");
        } catch (IllegalStateException ex) {
            error.setText(ex.getMessage());
        }
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s.trim());
    }

    public void clear() {
        for (JTextField f : new JTextField[] { name, budget, start, end, members }) f.setText("");
        error.setText(" ");
    }
}
