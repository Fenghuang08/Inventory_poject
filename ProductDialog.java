package com.inventaire;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dialogue de création / modification d'un produit.
 */
public class ProductDialog extends JDialog {

    private final JTextField        tfReference;
    private final JTextField        tfName;
    private final JTextArea         taDescription;
    private final JComboBox<String> cbCategory;
    private final JSpinner          spQuantity;
    private final JSpinner          spMinQuantity;
    private final JSpinner          spPrice;
    private final JComboBox<String> cbStatus;

    private boolean saved = false;
    private final Product editProduct;
    private final ProductController controller;

    public ProductDialog(JFrame parent, ProductController controller, Product product) {
        super(parent, product == null ? "Nouveau produit" : "Modifier un produit", true);
        this.controller  = controller;
        this.editProduct = product;

        tfReference   = new JTextField(20);
        tfName        = new JTextField(20);
        taDescription = new JTextArea(3, 20);
        taDescription.setLineWrap(true); taDescription.setWrapStyleWord(true);

        List<String> categories = controller.getAllCategories();
        cbCategory = new JComboBox<>(categories.toArray(new String[0]));
        cbCategory.setEditable(true);

        spQuantity    = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
        spMinQuantity = new JSpinner(new SpinnerNumberModel(5, 0, 999999, 1));
        spPrice       = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 9999999.0, 0.01));
        ((JSpinner.NumberEditor) spPrice.getEditor()).getFormat().applyPattern("#,##0.00");

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
        setMinimumSize(new Dimension(420, 380));
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        addRow(p, c, row++, "Référence * :",      tfReference);
        addRow(p, c, row++, "Nom * :",             tfName);
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        p.add(lbl("Description :"), c);
        c.gridx = 1; c.weightx = 1;
        p.add(new JScrollPane(taDescription,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), c);
        row++;
        addRow(p, c, row++, "Catégorie :",         cbCategory);
        addRow(p, c, row++, "Quantité initiale :",  spQuantity);
        addRow(p, c, row++, "Quantité minimale :",  spMinQuantity);
        addRow(p, c, row++, "Prix (€) :",           spPrice);
        addRow(p, c, row,   "Statut :",             cbStatus);
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
        JButton btnSave   = new JButton(editProduct == null ? "➕ Créer" : "✅ Enregistrer");
        JButton btnCancel = new JButton("Annuler");
        btnSave.setBackground(new Color(46, 139, 87));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setContentAreaFilled(true);
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
        spPrice.setValue(p.getPrice());
        cbStatus.setSelectedItem(p.getStatus());
    }

    private void save() {
        String ref  = tfReference.getText().trim();
        String name = tfName.getText().trim();
        if (ref.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "La référence et le nom sont obligatoires.",
                "Champs manquants", JOptionPane.WARNING_MESSAGE);
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
        p.setPrice(((Number) spPrice.getValue()).doubleValue());
        p.setStatus((String) cbStatus.getSelectedItem());

        if (editProduct == null) controller.addProduct(p);
        else controller.updateProduct(p);

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
}
