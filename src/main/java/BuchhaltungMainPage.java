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

    /**
     * {@summary Initialisiert die Hauptseite, konfiguriert DatePicker, Listener und Sichteinstellungen.}
     * Lädt anschließend die Einträge und passt die Tabellenansicht an.
     */
    public BuchhaltungMainPage() {
        configurePickers();
        wireListeners();
        configureDropBox();

        saldoComboB.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) updateSaldo();
        });
        updateSaldo();
        SwingUtilities.invokeLater(this::loadEntries);
        SwingUtilities.invokeLater(this::tuneTable);
    }

    /**
     * {@summary Lädt die Einträge aus der Datenquelle in die Tabelle.}
     * Zeigt bei Fehlern einen Dialog an, wendet Tabelleneinstellungen an
     * und scrollt auf den letzten Eintrag.
     */
    public void loadEntries() {
        try {
            eintraegeTable.setModel(DbLite.tableModelAll());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(rootPnl, "Konnte Einträge nicht laden: " + ex.getMessage());
        }
        tuneTable();
        snapBottom(eintraegeTable);
    }

    /**
     * {@summary Passt Spaltenbreiten, Ausrichtung und Zeilenhöhe der Tabelle an.}
     * Richtet außerdem die Betrags-Spalte rechtsbündig aus.
     */
    private void tuneTable() {
        JTable t = eintraegeTable;
        t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        javax.swing.table.DefaultTableCellRenderer right = new javax.swing.table.DefaultTableCellRenderer();
        right.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        var cm = t.getColumnModel();

        cm.getColumn(0).setMinWidth(30);
        cm.getColumn(0).setMaxWidth(50);
        cm.getColumn(0).setPreferredWidth(40);

        cm.getColumn(1).setMinWidth(50);
        cm.getColumn(1).setMaxWidth(70);
        cm.getColumn(1).setPreferredWidth(60);

        cm.getColumn(2).setPreferredWidth(200);

        cm.getColumn(3).setPreferredWidth(170);

        cm.getColumn(4).setPreferredWidth(200);

        cm.getColumn(5).setPreferredWidth(110);
        cm.getColumn(5).setCellRenderer(right);

        t.setRowHeight(Math.max(t.getRowHeight(), 22));
    }

    /**
     * {@summary Scrollt die Tabelle auf die letzte Zeile und hält sie dort bei Einfügen neuer Zeilen.}
     * Fügt außerdem ein ESC-Tastenkürzel hinzu, um jederzeit wieder nach unten zu springen.
     * @param t Tabelle, die gescrollt werden soll
     */
    static void snapBottom(JTable t) {
        Runnable go = () -> {
            int m = t.getModel().getRowCount() - 1;
            if (m < 0) return;
            int v = (t.getRowSorter() != null) ? t.convertRowIndexToView(m) : m;
            t.getSelectionModel().setSelectionInterval(v, v);
            t.scrollRectToVisible(t.getCellRect(v, 0, true));
        };

        t.getModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.INSERT)
                javax.swing.SwingUtilities.invokeLater(go);
        });

        t.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "snapBottom");
        t.getActionMap().put("snapBottom", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { go.run(); }
        });

        javax.swing.SwingUtilities.invokeLater(go);
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
