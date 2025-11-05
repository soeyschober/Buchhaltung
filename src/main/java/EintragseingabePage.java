import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class EintragseingabePage {
    private JPanel rootPnl;
    private JPanel northPnl;
    private JPanel centerPnl;
    private JPanel southPnl;
    private JLabel headerLbl;
    private JButton bestaetigenBtn;
    private JButton abbrechenBtn;
    private JTextField belegnrTextF;
    private JLabel belegnrLbl;
    private JLabel datumLbl;
    private JLabel betragLbl;
    private JLabel kategorieLbl;
    private JTextArea beschreibungTextArea;
    private JLabel beschreibungLbl;
    private JTextField datumTextF;
    private JComboBox<String> kategorieComboB;
    private JPanel helperPnl;
    private JFormattedTextField betragTextF;

    private BuchhaltungMainPage mainPage = new BuchhaltungMainPage();

    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public EintragseingabePage() {
        wireListeners();
        initAmountField();
        configureDropBox();

        datumTextF.setText(LocalDate.now().format(DATE_FMT));
    }

    /**
     * {@summary Initialisiert Listener, Betragsfeld und Kategorieauswahl.}
     */
    private void wireListeners() {
        abbrechenBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(rootPnl);
            if (w != null) w.dispose();
        });

        bestaetigenBtn.addActionListener(e -> save());
    }

    /**
     * {@summary Verdrahtet Aktionen wie Abbrechen-Schließen.}
     */
    private void initAmountField() {
        DecimalFormat df = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.GERMANY));
        df.setGroupingUsed(false);
        NumberFormatter nf = new NumberFormatter(df);
        nf.setValueClass(BigDecimal.class);
        nf.setAllowsInvalid(false);
        nf.setMinimum(BigDecimal.ZERO);
        if (betragTextF == null) betragTextF = new JFormattedTextField();
        betragTextF.setFormatterFactory(new DefaultFormatterFactory(nf));
        betragTextF.setColumns(10);
        betragTextF.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
    }

    /**
     * {@summary Befüllt die Kategorie-Auswahl und setzt die Voreinstellung.}
     */
    private void configureDropBox() {
        kategorieComboB.addItem("Einnahmen");
        kategorieComboB.addItem("Ausgaben");

        kategorieComboB.setSelectedIndex(0);
    }

    private void save() {
        try {
            // Belegnummer
            String belegnr = belegnrTextF.getText();

            // Datum parsen
            LocalDate d = LocalDate.parse(datumTextF.getText(), DATE_FMT);

            // Kategorie
            String kat = (String) kategorieComboB.getSelectedItem();
            if (kat == null || kat.isBlank()) kat = "Einnahmen";

            // Beschreibung
            String desc = beschreibungTextArea.getText().trim();

            // Betrag aus dem JFormattedTextField holen
            BigDecimal val = (BigDecimal) betragTextF.getValue();
            if (val == null) {
                JOptionPane.showMessageDialog(rootPnl, "Bitte Betrag eingeben (z. B. 12,34).");
                betragTextF.requestFocus();
                return;
            }
            int cents = val.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).intValueExact();
            if ("Ausgaben".equalsIgnoreCase(kat)) cents = -Math.abs(cents);

            // tatsächliches INSERT
            DbLite.insert(belegnr, d, kat, desc, cents);
            Window w = SwingUtilities.getWindowAncestor(rootPnl);
            if (w != null) w.dispose();
            JOptionPane.showMessageDialog(rootPnl, "Gespeichert.");
            mainPage.loadEntries();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(rootPnl, "Fehler: " + ex.getMessage());
        }
    }

    //region Getter & Setter

    public JPanel getRootPnl() {
        return rootPnl;
    }

    //endregion
}
