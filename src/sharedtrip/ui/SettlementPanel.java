package sharedtrip.ui;

import static sharedtrip.ui.Theme.*;

import java.awt.*;
import java.util.List;

import javax.swing.*;

import sharedtrip.composite.Trip;
import sharedtrip.observer.TripObserver;
import sharedtrip.observer.TripStore;
import sharedtrip.settlement.Settlement;
import sharedtrip.settlement.Settlement.Balance;
import sharedtrip.settlement.Settlement.Transfer;

// แท็บสรุปยอดโอน: ใครต้องโอนให้ใคร + ยอดคงเหลือแต่ละคน
public class SettlementPanel extends JPanel implements TripObserver {
    private final JPanel content = vbox();

    public SettlementPanel() {
        super(new BorderLayout());
        setOpaque(false);
        add(content, BorderLayout.NORTH);
        TripStore.getInstance().subscribe(this);
    }

    // ตั๋วโอนเงิน: คนโอน ---> คนรับ | ยอด
    private static JComponent ticket(Transfer t, List<String> members) {
        Card c = new Card(PINK, new BorderLayout());
        c.setBorder(new CardBorder(22, 4, 0));
        JPanel route = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Component a = getComponent(0), b = getComponent(2);
                int y = getHeight() / 2, x1 = a.getX() + a.getWidth() + 8, x2 = b.getX() - 8;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INK);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] { 7, 5 }, 0));
                g2.drawLine(x1, y, x2 - 10, y);
                g2.fillPolygon(new int[] { x2 - 12, x2, x2 - 12 }, new int[] { y - 7, y, y + 7 }, 3);
                g2.dispose();
            }
        };
        route.setOpaque(false);
        route.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        route.add(row(8, new Avatar(t.from, members), label(t.from, Font.BOLD, 16)), BorderLayout.WEST);
        route.add(Box.createHorizontalStrut(120), BorderLayout.CENTER);
        route.add(row(8, new Avatar(t.to, members), label(t.to, Font.BOLD, 16)), BorderLayout.EAST);

        JPanel price = new JPanel(new GridBagLayout());
        price.setBackground(YELLOW);
        price.setPreferredSize(new Dimension(170, 60));
        price.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, INK));
        price.add(label(baht(t.amount), Font.BOLD, 22));

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.add(route, BorderLayout.CENTER);
        inner.add(price, BorderLayout.EAST);
        c.add(inner);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        return c;
    }

    // แถบยอดคงเหลือ: แดงซ้าย = ต้องโอนเพิ่ม, เขียวขวา = ได้เงินคืน
    private static JComponent bar(double net, double max) {
        JComponent b = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth() - 2, h = 18, y = (getHeight() - h) / 2, mid = w / 2;
                Shape clip = new java.awt.geom.RoundRectangle2D.Float(1, y, w, h, h, h);
                g2.setColor(PAPER);
                g2.fill(clip);
                g2.setClip(clip);
                int len = (int) Math.round(Math.abs(net) / max * (w / 2.0));
                if (net > 0) { g2.setColor(MINT_STRONG); g2.fillRect(mid, y, len, h); }
                if (net < 0) { g2.setColor(RED); g2.fillRect(mid - len, y, len, h); }
                g2.setClip(null);
                g2.setColor(INK);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(clip);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] { 3, 3 }, 0));
                g2.drawLine(mid, y, mid, y + h);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(360, 30));
        return b;
    }

    @Override
    public void onTripChanged(Trip trip) {
        content.removeAll();
        if (trip == null) { revalidate(); repaint(); return; }

        if (trip.getExpenses().isEmpty()) {
            Card c = new Card(PAPER, new FlowLayout(FlowLayout.CENTER));
            c.add(label("ยังไม่มีอะไรต้องโอน · จดค่าใช้จ่ายในแท็บ \"ค่าใช้จ่าย\" แล้วยอดโอนจะสรุปที่นี่", Font.BOLD, 16));
            content.add(left(c));
            revalidate(); repaint();
            return;
        }

        List<Balance> balances = Settlement.getBalances(trip);
        List<Transfer> transfers = Settlement.getTransfers(balances);

        content.add(left(label("ใครต้องโอนให้ใคร", Font.BOLD, 20)));
        content.add(Box.createVerticalStrut(10));
        if (transfers.isEmpty()) content.add(left(label("ทุกคนจ่ายครบแล้ว ไม่ต้องโอนเพิ่ม", Font.BOLD, 16)));
        for (Transfer t : transfers) {
            content.add(left(ticket(t, trip.getMembers())));
            content.add(Box.createVerticalStrut(12));
        }
        content.add(Box.createVerticalStrut(10));

        Card card = new Card(PAPER, new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridwidth = 3; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(4, 4, 8, 4);
        card.add(label("ยอดของแต่ละคน", Font.BOLD, 20), c);
        c.gridwidth = 1;
        double max = 1;
        for (Balance b : balances) max = Math.max(max, Math.abs(b.net));
        for (Balance b : balances) {
            c.gridy++;
            c.gridx = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
            card.add(row(8, new Avatar(b.name, trip.getMembers()), label(b.name, Font.BOLD, 15)), c);
            c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            JComponent br = bar(b.net, max);
            br.setToolTipText("จ่ายไป " + baht(b.paid) + " · ส่วนของตัวเอง " + baht(b.owed));
            card.add(br, c);
            c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.EAST;
            JLabel v = label(b.net == 0 ? "ครบแล้ว" : (b.net > 0 ? "+" : "-") + baht(Math.abs(b.net)), Font.BOLD, 15);
            v.setForeground(b.net > 0 ? GREEN_TEXT : b.net < 0 ? RED_TEXT : INK_SOFT);
            v.setPreferredSize(new Dimension(110, 24));
            v.setHorizontalAlignment(SwingConstants.RIGHT);
            card.add(v, c);
            c.anchor = GridBagConstraints.WEST;
        }
        c.gridy++; c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        JPanel legend = new JPanel(new BorderLayout());
        legend.setOpaque(false);
        legend.add(hint("ซ้าย (แดง) = ต้องโอนเพิ่ม"), BorderLayout.WEST);
        legend.add(hint("ขวา (เขียว) = ได้เงินคืน"), BorderLayout.EAST);
        card.add(legend, c);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        content.add(left(card));
        revalidate();
        repaint();
    }
}
