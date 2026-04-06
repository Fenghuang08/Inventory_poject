package com.inventaire;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

/**
 * Dialogue de création / modification d'un compte utilisateur.
 */
public class UserDialog extends JDialog {

    private final JTextField     tfUsername = new JTextField(20);
    private final JTextField     tfFullName = new JTextField(20);
    private final JTextField     tfEmail    = new JTextField(20);
    private final JComboBox<String> cbRole  = new JComboBox<>(User.Role.labelArray());
    private final JCheckBox      chkActive  = new JCheckBox("Compte actif", true);
    private final JPasswordField pfPassword = new JPasswordField(20);
    private final JPasswordField pfConfirm  = new JPasswordField(20);

    private boolean saved = false;
    private final User           editUser;
    private final UserController controller;

    public UserDialog(JFrame parent, UserController controller, User user) {
        super(parent, user == null ? "Nouvel utilisateur" : "Modifier l'utilisateur", true);
        this.controller = controller;
        this.editUser   = user;

        setLayout(new BorderLayout(0, 8));
        JPanel form = buildForm();
        form.setBorder(new EmptyBorder(14, 16, 6, 16));
        add(form, BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);

        if (editUser != null) populateForm(editUser);

        pack();
        setMinimumSize(new Dimension(400, editUser == null ? 420 : 300));
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        addRow(p, c, row++, "Nom d'utilisateur * :", tfUsername);
        addRow(p, c, row++, "Nom complet :",          tfFullName);
        addRow(p, c, row++, "Adresse e-mail :",        tfEmail);
        addRow(p, c, row++, "Rôle :",                  cbRole);
        addRow(p, c, row++, "",                        chkActive);

        // Section mot de passe uniquement en création
        if (editUser == null) {
            c.gridx = 0; c.gridy = row; c.gridwidth = 2; c.weightx = 1;
            p.add(buildPasswordSection(), c);
        }
        return p;
    }

    private JPanel buildPasswordSection() {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Mot de passe"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(section, c, 0, "Mot de passe * :", pfPassword);
        addRow(section, c, 1, "Confirmer * :",    pfConfirm);

        JProgressBar strength = new JProgressBar(0, 4);
        strength.setStringPainted(true);
        strength.setString("Saisissez un mot de passe");
        pfPassword.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            void update() {
                int score = passwordStrength(new String(pfPassword.getPassword()));
                strength.setValue(score);
                String[] lbs = {"Très faible","Faible","Moyen","Fort","Très fort"};
                Color[]  cls = {Color.RED, new Color(220,120,0), Color.ORANGE,
                                new Color(0,160,0), new Color(0,100,0)};
                if (score < lbs.length) { strength.setString(lbs[score]); strength.setForeground(cls[score]); }
            }
        });
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        section.add(strength, c);
        return section;
    }

    private void addRow(JPanel panel, GridBagConstraints c,
                        int row, String text, JComponent field) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; c.gridwidth = 1;
        panel.add(lbl(text), c);
        c.gridx = 1; c.weightx = 1;
        panel.add(field, c);
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setPreferredSize(new Dimension(160, 24));
        return l;
    }

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        JButton btnSave   = new JButton(editUser == null ? "➕ Créer" : "✅ Enregistrer");
        JButton btnCancel = new JButton("Annuler");
        btnSave.setBackground(new Color(46, 139, 87));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setContentAreaFilled(true);  // ← AJOUTER
        btnSave.setFont(btnSave.getFont().deriveFont(Font.BOLD));
        btnSave.addActionListener(e -> save());
        btnCancel.addActionListener(e -> dispose());
        bar.add(btnCancel); bar.add(btnSave);
        return bar;
    }

    private void populateForm(User u) {
        tfUsername.setText(u.getUsername());
        tfFullName.setText(u.getFullName());
        tfEmail.setText(u.getEmail());
        cbRole.setSelectedItem(u.getRole().getLabel());
        chkActive.setSelected(u.isActive());
    }

    private void save() {
        String username = tfUsername.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom d'utilisateur est obligatoire.",
                "Champ manquant", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (editUser == null) {
            char[] pwd = pfPassword.getPassword();
            char[] cfm = pfConfirm.getPassword();
            if (pwd.length < 6) {
                JOptionPane.showMessageDialog(this, "Le mot de passe doit contenir au moins 6 caractères.",
                    "Mot de passe trop court", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!Arrays.equals(pwd, cfm)) {
                JOptionPane.showMessageDialog(this, "Les mots de passe ne correspondent pas.",
                    "Erreur", JOptionPane.WARNING_MESSAGE);
                pfConfirm.setText("");
                return;
            }
        }
        User u = editUser != null ? editUser : new User();
        u.setUsername(username);
        u.setFullName(tfFullName.getText().trim());
        u.setEmail(tfEmail.getText().trim());
        u.setRole(User.Role.fromLabel((String) cbRole.getSelectedItem()));
        u.setActive(chkActive.isSelected());

        if (editUser == null) {
            u.setRawPassword(new String(pfPassword.getPassword()));
            controller.addUser(u);
        } else {
            controller.updateUser(u);
        }
        saved = true;
        dispose();
    }

    private int passwordStrength(String pwd) {
        if (pwd.isEmpty()) return 0;
        int s = 0;
        if (pwd.length() >= 8)           s++;
        if (pwd.matches(".*[A-Z].*"))    s++;
        if (pwd.matches(".*[0-9].*"))    s++;
        if (pwd.matches(".*[^a-zA-Z0-9].*")) s++;
        return s;
    }

    public boolean isSaved() { return saved; }
}
