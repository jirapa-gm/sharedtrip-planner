package sharedtrip.ui;

import static sharedtrip.ui.Theme.*;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import sharedtrip.composite.Day;
import sharedtrip.composite.Trip;
import sharedtrip.io.TripJson;
import sharedtrip.observer.TripObserver;
import sharedtrip.observer.TripStore;

// หน้าต่างหลัก: หัวตั๋วเดินทาง + ปุ่มย้อนกลับ/ทำซ้ำ/บันทึก + 3 แท็บ
public class MainWindow extends JFrame implements TripObserver {
    private final TripStore store = TripStore.getInstance();
    private final CardLayout pages = new CardLayout();
    private final JPanel root = new JPanel(pages);
    private final CreateTripPanel createPanel = new CreateTripPanel(this::loadFile);

    private final JLabel tripName = label(" ", Font.BOLD, 34);
    private final JLabel tripDates = label(" ", Font.BOLD, 15);
    private final JPanel memberChips = row(6);
    private final JLabel spentTitle = label("ใช้ไปแล้ว", Font.PLAIN, 14);
    private final JLabel spent = label(" ", Font.BOLD, 30);
    private final JLabel spentSub = hint(" ");
    private final JPanel budgetBox = new JPanel(new GridBagLayout());

    private final PillButton undo = new PillButton("ย้อนกลับ", PAPER);
    private final PillButton redo = new PillButton("ทำซ้ำ", PAPER);
    private final PillButton save = new PillButton("บันทึกไฟล์", PAPER);

    private final CardLayout tabs = new CardLayout();
    private final JPanel tabBody = new JPanel(tabs);
    private final PillButton[] tabButtons = {
            new PillButton("แผนการเดินทาง", PAPER), new PillButton("ค่าใช้จ่าย", PAPER), new PillButton("สรุปยอดโอน", PAPER) };

    public MainWindow() {
        super("SharedTrip Planner");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);

        root.setBackground(BG);
        root.add(createPanel, "create");
        root.add(buildTripView(), "trip");
        add(root);

