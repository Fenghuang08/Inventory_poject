package com.inventaire;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialogue de saisie d'un mouvement de stock (entrée ou sortie).
 */
public class MovementDialog extends JDialog {

    private final JLabel     lblProductName;
    private final JLabel     lblCurrentStock;
    private final JLabel     lblNewStock;
    private final JRadioButton rbIn;
    private final JRadioButton rbOut;
    private final JSpinner   spQuantity;
    private final JTextField tfNote;
    private final JComboBox<String> cbReason;

    private boolean saved = false;
    private final Product           product;
    private final ProductController controller;

    private static final String[] IN_REASONS  =
        {"Réapprovisionnement", "Retour client", "Correction inventaire", "Don reçu", "Autre"};
    private static final String[] OUT_REASONS =
        {"Vente", "Consommation interne", "Casse / perte", "Correction inventaire", "Autre"};

    public MovementDialog(JFrame parent, ProductController controller, Product product) {
        super(parent, "Enregistrer un mouvement", true);
        this.controller = controller;
        this.product    = product;

        lblProductName  = new JLabel(product.getName() + "  [" + product.getReference() + "]");
        lblProductName.setFont(lblProductName.getFont().deriveFont(Font.BOLD, 14f));

        lblCurrentStock = new JLabel(String.valueOf(product.getQuantity()));
        lblCurrentStock.setFont(lblCurrentStock.getFont().deriveFont(Font.BOLD, 13f));
        lblCurrentStock.setForeground(stockColor(product.getQuantity(), product.getMinQuantity()));

        lblNewStock = new JLabel(String.valueOf(product.getQuantity()));
        lblNewStock.setFont(lblNewStock.getFont().deriveFont(Font.BOLD, 13f));

        rbIn  = new JRadioButton("Entrée ⬆", true);
        rbOut = new JRadioButton("Sortie ⬇");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbIn); bg.add(rbOut);

        spQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
        cbReason   = new JComboBox<>(IN_REASONS);
        cbReason.setEditable(true);
        tfNote = new JTextField(22);
        tfNote.putClientProperty("JTextField.placeholderText", "Remarque optionnelle…");

        rbIn.addActionListener(e  -> { cbReason.setModel(new DefaultComboBoxModel<>(IN_REASONS));  updatePreview(); });
        rbOut.addActionListener(e -> { cbReason.setModel(new DefaultComboBoxModel<>(OUT_REASONS)); updatePreview(); });
        spQuantity.addChangeListener(e -> updatePreview());

        setLayout(new BorderLayout(0, 8));
        JPanel form = buildForm();
        form.setBorder(new EmptyBorder(14, 16, 6, 16));
        add(form, BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(400, 320));
        setLocationRelativeTo(parent);
        setResizable(false);
        updatePreview();
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; p.add(lblProductName, c);
        c.gridwidth = 1;
        addRow(p, c, 1, "Stock actuel :", lblCurrentStock);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; p.add(new JSeparator(), c);
        c.gridwidth = 1;

        c.gridx = 0; c.gridy = 3; p.add(lbl("Type :"), c);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rbIn.setForeground(new Color(0, 128, 0));
        rbOut.setForeground(new Color(160, 0, 0));

        radioPanel.add(rbIn); radioPanel.add(rbOut);
        c.gridx = 1; p.add(radioPanel, c);

        addRow(p, c, 4, "Quantité :", spQuantity);
        addRow(p, c, 5, "Raison :",   cbReason);
        addRow(p, c, 6, "Note :",     tfNote);
        addRow(p, c, 7, "Nouveau stock :", lblNewStock);
        return p;
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String text, JComponent field) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; p.add(lbl(text), c);
        c.gridx = 1; c.weightx = 1; p.add(field, c);
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setPreferredSize(new Dimension(130, 24));
        return l;
    }

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        JButton btnSave   = new JButton("✅ Valider le mouvement");
        JButton btnCancel = new JButton("Annuler");
        btnSave.setBackground(new Color(70, 130, 180));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setContentAreaFilled(true);
        btnSave.setFont(btnSave.getFont().deriveFont(Font.BOLD));
        btnSave.addActionListener(e -> save());
        btnCancel.addActionListener(e -> dispose());
        bar.add(btnCancel); bar.add(btnSave);
        return bar;
    }

    private void updatePreview() {
        int qty    = (int) spQuantity.getValue();
        int newQty = rbIn.isSelected() ? product.getQuantity() + qty : product.getQuantity() - qty;
        lblNewStock.setText(newQty + (newQty < 0 ? "  ⚠️ Stock négatif !" : ""));
        lblNewStock.setForeground(newQty < product.getMinQuantity()
            ? new Color(180, 60, 0) : new Color(0, 100, 0));
    }

    private void save() {
        int qty   = (int) spQuantity.getValue();
        boolean isOut = rbOut.isSelected();
        if (isOut && qty > product.getQuantity()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "La quantité demandée (" + qty + ") dépasse le stock (" + product.getQuantity() + ").\nForcer ?",
                "Stock insuffisant", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
        }
        Movement m = new Movement();
        m.setProductId(product.getId());
        m.setType(isOut ? Movement.Type.OUT : Movement.Type.IN);
        m.setQuantity(qty);
        m.setDateTime(java.time.LocalDateTime.now());
        String reason = cbReason.getSelectedItem() != null ? cbReason.getSelectedItem().toString() : "";
        String note   = tfNote.getText().trim();
        m.setNote(reason + (note.isEmpty() ? "" : " – " + note));
        controller.addMovement(m);
        saved = true;
        dispose();
    }

    private Color stockColor(int qty, int min) {
        if (qty <= 0)   return new Color(180, 0, 0);
        if (qty <= min) return new Color(200, 100, 0);
        return new Color(0, 110, 0);
    }

    public boolean isSaved() { return saved; }
}
