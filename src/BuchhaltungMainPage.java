import java.awt.*;
import javax.swing.*;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import java.util.Locale;
import java.time.LocalDate;

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

    public BuchhaltungMainPage() {
        configurePickers();
        wireListeners();

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


    public JPanel getRootPnl() {
        return rootPnl;
    }
}
