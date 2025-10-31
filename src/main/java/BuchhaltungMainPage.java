import java.awt.*;
import javax.swing.*;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import java.awt.event.ItemEvent;
import java.util.Locale;
import java.time.LocalDate;

/**
 * {@summary Hauptseite der Buchhaltung mit Filterleiste, Eintragsliste und Aktionen.}
 * Stellt Datum-Filter, Saldo-Auswahl und den Dialog zum Anlegen neuer Einträge bereit.
 */
public class BuchhaltungMainPage {

    private JPanel rootPnl;
    private JPanel northPnl;
    private JPanel southPnl;
    private JTextField saldoTextF;
    private JPanel filterPnl;
    private JScrollPane eintragScrollPnl;
    private JLabel headerLbl;
    private JComboBox<String> saldoComboB;
    private DatePicker vonDatePicker;
    private DatePicker bisDatePicker;
    private JButton neuerEintragBtn;
    private JTable eintraegeTable;

    public BuchhaltungMainPage() {
        configurePickers();
        wireListeners();
        configureDropBox();
        // im Konstruktor NACH configureDropBox():
        saldoComboB.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) updateSaldo();
        });
        updateSaldo();
        SwingUtilities.invokeLater(this::loadEntries);
        SwingUtilities.invokeLater(this::tuneTable);
    }

    public void loadEntries() {
        try {
            eintraegeTable.setModel(DbLite.tableModelAll());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(rootPnl, "Konnte Einträge nicht laden: " + ex.getMessage());
        }
    }

    private void tuneTable() {
        JTable t = eintraegeTable; // use your JTable variable name
        t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        // Column indices based on your model: 0=ID, 1=Datum, 2=Kategorie, 3=Beschreibung, 4=Betrag (€)
        var cm = t.getColumnModel();

        // ID: small, stop hogging pixels
        cm.getColumn(0).setMinWidth(50);
        cm.getColumn(0).setMaxWidth(70);
        cm.getColumn(0).setPreferredWidth(60);

        // Date: compact
        cm.getColumn(1).setPreferredWidth(200);

        // Category: modest
        cm.getColumn(2).setPreferredWidth(170);

        // Description: give it space
        cm.getColumn(3).setPreferredWidth(200);

        // Money: reasonable width + right align
        cm.getColumn(4).setPreferredWidth(110);
        javax.swing.table.DefaultTableCellRenderer right = new javax.swing.table.DefaultTableCellRenderer();
        right.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cm.getColumn(4).setCellRenderer(right);

        // Optional: nicer row height a tiny bit
        t.setRowHeight(Math.max(t.getRowHeight(), 22));
    }

    /**
     * {@summary Verdrahtet Event-Listener für alle Aktionen.}
     */
    private void wireListeners() {
        neuerEintragBtn.addActionListener(e ->  {
            openEntryDialog();
        });
    }

    /**
     * {@summary Öffnet den Dialog zum erstellen von einem neuen Eintrag.}
     */
    private void openEntryDialog() {
        EintragseingabePage form = new EintragseingabePage();

        Window owner = SwingUtilities.getWindowAncestor(getRootPnl());

        JDialog dlg = new JDialog(owner, "Neuer Eintrag", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setContentPane(form.getRootPnl());
        dlg.pack();

        dlg.setLocationRelativeTo(owner);

        dlg.setVisible(true);
        loadEntries();
    }

    /**
     * {@summary Konfiguriert beide DatePicker: }
     * Locale/Format, keine Tastatureingabe, gegenseitige
     * Bereichslimits und Erzwingen von (von ≤ bis).
     */
    private void configurePickers() {
        Locale deAT = Locale.forLanguageTag("de-AT");

        for (DatePicker dp : new DatePicker[]{ vonDatePicker, bisDatePicker }) {
            DatePickerSettings s = dp.getSettings();
            s.setLocale(deAT);
            s.setFormatForDatesCommonEra("dd.MM.yyyy");
            s.setAllowKeyboardEditing(false);
        }

        vonDatePicker.addDateChangeListener(e -> {
            LocalDate from = e.getNewDate();

            bisDatePicker.getSettings().setDateRangeLimits(from, null);

            LocalDate to = bisDatePicker.getDate();
            if (from != null && to != null && to.isBefore(from)) {
                bisDatePicker.setDate(from);
            }
        });

        bisDatePicker.addDateChangeListener(e -> {
            LocalDate to = e.getNewDate();

            vonDatePicker.getSettings().setDateRangeLimits(null, to);

            LocalDate from = vonDatePicker.getDate();
            if (to != null && from != null && from.isAfter(to)) {
                vonDatePicker.setDate(to);
            }
        });
    }

    /**
     * {@summary Befüllt die Saldo-Auswahl und setzt die Voreinstellung.}
     */
    private void configureDropBox() {
        saldoComboB.addItem("Alle");
        saldoComboB.addItem("Einnahmen");
        saldoComboB.addItem("Ausgaben");

        saldoComboB.setSelectedIndex(0);
    }

    /**
     * {@summary Aktualisiert den Saldo-Text anhand der aktuellen Auswahl der Combo-Box.}
     * Setzt je nach Modus "Einnahmen", "Ausgaben" oder "Alles" in das Saldo-Feld.
     * !NUR DEMO - LOGIK MIT DB FEHLT NOCH!
     */
    private void updateSaldo() {
        String mode = (String) saldoComboB.getSelectedItem();
        switch (mode) {
            case "Einnahmen" -> saldoTextF.setText("Einnahmen");
            case "Ausgaben"  -> saldoTextF.setText("Ausgaben");
            default          -> saldoTextF.setText("Alles");
        }
    }

    //region Getter & Setter

    public JPanel getRootPnl() {
        return rootPnl;
    }

    //endregion
}
