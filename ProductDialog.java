package com.inventaire;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ProductDialog extends JDialog {

    private final JTextField        tfReference;
    private final JTextField        tfName;
    private final JTextArea         taDescription;
    private final JComboBox<String> cbCategory;
    private final JSpinner          spQuantity;
    private final JSpinner          spMinQuantity;
    private final JTextField        tfStorageLocation;  // ancien : spPrice
    private final JComboBox<String> cbStatus;

    private boolean saved = false;
    private final Product editProduct;
    private final ProductController controller;

    public ProductDialog(JFrame parent, ProductController controller, Product product) {
        super(parent, product == null ? "Nouveau produit" : "Modifier un produit", true);
        this.controller  = controller;
        this.editProduct = product;

        // ── Référence auto ───────────────────────────────────────────────────
        tfReference = new JTextField(20);
        if (product == null) {
            tfReference.setText(controller.generateNextReference());
            tfReference.setEditable(false);
            tfReference.setBackground(new Color(240, 240, 240));
            tfReference.setToolTipText("Reference generee automatiquement");
        } else {
            tfReference.setText(product.getReference());
        }

        tfName            = new JTextField(20);
        taDescription     = new JTextArea(3, 20);
        taDescription.setLineWrap(true); taDescription.setWrapStyleWord(true);

        List<String> categories = controller.getAllCategories();
        cbCategory = new JComboBox<>(categories.toArray(new String[0]));
        cbCategory.setEditable(true);

        spQuantity        = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
        spMinQuantity     = new JSpinner(new SpinnerNumberModel(5, 0, 999999, 1));
        tfStorageLocation = new JTextField(20);
        tfStorageLocation.putClientProperty("JTextField.placeholderText", "Ex: Etagere A3, Armoire 2...");

        cbStatus = new JComboBox<>(new String[]{
            Product.STATUS_ACTIVE, Product.STATUS_INACTIVE, Product.STATUS_DISCONTINUED
        });

        setLayout(new BorderLayout(0, 8));
        JPanel form = buildForm();
        form.setBorder(new EmptyBorder(14, 16, 6, 16));
        add(form, BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);

        if (editProduct != null) populateForm(editProduct);

        pack();
        setMinimumSize(new Dimension(440, 400));
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        // Référence avec badge AUTO
        c.gridx = 0; c.gridy = row; c.weightx = 0; p.add(lbl("Reference :"), c);
        c.gridx = 1; c.weightx = 1;
        JPanel refPanel = new JPanel(new BorderLayout(6, 0));
        refPanel.add(tfReference, BorderLayout.CENTER);
        if (editProduct == null) {
            JLabel autoLabel = new JLabel("AUTO");
            autoLabel.setFont(autoLabel.getFont().deriveFont(Font.BOLD, 10f));
            autoLabel.setForeground(Color.WHITE);
            autoLabel.setBackground(new Color(70, 130, 180));
            autoLabel.setOpaque(true);
            autoLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            refPanel.add(autoLabel, BorderLayout.EAST);
        }
        p.add(refPanel, c); row++;

        addRow(p, c, row++, "Nom * :",              tfName);

        c.gridx = 0; c.gridy = row; c.weightx = 0;
        p.add(lbl("Description :"), c);
        c.gridx = 1; c.weightx = 1;
        p.add(new JScrollPane(taDescription,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), c);
        row++;

        addRow(p, c, row++, "Categorie :",          cbCategory);
        addRow(p, c, row++, "Quantite initiale :",   spQuantity);
        addRow(p, c, row++, "Quantite minimale :",   spMinQuantity);
        addRow(p, c, row++, "Lieu de stockage :",    tfStorageLocation);  // ← ICI
        addRow(p, c, row,   "Statut :",              cbStatus);
        return p;
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String text, JComponent field) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; p.add(lbl(text), c);
        c.gridx = 1; c.weightx = 1; p.add(field, c);
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setPreferredSize(new Dimension(150, 24));
        return l;
    }

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        JButton btnSave   = new JButton(editProduct == null ? "Creer" : "Enregistrer");
        JButton btnCancel = new JButton("Annuler");
        btnSave.setOpaque(true);
        btnSave.setContentAreaFilled(true);
        btnSave.setBorderPainted(false);
        btnSave.setBackground(new Color(46, 139, 87));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(btnSave.getFont().deriveFont(Font.BOLD));
        btnSave.addActionListener(e -> save());
        btnCancel.addActionListener(e -> dispose());
        bar.add(btnCancel); bar.add(btnSave);
        return bar;
    }

    private void populateForm(Product p) {
        tfReference.setText(p.getReference());
        tfName.setText(p.getName());
        taDescription.setText(p.getDescription());
        cbCategory.setSelectedItem(p.getCategory());
        spQuantity.setValue(p.getQuantity());
        spMinQuantity.setValue(p.getMinQuantity());
        tfStorageLocation.setText(p.getStorageLocation());
        cbStatus.setSelectedItem(p.getStatus());
    }

    private void save() {
        String ref  = tfReference.getText().trim();
        String name = tfName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom est obligatoire.",
                "Champ manquant", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (controller.referenceExists(ref) &&
                (editProduct == null || !ref.equalsIgnoreCase(editProduct.getReference()))) {
            JOptionPane.showMessageDialog(this,
                "La reference " + ref + " existe deja.",
                "Reference en double", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Product p = editProduct != null ? editProduct : new Product();
        p.setReference(ref);
        p.setName(name);
        p.setDescription(taDescription.getText().trim());
        p.setCategory(cbCategory.getSelectedItem() != null
            ? cbCategory.getSelectedItem().toString().trim() : "");
        p.setQuantity((int) spQuantity.getValue());
        p.setMinQuantity((int) spMinQuantity.getValue());
        p.setStorageLocation(tfStorageLocation.getText().trim());
        p.setStatus((String) cbStatus.getSelectedItem());

        if (editProduct == null) controller.addProduct(p);
        else controller.updateProduct(p);

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
}
