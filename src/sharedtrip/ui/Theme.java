package sharedtrip.ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

// สี ฟอนต์ และชิ้นส่วนหน้าจอที่ใช้ซ้ำ ธีมพาสเทล ขอบม่วงเข้ม เงาทึบ
public final class Theme {
    private Theme() {}

    public static final Color INK = new Color(0x3b2f55);
    public static final Color INK_SOFT = new Color(0x6e6288);
    public static final Color BG = new Color(0xfff4fa);
    public static final Color PAPER = Color.WHITE;
    public static final Color PINK = new Color(0xffc8de);
    public static final Color PINK_STRONG = new Color(0xff8fb8);
    public static final Color YELLOW = new Color(0xfff0b0);
    public static final Color MINT = new Color(0xc8f2df);
    public static final Color MINT_STRONG = new Color(0x5fcf9f);
    public static final Color BLUE = new Color(0xd3e8ff);
    public static final Color LILAC = new Color(0xddd2fb);
    public static final Color RED = new Color(0xff6b8a);
    public static final Color RED_TEXT = new Color(0xc2224a);
    public static final Color GREEN_TEXT = new Color(0x148a5c);

    private static final Color[] AVATAR = { PINK, MINT, BLUE, LILAC, YELLOW, new Color(0xffd9c2) };

    private static final String FONT = pickFont();

    private static String pickFont() {
        String[] wanted = { "Tahoma", "Leelawadee UI", "Leelawadee", "Sukhumvit Set", "Thonburi", "Loma", "Garuda", "Dialog" };
        java.util.Set<String> have = new java.util.HashSet<>(java.util.Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : wanted) if (have.contains(f)) return f;
        return Font.DIALOG;
    }

    public static Font font(int style, float size) {
        return new Font(FONT, style, Math.round(size));
    }

    public static void install() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
        Font base = font(Font.PLAIN, 15);
        for (String k : new String[] { "Label.font", "Button.font", "TextField.font", "ComboBox.font", "CheckBox.font",
                "RadioButton.font", "TabbedPane.font", "OptionPane.messageFont", "OptionPane.buttonFont",
                "List.font", "ToolTip.font", "FileChooser.listFont", "Spinner.font" })
            UIManager.put(k, base);
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", BG);
        UIManager.put("Label.foreground", INK);
        UIManager.put("TabbedPane.selected", PINK);
        UIManager.put("TabbedPane.contentAreaColor", BG);
    }

    // ---------- ขอบโค้ง + เงาทึบ ----------
    public static class CardBorder extends AbstractBorder {
        private final int radius, shadow, pad;

        public CardBorder(int radius, int shadow, int pad) {
            this.radius = radius;
            this.shadow = shadow;
            this.pad = pad;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(pad + 2, pad + 2, pad + 2 + shadow, pad + 2 + shadow);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(INK);
            g2.draw(new RoundRectangle2D.Float(x + 1.5f, y + 1.5f, w - shadow - 3, h - shadow - 3, radius, radius));
            g2.dispose();
        }
    }

    // แผงกล่องสีพื้น มีขอบโค้งและเงา
    public static class Card extends JPanel {
        private final int radius = 22, shadow = 4;

        public Card(Color bg, LayoutManager lm) {
            super(lm);
            setOpaque(false);
            setBackground(bg);
            setBorder(new CardBorder(radius, shadow, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - shadow, h = getHeight() - shadow;
            g2.setColor(INK);
            g2.fillRoundRect(shadow, shadow, w, h, radius, radius);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, w, h, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ปุ่มทรงแคปซูล
    public static class PillButton extends JButton {
        private boolean selected;

        public PillButton(String text, Color bg) {
            super(text);
            setBackground(bg);
            setForeground(INK);
            setFont(font(Font.BOLD, 15));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 18, 11, 21));
        }

        public void setSelectedLook(boolean s) {
            selected = s;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int sh = selected || getModel().isPressed() ? 1 : 3;
            int off = 3 - sh;
            int w = getWidth() - 4, h = getHeight() - 4;
            g2.setColor(isEnabled() ? INK : INK_SOFT);
            g2.fillRoundRect(sh + off, sh + off, w, h, h, h);
            g2.setColor(isEnabled() ? getBackground() : new Color(0xf1edf6));
            g2.fillRoundRect(off, off, w, h, h, h);
            g2.setStroke(new BasicStroke(2.2f));
            g2.setColor(isEnabled() ? INK : INK_SOFT);
            g2.drawRoundRect(off + 1, off + 1, w - 2, h - 2, h - 2, h - 2);
            g2.translate(off, off);
            g2.dispose();
            Graphics2D t = (Graphics2D) g.create();
            t.translate(off, off);
            super.paintComponent(t);
            t.dispose();
        }
    }

    // ช่องกรอกข้อความ ขอบหนา
    public static JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        f.setForeground(INK);
        f.setBackground(PAPER);
        f.setCaretColor(INK);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 2, 4, 4, INK), new EmptyBorder(6, 10, 6, 10)));
        return f;
    }

    public static JLabel label(String text, int style, float size) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(INK);
        return l;
    }

    public static JLabel hint(String text) {
        JLabel l = label(text, Font.PLAIN, 13);
        l.setForeground(INK_SOFT);
        return l;
    }

    // วงกลมตัวอักษรแรกของชื่อ สีคงที่ตามลำดับสมาชิก
    public static class Avatar extends JComponent {
        private final String name;
        private final Color color;

        public Avatar(String name, List<String> members) {
            this.name = name;
            int i = members.indexOf(name);
            this.color = AVATAR[(i >= 0 ? i : Math.abs(name.hashCode())) % AVATAR.length];
            setPreferredSize(new Dimension(30, 30));
            setMinimumSize(new Dimension(30, 30));
        }

        private String initial() {
            for (char ch : name.toCharArray())
                if (Character.isLetterOrDigit(ch) && !(ch >= 'ะ' && ch <= '๎'))
                    return String.valueOf(ch).toUpperCase();
            return name.isEmpty() ? "?" : name.substring(0, 1);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight()) - 2;
            g2.setColor(color);
            g2.fillOval(1, 1, d, d);
            g2.setColor(INK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(1, 1, d, d);
            g2.setFont(font(Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            String s = initial();
            g2.drawString(s, 1 + (d - fm.stringWidth(s)) / 2, 1 + (d - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.##");

    public static String baht(double v) {
        return "฿" + MONEY.format(v);
    }

    public static JPanel row(int gap, Component... cs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0));
        p.setOpaque(false);
        for (Component c : cs) p.add(c);
        return p;
    }

    public static JPanel vbox() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    public static <T extends JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }
}
