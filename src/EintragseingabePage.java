import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Locale;
import java.time.format.DateTimeFormatter;

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

    public EintragseingabePage() {
        wireListeners();
        initAmountField();
        configureDropBox();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        datumTextF.setText(LocalDate.now().format(fmt));
    }

    /**
     * {@summary Initialisiert Listener, Betragsfeld und Kategorieauswahl.}
     */
    private void wireListeners() {
        abbrechenBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(rootPnl);
            if (w != null) w.dispose();
        });
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

    //region Getter & Setter

    public JPanel getRootPnl() {
        return rootPnl;
    }

    //endregion
}
