package com.inventaire;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Panneau d'affichage de l'historique des mouvements de stock.
 */
public class HistoryPanel extends JPanel {

    private final JTable table;
    private final HistoryTableModel tableModel;
    private final TableRowSorter<HistoryTableModel> sorter;

    private JComboBox<String> typeFilter;
    private JTextField        productFilter;
    private JSpinner          dateFrom;
    private JSpinner          dateTo;

    private final JLabel lblTotal   = new JLabel();
    private final JLabel lblIn      = new JLabel();
    private final JLabel lblOut     = new JLabel();
    private final JLabel lblBalance = new JLabel();

    private final MovementController movementController;

    public HistoryPanel(MovementController movementController) {
        this.movementController = movementController;
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel filterPanel = buildFilterPanel();
        add(filterPanel, BorderLayout.NORTH);

        tableModel = new HistoryTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getColumnModel().getColumn(1).setCellRenderer(new DateCellRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        int[] widths = {40, 145, 180, 110, 75, 70, 110, 200};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildStatsBar(), BorderLayout.SOUTH);

        typeFilter    = (JComboBox<String>) findNamed(filterPanel, "typeFilter");
        productFilter = (JTextField)        findNamed(filterPanel, "productFilter");
        dateFrom      = (JSpinner)          findNamed(filterPanel, "dateFrom");
        dateTo        = (JSpinner)          findNamed(filterPanel, "dateTo");

        refreshTable();
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Filtres"));

        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Tous les types", "Entrée", "Sortie"});
        typeBox.setName("typeFilter");
        typeBox.setPreferredSize(new Dimension(140, 28));

        JTextField prodField = new JTextField(14);
        prodField.setName("productFilter");
        prodField.putClientProperty("JTextField.placeholderText", "Nom / référence…");

        SpinnerDateModel fromModel = new SpinnerDateModel();
        JSpinner spFrom = new JSpinner(fromModel);
        spFrom.setName("dateFrom");
        spFrom.setEditor(new JSpinner.DateEditor(spFrom, "dd/MM/yyyy"));
        spFrom.setPreferredSize(new Dimension(110, 28));
        fromModel.setValue(java.sql.Date.valueOf(LocalDate.now().minusDays(30)));

        SpinnerDateModel toModel = new SpinnerDateModel();
        JSpinner spTo = new JSpinner(toModel);
        spTo.setName("dateTo");
        spTo.setEditor(new JSpinner.DateEditor(spTo, "dd/MM/yyyy"));
        spTo.setPreferredSize(new Dimension(110, 28));

        JButton btnFilter  = new JButton("🔍 Filtrer");
        JButton btnReset   = new JButton("✖ Réinitialiser");
        JButton btnExport  = new JButton("📤 Exporter CSV");

        panel.add(new JLabel("Type :"));   panel.add(typeBox);
        panel.add(new JLabel("  Produit :")); panel.add(prodField);
        panel.add(new JLabel("  Du :"));   panel.add(spFrom);
        panel.add(new JLabel("Au :"));     panel.add(spTo);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(btnFilter); panel.add(btnReset); panel.add(btnExport);

        btnFilter.addActionListener(e -> applyFilters());
        btnReset.addActionListener(e -> {
            typeBox.setSelectedIndex(0);
            prodField.setText("");
            fromModel.setValue(java.sql.Date.valueOf(LocalDate.now().minusDays(30)));
            toModel.setValue(java.sql.Date.valueOf(LocalDate.now()));
            refreshTable();
        });
        btnExport.addActionListener(e -> exportCsv());

        return panel;
    }

    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 4));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        lblIn.setForeground(new Color(0, 128, 0));
        lblOut.setForeground(new Color(180, 0, 0));
        for (JLabel l : new JLabel[]{lblTotal, lblIn, lblOut, lblBalance})
            l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        bar.add(lblTotal); bar.add(new JSeparator(SwingConstants.VERTICAL));
        bar.add(lblIn); bar.add(lblOut);
        bar.add(new JSeparator(SwingConstants.VERTICAL)); bar.add(lblBalance);
        return bar;
    }

    public void refreshTable() {
        tableModel.setMovements(movementController.getAllMovements());
        updateStats();
    }

    private void applyFilters() {
        String typeStr = typeFilter    != null ? (String) typeFilter.getSelectedItem() : "Tous les types";
        String prodStr = productFilter != null ? productFilter.getText().trim() : "";
        LocalDate from = toLocalDate(dateFrom != null ? (java.util.Date) dateFrom.getValue() : null,
                                     LocalDate.now().minusDays(30));
        LocalDate to   = toLocalDate(dateTo   != null ? (java.util.Date) dateTo.getValue()   : null,
                                     LocalDate.now());

        Movement.Type type = switch (typeStr) {
            case "Entrée" -> Movement.Type.IN;
            case "Sortie" -> Movement.Type.OUT;
            default       -> null;
        };
        tableModel.setMovements(movementController.getMovementsFiltered(
            type, prodStr, from.atStartOfDay(), to.atTime(LocalTime.MAX)));
        updateStats();
    }

    private void updateStats() {
        int total = tableModel.getMovementCount();
        long inQ = 0, outQ = 0;
        for (int r = 0; r < total; r++) {
            Movement m = tableModel.getMovementAt(r);
            if (m.getType() == Movement.Type.IN) inQ += m.getQuantity();
            else outQ += m.getQuantity();
        }
        int bal = tableModel.getNetBalance();
        lblTotal.setText("  Total : " + total + " mvt.");
        lblIn.setText("Entrées : +" + inQ);
        lblOut.setText("Sorties : -" + outQ);
        lblBalance.setText("Solde net : " + (bal >= 0 ? "+" : "") + bal);
        lblBalance.setForeground(bal >= 0 ? new Color(0, 100, 0) : new Color(160, 0, 0));
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("historique_stock.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.FileWriter(chooser.getSelectedFile()))) {
            pw.println("ID;Date;Produit;Référence;Type;Quantité;Utilisateur;Note");
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    if (c > 0) sb.append(';');
                    Object v = tableModel.getValueAt(r, c);
                    sb.append(v != null ? v.toString().replace(";", ",") : "");
                }
                pw.println(sb);
            }
            JOptionPane.showMessageDialog(this, "Export réussi : " + chooser.getSelectedFile().getName(),
                "Export CSV", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate toLocalDate(java.util.Date d, LocalDate fallback) {
        if (d == null) return fallback;
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    @SuppressWarnings("unchecked")
    private <T extends Component> T findNamed(Container c, String name) {
        for (Component child : c.getComponents()) {
            if (name.equals(child.getName())) return (T) child;
            if (child instanceof Container sub) { T f = findNamed(sub, name); if (f != null) return f; }
        }
        return null;
    }
}
