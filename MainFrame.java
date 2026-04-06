package com.inventaire;

import javax.swing.*;
import java.awt.*;

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

        // ── Icône (optionnelle) ──────────────────────────────────────────────
        // setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());

        // ── Panneaux principaux ──────────────────────────────────────────────
        inventoryPanel = new InventoryPanel(appController.getProductController());
        historyPanel   = new HistoryPanel(appController.getMovementController());
        usersPanel     = new UsersPanel(appController.getUserController());
        settingsPanel  = new SettingsPanel(appController);

        // ── Onglets ──────────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📦 Inventaire",  inventoryPanel);
        tabs.addTab("📋 Historique",  historyPanel);
        tabs.addTab("👥 Utilisateurs",usersPanel);
        tabs.addTab("⚙️ Paramètres", settingsPanel);

        // Rafraîchir l'historique quand on bascule sur cet onglet
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 1) historyPanel.refreshTable();
            if (idx == 2) usersPanel.refreshTable();
        });

        // ── Barre de menu ────────────────────────────────────────────────────
        setJMenuBar(buildMenuBar());

        // ── Barre de statut ──────────────────────────────────────────────────
        JLabel statusBar = new JLabel("  Inventaire-v2  |  Prêt");
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        statusBar.setFont(statusBar.getFont().deriveFont(Font.ITALIC, 11f));

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        pack();
    }

    // ── Menu ─────────────────────────────────────────────────────────────────
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        // Fichier
        JMenu fichier = new JMenu("Fichier");
        JMenuItem miExport  = new JMenuItem("📤 Exporter CSV…");
        JMenuItem miBackup  = new JMenuItem("💾 Sauvegarder…");
        JMenuItem miQuitter = new JMenuItem("✖ Quitter");
        miQuitter.addActionListener(e -> System.exit(0));
        fichier.add(miExport); fichier.add(miBackup);
        fichier.addSeparator(); fichier.add(miQuitter);

        // Affichage
        JMenu affichage = new JMenu("Affichage");
        JMenuItem miRefresh = new JMenuItem("↺ Actualiser tout");
        miRefresh.addActionListener(e -> {
            inventoryPanel.refreshAllTabs();
            historyPanel.refreshTable();
            usersPanel.refreshTable();
        });
        affichage.add(miRefresh);

        // Aide
        JMenu aide = new JMenu("Aide");
        JMenuItem miAbout = new JMenuItem("À propos");
        miAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Inventaire-v2\nApplication de gestion de stock\nVersion 2.0",
            "À propos", JOptionPane.INFORMATION_MESSAGE));
        aide.add(miAbout);

        bar.add(fichier); bar.add(affichage); bar.add(aide);
        return bar;
    }
}