        store.subscribe(this);
        setSize(1100, 860);
        setMinimumSize(new Dimension(900, 640));
        setLocationRelativeTo(null);
        onTripChanged(null);
    }

    private JComponent buildTripView() {
        JPanel view = new JPanel(new BorderLayout(0, 16));
        view.setBackground(BG);
        view.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // แถบบน: ชื่อแอป + ปุ่ม
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel brand = label("SharedTrip Planner", Font.BOLD, 20);
        top.add(brand, BorderLayout.WEST);
        undo.addActionListener(e -> store.undo());
        redo.addActionListener(e -> store.redo());
        save.addActionListener(e -> saveFile());
        PillButton open = new PillButton("เปิดไฟล์", PAPER);
        open.addActionListener(e -> loadFile());
        top.add(row(8, undo, redo, save, open), BorderLayout.EAST);

        // หัวตั๋ว
        Card ticket = new Card(PINK, new BorderLayout());
        ticket.setBorder(new CardBorder(22, 4, 0));
        JPanel leftSide = vbox();
        leftSide.setBorder(BorderFactory.createEmptyBorder(18, 26, 18, 18));
        leftSide.add(left(tripDates));
        leftSide.add(left(tripName));
        leftSide.add(left(memberChips));
        budgetBox.setBackground(YELLOW);
        budgetBox.setPreferredSize(new Dimension(280, 140));
        budgetBox.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, INK));
        JPanel b = vbox();
        b.add(left(spentTitle));
        b.add(left(spent));
        b.add(left(spentSub));
        budgetBox.add(b);
        JPanel ticketInner = new JPanel(new BorderLayout());
        ticketInner.setOpaque(false);
        ticketInner.add(leftSide, BorderLayout.CENTER);
        ticketInner.add(budgetBox, BorderLayout.EAST);
        ticket.add(ticketInner);

        JPanel header = vbox();
        header.add(left(top));
        header.add(Box.createVerticalStrut(12));
        header.add(left(ticket));
        header.add(Box.createVerticalStrut(14));
        JPanel tabBar = row(10);
        String[] keys = { "itinerary", "expenses", "settle" };
        for (int i = 0; i < tabButtons.length; i++) {
            final int k = i;
            tabButtons[i].addActionListener(e -> showTab(k, keys[k]));
            tabBar.add(tabButtons[i]);
        }
        PillButton newTrip = new PillButton("เริ่มทริปใหม่", PAPER);
        newTrip.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "เริ่มทริปใหม่? ทริปปัจจุบันจะหายถ้ายังไม่ได้บันทึกไฟล์",
                    "เริ่มทริปใหม่", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                createPanel.clear();
                store.reset();
            }
        });
        JPanel tabRow = new JPanel(new BorderLayout());
        tabRow.setOpaque(false);
        tabRow.add(tabBar, BorderLayout.WEST);
        tabRow.add(newTrip, BorderLayout.EAST);
        header.add(left(tabRow));

        tabBody.setOpaque(false);
        tabBody.add(scroll(new ItineraryPanel()), keys[0]);
        tabBody.add(scroll(new ExpensePanel()), keys[1]);
        tabBody.add(scroll(new SettlementPanel()), keys[2]);
        showTab(0, keys[0]);

        view.add(header, BorderLayout.NORTH);
        view.add(tabBody, BorderLayout.CENTER);
        return view;
    }

    // ทั้งแท็บเลื่อนขึ้นลงได้ ตัวแท็บกว้างเท่าหน้าต่าง
    private static JScrollPane scroll(JComponent c) {
        JPanel holder = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() { return super.getPreferredSize(); }
        };
        holder.setOpaque(false);
        holder.add(c, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(holder, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        return sp;
    }

    private void showTab(int index, String key) {
        tabs.show(tabBody, key);
        for (int i = 0; i < tabButtons.length; i++) {
            tabButtons[i].setBackground(i == index ? INK : PAPER);
            tabButtons[i].setForeground(i == index ? Color.WHITE : INK);
            tabButtons[i].setSelectedLook(i == index);
        }
    }

    private static String thaiShort(String iso) {
        LocalDate d = LocalDate.parse(iso);
        return d.getDayOfMonth() + " " + d.getMonth().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("th-TH"));
    }

    @Override
    public void onTripChanged(Trip trip) {
        if (trip == null) {
            pages.show(root, "create");
            return;
        }
        pages.show(root, "trip");
        java.util.List<Day> days = trip.getDays();
        tripName.setText(trip.getName());
        tripDates.setText(thaiShort(days.get(0).getDate()) + " – " + thaiShort(days.get(days.size() - 1).getDate())
                + " · " + days.size() + " วัน");
        memberChips.removeAll();
        for (String m : trip.getMembers()) memberChips.add(row(4, new Avatar(m, trip.getMembers()), label(m, Font.BOLD, 14)));

        int mins = trip.getDuration();
        spent.setText(baht(trip.getTotalSpent()));
        if (trip.isOverBudget()) {
            budgetBox.setBackground(new Color(0xffd0da));
            spentTitle.setText("เกินงบแล้ว!");
            spentTitle.setForeground(RED_TEXT);
            spentSub.setText("งบ " + baht(trip.getBudget()) + " · เกิน " + baht(trip.getTotalSpent() - trip.getBudget()));
        } else {
            budgetBox.setBackground(YELLOW);
            spentTitle.setText("ใช้ไปแล้ว");
            spentTitle.setForeground(INK);
            spentSub.setText((trip.getBudget() > 0 ? "จากงบ " + baht(trip.getBudget()) + " · " : "")
                    + "กิจกรรมรวม " + (mins / 60) + " ชม. " + (mins % 60) + " นาที");
        }
        undo.setEnabled(store.canUndo());
        redo.setEnabled(store.canRedo());
        revalidate();
        repaint();
    }

    private JFileChooser chooser() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("ไฟล์ทริป (.json)", "json"));
        return fc;
    }

    private void saveFile() {
        Trip t = store.getTrip();
        if (t == null) return;
        JFileChooser fc = chooser();
        fc.setSelectedFile(new File(t.getName() + ".json"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path p = fc.getSelectedFile().toPath();
        if (!p.toString().toLowerCase().endsWith(".json")) p = Path.of(p + ".json");
        try {
            TripJson.save(t, p);
            JOptionPane.showMessageDialog(this, "บันทึกแล้ว: " + p.getFileName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "บันทึกไม่ได้: " + ex.getMessage(), "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadFile() {
        JFileChooser fc = chooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            Trip t = TripJson.load(fc.getSelectedFile().toPath());
            if (t.getDays().isEmpty()) throw new IllegalArgumentException("ไม่มีวันเดินทาง");
            store.setTrip(t);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "เปิดไฟล์ไม่ได้ ต้องเป็นไฟล์ .json ที่บันทึกจากแอปนี้",
                    "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
        }
    }
}
