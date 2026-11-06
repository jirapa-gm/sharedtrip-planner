package sharedtrip;

import javax.swing.SwingUtilities;

import sharedtrip.ui.MainWindow;
import sharedtrip.ui.Theme;

// จุดเริ่มโปรแกรม SharedTrip Planner
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            new MainWindow().setVisible(true);
        });
    }
}
