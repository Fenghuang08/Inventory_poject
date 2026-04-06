package com.inventaire;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Panneau des paramètres de l'application.
 */
public class SettingsPanel extends JPanel {

    // ── Champs ──────────────────────────────────────────────────────────────
    private final JTextField     companyName    = new JTextField(24);
    private final JTextField     companyAddress = new JTextField(24);
    private final JTextField     companyPhone   = new JTextField(16);
    private final JTextField     companyEmail   = new JTextField(24);

    private final JTextField     dbHost     = new JTextField("localhost", 16);
    private final JSpinner       dbPort     = new JSpinner(new SpinnerNumberModel(3306, 1, 65535, 1));
    private final JTextField     dbName     = new JTextField("inventaire_db", 16);
    private final JTextField     dbUser     = new JTextField("root", 16);
    private final JPasswordField dbPassword = new JPasswordField(16);

    private final JSpinner       lowStockThreshold = new JSpinner(new SpinnerNumberModel(5, 0, 9999, 1));
    private final JCheckBox      emailAlerts       = new JCheckBox("Activer les alertes par e-mail");
    private final JTextField     alertEmail        = new JTextField(24);

    private final JComboBox<String> themeSelector    = new JComboBox<>(new String[]{"Système", "Clair", "Sombre"});
    private final JComboBox<String> languageSelector = new JComboBox<>(new String[]{"Français", "English", "Español"});
    private final JSpinner          rowsPerPage      = new JSpinner(new SpinnerNumberModel(25, 10, 200, 5));

    private final AppController appController;

    public SettingsPanel(AppController appController) {
        this.appController = appController;
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("🏢 Entreprise",      buildCompanySection());
        tabs.addTab("🗄 Base de données", buildDatabaseSection());
        tabs.addTab("🔔 Alertes",         buildAlertsSection());
        tabs.addTab("🎨 Apparence",       buildAppearanceSection());
        tabs.addTab("💾 Sauvegarde",      buildBackupSection());

        add(tabs, BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);
        loadSettings();
    }

    private JPanel buildCompanySection() {
        JPanel p = grid("Informations de l'entreprise", 4);
        p.add(lbl("Nom de l'entreprise :")); p.add(companyName);
        p.add(lbl("Adresse :"));            p.add(companyAddress);
        p.add(lbl("Téléphone :"));          p.add(companyPhone);
        p.add(lbl("E-mail :"));             p.add(companyEmail);
        return wrap(p);
    }

    private JPanel buildDatabaseSection() {
        JPanel p = grid("Connexion à la base de données", 5);
        p.add(lbl("Hôte :"));        p.add(dbHost);
        p.add(lbl("Port :"));        p.add(dbPort);
        p.add(lbl("Nom de la BDD:")); p.add(dbName);
        p.add(lbl("Utilisateur :"));  p.add(dbUser);
        p.add(lbl("Mot de passe :")); p.add(dbPassword);

        JPanel w = wrap(p);
        JButton btnTest = new JButton("🔌 Tester la connexion");
        btnTest.addActionListener(e -> testDbConnection());
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(btnTest);
        w.add(row, BorderLayout.SOUTH);
        return w;
    }

    private JPanel buildAlertsSection() {
        JPanel p = grid("Paramètres d'alertes", 3);
        p.add(lbl("Seuil stock faible :"));    p.add(lowStockThreshold);
        p.add(lbl(""));                         p.add(emailAlerts);
        p.add(lbl("E-mail de notification :")); p.add(alertEmail);
        emailAlerts.addActionListener(e -> alertEmail.setEnabled(emailAlerts.isSelected()));
        alertEmail.setEnabled(false);
        return wrap(p);
    }

    private JPanel buildAppearanceSection() {
        JPanel p = grid("Préférences d'affichage", 3);
        p.add(lbl("Thème :"));           p.add(themeSelector);
        p.add(lbl("Langue :"));          p.add(languageSelector);
        p.add(lbl("Lignes par page :")); p.add(rowsPerPage);
        return wrap(p);
    }

