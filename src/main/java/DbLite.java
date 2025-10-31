import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDate;

public final class DbLite {
    private static final String URL = "jdbc:sqlite:app.db"; // DB file next to your JAR

    private DbLite() {}

    // call once at startup
    public static void init() {
        try (Connection c = DriverManager.getConnection(URL);
             Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS entry(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  belegnr TEXT NOT NULL,
                  datum TEXT NOT NULL,
                  kategorie TEXT NOT NULL,
                  beschreibung TEXT,
                  betrag_cents INTEGER NOT NULL
                )
            """);
            st.execute("CREATE INDEX IF NOT EXISTS idx_entry_date ON entry(datum)");
        } catch (SQLException e) {
            throw new RuntimeException("DB init failed: " + e.getMessage(), e);
        }
    }

    // write
    public static void insert(String belegnr, LocalDate datum, String kategorie, String beschreibung, int betragCents) {
        String sql = "INSERT INTO entry(belegnr,datum,kategorie,beschreibung,betrag_cents) VALUES(?,?,?,?)";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, belegnr);
            ps.setString(2, datum.toString());
            ps.setString(3, kategorie);
            ps.setString(4, beschreibung);
            ps.setInt(5, betragCents);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Insert failed: " + e.getMessage(), e);
        }
    }

    // read -> ready-to-use JTable model
    public static DefaultTableModel tableModelAll() {
        String[] cols = {"ID","Beleg","Datum","Kategorie","Beschreibung","Betrag (€)"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        String sql = "SELECT id,belegnr,datum,kategorie,beschreibung,betrag_cents FROM entry ORDER BY date(datum), id";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int cents = rs.getInt("betrag_cents");
                m.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("belegnr"),
                        rs.getString("datum"),
                        rs.getString("kategorie"),
                        rs.getString("beschreibung"),
                        centsToEuroString(cents)
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + e.getMessage(), e);
        }
        return m;
    }

    public static String centsToEuroString(int cents) {
        boolean neg = cents < 0; int abs = Math.abs(cents);
        int eur = abs / 100; int ct = abs % 100;
        return (neg ? "-" : "") + eur + "," + String.format("%02d", ct);
    }
}
