import java.awt.*;
import javax.swing.*;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import java.awt.event.ItemEvent;
import java.util.Locale;
import java.time.LocalDate;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.time.format.DateTimeFormatter;

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

    private TableRowSorter<TableModel> sorter;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final int COL_TYP = 1;
    private static final int COL_DATUM = 2;
    private static final int COL_BETRAG = 5;

    /**
     * {@summary Initialisiert die Hauptseite, konfiguriert DatePicker, Listener und Sichteinstellungen.}
     * Lädt anschließend die Einträge und passt die Tabellenansicht an.
     */
    public BuchhaltungMainPage() {
        configurePickers();
        wireListeners();
        configureDropBox();
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

        sorter = new TableRowSorter<>(eintraegeTable.getModel());
        eintraegeTable.setRowSorter(sorter);
        applyFilters();

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

        saldoComboB.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) updateSaldo();
        });

        vonDatePicker.addDateChangeListener(e -> {
            LocalDate from = e.getNewDate();
            bisDatePicker.getSettings().setDateRangeLimits(from, null);
            applyFilters();
            LocalDate to = bisDatePicker.getDate();
            if (to != null && from != null && to.isBefore(from)) {
                bisDatePicker.setDate(from);
            }
        });

        bisDatePicker.addDateChangeListener(e -> {
            LocalDate to = e.getNewDate();
            vonDatePicker.getSettings().setDateRangeLimits(null, to);
            applyFilters();
            LocalDate from = vonDatePicker.getDate();
            if (to != null && from != null && from.isAfter(to)) {
                vonDatePicker.setDate(to);
            }
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
            s.setFormatForDatesCommonEra(DATE_FMT);
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
        if (eintraegeTable.getModel() == null) return;
        String mode = (String) saldoComboB.getSelectedItem();
        double sum = 0.0;

        TableRowSorter<?> rs = (TableRowSorter<?>) eintraegeTable.getRowSorter();
        int rowCount = eintraegeTable.getRowCount();

        for (int v = 0; v < rowCount; v++) {
            int m = (rs != null) ? eintraegeTable.convertRowIndexToModel(v) : v;
            Object amtObj = eintraegeTable.getModel().getValueAt(m, COL_BETRAG);
            double val = parseAmount(amtObj);
            Object typObj = safeGet(eintraegeTable.getModel(), m, COL_TYP);
            boolean isIncome = inferIncome(typObj, val);

            switch (mode) {
                case "Einnahmen":
                    if (isIncome) sum += Math.abs(val);
                    break;
                case "Ausgaben":
                    if (!isIncome) sum += Math.abs(val);
                    break;
                default: // Alle
                    sum += val;
            }
        }

        saldoTextF.setText(String.format(Locale.GERMANY, "%,.2f €", sum));
        if (sum > 0) saldoTextF.setForeground(new Color(0, 128, 0));
        else if (sum < 0) saldoTextF.setForeground(new Color(160, 0, 0));
        else saldoTextF.setForeground(UIManager.getColor("TextField.foreground"));
    }

    /**
     * {@summary}
     * Applies both date and type filters according to pickers and combo box.
     * Also triggers saldo recomputation.
     */
    private void applyFilters() {
        if (eintraegeTable.getRowSorter() == null) return;

        final LocalDate from = vonDatePicker.getDate();
        final LocalDate to   = bisDatePicker.getDate();
        final String mode = (String) saldoComboB.getSelectedItem();

        RowFilter<TableModel, Object> filter = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Object> entry) {
                // date check
                Object dateVal = entry.getValue(COL_DATUM);
                if (dateVal == null) return false;
                LocalDate d = parseDateSafe(dateVal.toString().trim());
                if (d == null) return false;
                if (from != null && d.isBefore(from)) return false;
                if (to   != null && d.isAfter(to))   return false;

                // type check
                Object typVal = entry.getValue(COL_TYP);
                Object amtObj = entry.getValue(COL_BETRAG);
                double val = parseAmount(amtObj);
                boolean isIncome = inferIncome(typVal, val);

                if ("Einnahmen".equals(mode)) return isIncome;
                if ("Ausgaben".equals(mode))  return !isIncome;
                return true; // Alle
            }
        };

        ((TableRowSorter<?>) eintraegeTable.getRowSorter()).setRowFilter(filter);
        updateSaldo();
    }


    private boolean inferIncome(Object typVal, double amount) {
        if (typVal != null) {
            String s = String.valueOf(typVal).toLowerCase(Locale.ROOT);
            if (s.contains("einnah")) return true;
            if (s.contains("ausgab")) return false;
            if (s.equals("+") || s.equals("in") || s.equals("ein")) return true;
            if (s.equals("-") || s.equals("out") || s.equals("aus")) return false;
        }
        // fallback by sign
        return amount >= 0.0;
    }

    private Object safeGet(TableModel m, int row, int col) {
        try { return m.getValueAt(row, col); } catch (Exception ex) { return null; }
    }

    private double parseAmount(Object amtObj) {
        if (amtObj == null) return 0.0;
        String s = amtObj.toString().trim();
        if (s.isEmpty()) return 0.0;
        // keep digits, sign, separators
        s = s.replaceAll("[^0-9,.-]", "");
        // handle "1.234,56" -> "1234.56" and "1,234.56" -> "1234.56"
        int lastComma = s.lastIndexOf(',');
        int lastDot = s.lastIndexOf('.');
        if (lastComma > lastDot) {
            s = s.replace(".", "");
            s = s.replace(",", ".");
        } else {
            s = s.replace(",", "");
        }
        try { return Double.parseDouble(s); } catch (Exception ex) { return 0.0; }
    }

    private LocalDate parseDateSafe(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return LocalDate.parse(s); }                               // yyyy-MM-dd
        catch (Exception ignored) {}
        try { return LocalDate.parse(s, DATE_FMT); }
        catch (Exception ignored) {}
        return null;
    }


    //region Getter & Setter

    public JPanel getRootPnl() {
        return rootPnl;
    }

    //endregion
}
