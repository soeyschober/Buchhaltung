import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignored) {}
            JFrame f = new JFrame("Buchhaltung");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(new BuchhaltungMainPage().getRootPnl());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}