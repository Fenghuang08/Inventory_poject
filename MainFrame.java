package com.inventaire;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainFrame extends JFrame {

    private final AppController appController;

    private final InventoryPanel inventoryPanel;
    private final HistoryPanel   historyPanel;
    private final UsersPanel     usersPanel;
    private final SettingsPanel  settingsPanel;

    public MainFrame(AppController appController) {
        super("Inventaire-v2");
        this.appController = appController;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        inventoryPanel = new InventoryPanel(appController.getProductController());
        historyPanel   = new HistoryPanel(appController.getMovementController());
        usersPanel     = new UsersPanel(appController.getUserController());
        settingsPanel  = new SettingsPanel(appController);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Inventaire",   inventoryPanel);
        tabs.addTab("Historique",   historyPanel);
        tabs.addTab("Utilisateurs", usersPanel);
        tabs.addTab("Parametres",   settingsPanel);

        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 1) historyPanel.refreshTable();
            if (idx == 2) usersPanel.refreshTable();
        });

        setJMenuBar(buildMenuBar());

        JLabel statusBar = new JLabel("  Inventaire-v2  |  Pret");
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        statusBar.setFont(statusBar.getFont().deriveFont(Font.ITALIC, 11f));

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        pack();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        // ── Fichier ──────────────────────────────────────────────────────────
        JMenu fichier = new JMenu("Fichier");

        JMenuItem miExportExcel = new JMenuItem("Exporter vers Excel (.xlsx)");
        miExportExcel.addActionListener(e ->
            ExcelExporter.exportWithDialog(this, appController.getProductController()));

        JMenuItem miExportCsv = new JMenuItem("Exporter vers CSV");
        miExportCsv.addActionListener(e -> exportCsv());

        JMenuItem miBackup  = new JMenuItem("Sauvegarder");
        miBackup.addActionListener(e -> appController.backup("backup.sql"));

        JMenuItem miQuitter = new JMenuItem("Quitter");
        miQuitter.addActionListener(e -> System.exit(0));

        fichier.add(miExportExcel);
        fichier.add(miExportCsv);
        fichier.add(miBackup);
        fichier.addSeparator();
        fichier.add(miQuitter);

        // ── Affichage ────────────────────────────────────────────────────────
        JMenu affichage = new JMenu("Affichage");
        JMenuItem miRefresh = new JMenuItem("Actualiser tout");
        miRefresh.addActionListener(e -> {
            inventoryPanel.refreshAllTabs();
            historyPanel.refreshTable();
            usersPanel.refreshTable();
        });
        affichage.add(miRefresh);

        // ── Aide ─────────────────────────────────────────────────────────────
        JMenu aide = new JMenu("Aide");
        JMenuItem miAbout = new JMenuItem("A propos");
        miAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Inventaire-v2\nApplication de gestion de stock\nVersion 2.0",
            "A propos", JOptionPane.INFORMATION_MESSAGE));
        aide.add(miAbout);

        bar.add(fichier);
        bar.add(affichage);
        bar.add(aide);
        return bar;
    }

    // ── Export CSV ───────────────────────────────────────────────────────────
    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exporter vers CSV");
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        chooser.setSelectedFile(new File("inventaire_" + ts + ".csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("Fichier CSV (*.csv)", "csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".csv"))
            file = new File(file.getAbsolutePath() + ".csv");

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8))) {
            // BOM UTF-8 pour Excel
            pw.print('\uFEFF');

            // En-tête
            pw.println("ID;Reference;Nom;Description;Categorie;Quantite;Qte min.;Prix (EUR);Statut");

            // Données
            List<Product> products = appController.getProductController().getAllProducts();
            for (Product p : products) {
                pw.println(
                    p.getId() + ";" +
                    csv(p.getReference()) + ";" +
                    csv(p.getName()) + ";" +
                    csv(p.getDescription()) + ";" +
                    csv(p.getCategory()) + ";" +
                    p.getQuantity() + ";" +
                    p.getMinQuantity() + ";" + csv(p.getStorageLocation()) + ";" +
                    csv(p.getStatus())
                );
            }

            JOptionPane.showMessageDialog(this,
                "Export CSV reussi !\n" + file.getName() + "\n"
                + products.size() + " produit(s) exporte(s)",
                "Export CSV", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de l'export : " + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Échappe une valeur pour CSV (guillemets si contient ; ou ") */
    private String csv(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