    private JPanel buildBackupSection() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JTextArea info = new JTextArea(
            "Les sauvegardes exportent l'intégralité de la base de données\n"
            + "dans un fichier SQL compressé. Planifiez des sauvegardes\n"
            + "automatiques ou lancez-en une manuellement ci-dessous.");
        info.setEditable(false);
        info.setBackground(UIManager.getColor("Panel.background"));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnNow     = new JButton("💾 Sauvegarder maintenant");
        JButton btnRestore = new JButton("📂 Restaurer…");
        btnNow.addActionListener(e -> backupNow());
        btnRestore.addActionListener(e -> restore());
        btns.add(btnNow); btns.add(btnRestore);
        outer.add(info, BorderLayout.NORTH);
        outer.add(btns, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        JButton btnSave   = new JButton("✅ Enregistrer");
        JButton btnCancel = new JButton("✖ Annuler");
        btnSave.setBackground(new Color(46, 139, 87));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);             // ← AJOUTER
        btnSave.setContentAreaFilled(true);
        btnSave.setFont(btnSave.getFont().deriveFont(Font.BOLD));
        btnSave.addActionListener(e -> saveSettings());
        btnCancel.addActionListener(e -> loadSettings());
        bar.add(btnCancel); bar.add(btnSave);
        return bar;
    }

    private void loadSettings() {
        Map<String, String> cfg = appController.getSettings();
        companyName.setText(cfg.getOrDefault("company.name", ""));
        companyAddress.setText(cfg.getOrDefault("company.address", ""));
        companyPhone.setText(cfg.getOrDefault("company.phone", ""));
        companyEmail.setText(cfg.getOrDefault("company.email", ""));
        dbHost.setText(cfg.getOrDefault("db.host", "localhost"));
        dbName.setText(cfg.getOrDefault("db.name", "inventaire_db"));
        dbUser.setText(cfg.getOrDefault("db.user", "root"));
        lowStockThreshold.setValue(Integer.parseInt(cfg.getOrDefault("alert.lowstock", "5")));
        boolean mailOn = Boolean.parseBoolean(cfg.getOrDefault("alert.email.enabled", "false"));
        emailAlerts.setSelected(mailOn);
        alertEmail.setText(cfg.getOrDefault("alert.email.address", ""));
        alertEmail.setEnabled(mailOn);
        themeSelector.setSelectedItem(cfg.getOrDefault("ui.theme", "Système"));
        languageSelector.setSelectedItem(cfg.getOrDefault("ui.language", "Français"));
    }

    private void saveSettings() {
        Map<String, String> cfg = new HashMap<>();
        cfg.put("company.name",    companyName.getText().trim());
        cfg.put("company.address", companyAddress.getText().trim());
        cfg.put("company.phone",   companyPhone.getText().trim());
        cfg.put("company.email",   companyEmail.getText().trim());
        cfg.put("db.host",  dbHost.getText().trim());
        cfg.put("db.name",  dbName.getText().trim());
        cfg.put("db.user",  dbUser.getText().trim());
        cfg.put("db.password", new String(dbPassword.getPassword()));
        cfg.put("alert.lowstock",      lowStockThreshold.getValue().toString());
        cfg.put("alert.email.enabled", String.valueOf(emailAlerts.isSelected()));
        cfg.put("alert.email.address", alertEmail.getText().trim());
        cfg.put("ui.theme",    (String) themeSelector.getSelectedItem());
        cfg.put("ui.language", (String) languageSelector.getSelectedItem());
        appController.saveSettings(cfg);
        JOptionPane.showMessageDialog(this, "Paramètres enregistrés avec succès.",
            "Paramètres", JOptionPane.INFORMATION_MESSAGE);
    }

    private void testDbConnection() {
        boolean ok = appController.testDatabaseConnection(
            dbHost.getText(), (int) dbPort.getValue(),
            dbName.getText(), dbUser.getText(), new String(dbPassword.getPassword()));
        JOptionPane.showMessageDialog(this,
            ok ? "Connexion réussie ✅" : "Impossible de se connecter ❌\nVérifiez les paramètres.",
            "Test BDD", ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    private void backupNow() {
        JFileChooser c = new JFileChooser();
        c.setSelectedFile(new java.io.File("backup_inventaire.sql"));
        if (c.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            boolean ok = appController.backup(c.getSelectedFile().getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                ok ? "Sauvegarde créée : " + c.getSelectedFile().getName() : "Échec de la sauvegarde.",
                "Sauvegarde", ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restore() {
        JFileChooser c = new JFileChooser();
        if (c.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            if (JOptionPane.showConfirmDialog(this,
                    "⚠️ Cette opération remplacera toutes les données.\nContinuer ?",
                    "Restauration", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
                appController.restore(c.getSelectedFile().getAbsolutePath());
        }
    }

    // ── Utilitaires UI ───────────────────────────────────────────────────────
    private JPanel grid(String title, int rows) {
        JPanel p = new JPanel(new GridLayout(rows, 2, 10, 8));
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), title, TitledBorder.LEFT, TitledBorder.TOP));
        return p;
    }
    private JPanel wrap(JPanel inner) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        outer.add(inner, BorderLayout.NORTH);
        return outer;
    }
    private JLabel lbl(String text) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 13f));
        return l;
    }
}
