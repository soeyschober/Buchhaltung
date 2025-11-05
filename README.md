# Buchhaltung

Author: Soey Schober 

LBS Eibiswald | 2aAPC

Dieses Projekt ist eine einfache Buchhaltungs-Anwendung in Java mit Swing-Oberfläche und SQLite-Datenbank. Fokus: übersichtliche UI, schnelle Eingabe von Buchungen, Filter nach Zeitraum/Kategorie, Suche und Saldo-Berechnung. Die App zeigt grundlegende Java-Konzepte wie GUI, Events, Tabellen/Renderer sowie eine schlanke Persistenzschicht über JDBC.

## Verwendung

### Technologien im Einsatz

- Java

- IntelliJ IDEA

- Swing UI Designer (.form)

- JDBC (SQLite)

- LGoodDatePicker

- Nimbus Look & Feel

### Start der Anwendung

Main.java ausführen. Beim Start wird die Datenbank initialisiert und das Hauptfenster Buchhaltung geöffnet.

Neue Einträge können über den entsprechenden Button/Dialog erfasst werden; die Tabelle aktualisiert sich danach automatisch.

**Hinweise:**

Die SQLite-Datei app.db liegt im Projektverzeichnis neben dem JAR/den Klassen.

Beim ersten Start wird die Tabelle automatisch erstellt, falls sie nicht existiert.


## Output / Screens

### Hauptfenster mit Filter und Tabelle

<img width="914" height="452" alt="image" src="https://github.com/user-attachments/assets/c107ee6a-6bbe-4074-a012-d4fc971108c3" />


### Eingabedialog

<img width="916" height="449" alt="image" src="https://github.com/user-attachments/assets/69239d16-2cfa-4135-b4ad-84b69addba07" />


### Datumauswahl durch Library
Ausgegraute Felder dürfen/können nicht ausgewählt werden, um Fehler zu vermeiden.
Sonst könnenten Abfragen wie "Alle Einträge vom 25.10.2025 bis zum 15.09.2025" - das wär sozusagen 'Minus Zeit' und schmeisst einen Error.
<img width="1080" height="576" alt="image" src="https://github.com/user-attachments/assets/058b2b6c-e636-4b73-80f8-19e7b7d2c478" />
