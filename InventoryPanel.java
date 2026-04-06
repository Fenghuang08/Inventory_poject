package com.inventaire;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

/**
 * Panneau inventaire avec onglets dynamiques par catégorie.
 * - Onglet "Tout" : tous les produits
 * - Un onglet par catégorie
 * - Bouton "+" pour créer une nouvelle catégorie
 * - Clic droit sur un onglet pour le renommer ou supprimer
 */
public class InventoryPanel extends JPanel {

    private static final String TAB_ALL = "Tout";

    private static final String[] COLUMNS = {
        "ID", "Référence", "Nom", "Catégorie", "Quantité", "Qté min.", "Prix (€)", "Statut"
    };

    // ── Composants principaux ────────────────────────────────────────────────
    private final JTabbedPane categoryTabs;
    private final JLabel statusLabel;
    private final ProductController productController;

    // ────────────────────────────────────────────────────────────────────────
    public InventoryPanel(ProductController productController) {
        this.productController = productController;
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // ── Barre d'outils globale ───────────────────────────────────────────
        add(buildGlobalToolbar(), BorderLayout.NORTH);

        // ── Onglets ─────────────────────────────────────────────────────────
        categoryTabs = new JTabbedPane();
        categoryTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        add(categoryTabs, BorderLayout.CENTER);

        // ── Barre de statut ──────────────────────────────────────────────────
        statusLabel = new JLabel(" ");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC, 11f));
        add(statusLabel, BorderLayout.SOUTH);

        // Initialiser les onglets
        rebuildTabs();
    }

    // ── Construction des onglets ─────────────────────────────────────────────
    private void rebuildTabs() {
        String selectedTitle = getCurrentTabTitle();
        categoryTabs.removeAll();

        // Onglet "Tout"
        categoryTabs.addTab(TAB_ALL, buildTabPanel(null));

        // Un onglet par catégorie
        for (String cat : productController.getAllCategories()) {
            addCategoryTab(cat);
        }

        // Bouton "+" (onglet factice)
        categoryTabs.addTab("+", new JPanel());
        int plusIndex = categoryTabs.getTabCount() - 1;
        categoryTabs.setTabComponentAt(plusIndex, buildPlusButton());

        // Restaurer la sélection
        restoreTab(selectedTitle);

        // Listener pour intercepter le clic sur "+"
        categoryTabs.addChangeListener(e -> {
            int idx = categoryTabs.getSelectedIndex();
            if (idx == categoryTabs.getTabCount() - 1) {
                // Clic sur "+" → revenir à l'onglet précédent et ouvrir dialog
                categoryTabs.setSelectedIndex(Math.max(0, idx - 1));
                createNewCategory();
            } else {
                updateStatusBar();
            }
        });

        // Clic droit sur les onglets
        categoryTabs.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int idx = categoryTabs.indexAtLocation(e.getX(), e.getY());
                    if (idx > 0 && idx < categoryTabs.getTabCount() - 1) {
                        showTabContextMenu(e, idx);
                    }
                }
            }
        });

        updateStatusBar();
    }

    private void addCategoryTab(String category) {
        int insertAt = categoryTabs.getTabCount(); // avant le "+"
        categoryTabs.insertTab(category, null, buildTabPanel(category), category, insertAt);
    }

    // ── Panneau d'un onglet ──────────────────────────────────────────────────
    private JPanel buildTabPanel(String categoryFilter) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        // Toolbar de l'onglet
        panel.add(buildTabToolbar(categoryFilter), BorderLayout.NORTH);

        // Tableau
        DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) { case 0, 4, 5 -> Integer.class; case 6 -> Double.class; default -> String.class; };
            }
        };

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusCellRenderer());

        int[] widths = {40, 100, 180, 120, 70, 70, 80, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Champ de recherche dans l'onglet
        JTextField search = new JTextField(16);
        search.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                String t = search.getText().trim();
                sorter.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + t, 1, 2, 3));
            }
        });

        // Stocker les références pour le refresh
        panel.putClientProperty("table",  table);
        panel.putClientProperty("model",  model);
        panel.putClientProperty("search", search);
        panel.putClientProperty("category", categoryFilter);

        fillTable(model, categoryFilter);
        return panel;
    }

    // ── Toolbar d'un onglet ──────────────────────────────────────────────────
    private JPanel buildTabToolbar(String categoryFilter) {
        JPanel bar  = new JPanel(new BorderLayout(8, 0));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JPanel right= new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

        JButton btnAdd     = btn("Nouveau",    new Color(46, 139, 87));
        JButton btnEdit    = btn("Modifier",   new Color(70, 130, 180));
        JButton btnDelete  = btn("Supprimer",  new Color(178, 34, 34));
        JButton btnMove    = btn("Mouvement",  new Color(160, 100, 30));
        JButton btnRefresh = btn("Actualiser", new Color(90, 90, 90));

        left.add(btnAdd); left.add(btnEdit); left.add(btnDelete);
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(btnMove); left.add(btnRefresh);

        JTextField search = new JTextField(16);
        search.putClientProperty("JTextField.placeholderText", "Rechercher…");
        right.add(new JLabel("Rechercher :")); right.add(search);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        // Les actions utiliseront le panneau parent via SwingUtilities
        btnAdd.addActionListener(e -> openProductDialog(null, categoryFilter, search));
        btnEdit.addActionListener(e -> {
            Product p = getSelectedProductFromCurrentTab();
            if (p != null) openProductDialog(p, categoryFilter, search);
        });
        btnDelete.addActionListener(e -> deleteSelectedProduct(search));
        btnMove.addActionListener(e -> {
            Product p = getSelectedProductFromCurrentTab();
            if (p != null) openMovementDialog(p, search);
        });
        btnRefresh.addActionListener(e -> refreshCurrentTab());

        // Lier le champ de recherche au sorter du tableau
        search.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                JPanel tabPanel = getCurrentTabPanel();
                if (tabPanel == null) return;
                JTable table = (JTable) tabPanel.getClientProperty("table");
                if (table == null) return;
                String t = search.getText().trim();
                ((TableRowSorter<?>) table.getRowSorter())
                    .setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + t, 1, 2, 3));
            }
        });

        return bar;
    }

    // ── Toolbar global (au-dessus des onglets) ───────────────────────────────
    private JPanel buildGlobalToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JButton btnNewCat = btn("+ Nouvelle catégorie", new Color(70, 100, 160));
        btnNewCat.addActionListener(e -> createNewCategory());
        bar.add(btnNewCat);

        JLabel hint = new JLabel("  Clic droit sur un onglet pour le renommer ou supprimer");
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
        hint.setForeground(Color.GRAY);
        bar.add(hint);

        return bar;
    }

    // ── Bouton "+" dans les onglets ──────────────────────────────────────────
    private JLabel buildPlusButton() {
        JLabel plus = new JLabel("  ＋  ");
        plus.setFont(plus.getFont().deriveFont(Font.BOLD, 14f));
        plus.setForeground(new Color(70, 100, 160));
        plus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        plus.setToolTipText("Créer une nouvelle catégorie");
        plus.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { createNewCategory(); }
        });
        return plus;
    }

    // ── Création d'une nouvelle catégorie ────────────────────────────────────
    private void createNewCategory() {
        String name = JOptionPane.showInputDialog(this,
            "Nom de la nouvelle catégorie :", "Nouvelle catégorie",
            JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;
        name = name.trim();

        // Vérifier doublon
        for (String cat : productController.getAllCategories()) {
            if (cat.equalsIgnoreCase(name)) {
                JOptionPane.showMessageDialog(this,
                    "La catégorie « " + name + " » existe déjà.",
                    "Doublon", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        productController.addCategory(name);
        String finalName = name;
        SwingUtilities.invokeLater(() -> {
            rebuildTabs();
            // Sélectionner le nouvel onglet
            for (int i = 0; i < categoryTabs.getTabCount(); i++) {
                if (finalName.equals(categoryTabs.getTitleAt(i))) {
                    categoryTabs.setSelectedIndex(i);
                    break;
                }
            }
        });
    }

    // ── Menu contextuel onglet ───────────────────────────────────────────────
    private void showTabContextMenu(MouseEvent e, int tabIndex) {
        String tabTitle = categoryTabs.getTitleAt(tabIndex);
        JPopupMenu menu = new JPopupMenu();

        JMenuItem miRename = new JMenuItem("✏️ Renommer « " + tabTitle + " »");
        miRename.addActionListener(ev -> {
            String newName = JOptionPane.showInputDialog(this,
                "Nouveau nom :", tabTitle);
            if (newName != null && !newName.trim().isEmpty()) {
                productController.renameCategory(tabTitle, newName.trim());
                rebuildTabs();
            }
        });

        JMenuItem miDelete = new JMenuItem("🗑 Supprimer « " + tabTitle + " »");
        miDelete.setForeground(new Color(178, 34, 34));
        miDelete.addActionListener(ev -> {
            long count = productController.getAllProducts().stream()
                .filter(p -> tabTitle.equals(p.getCategory())).count();
            String msg = count > 0
                ? "Supprimer la catégorie « " + tabTitle + " » ?\n"
                  + count + " produit(s) seront déplacés dans « Non classé »."
                : "Supprimer la catégorie « " + tabTitle + " » ?";
            if (JOptionPane.showConfirmDialog(this, msg, "Confirmation",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                    == JOptionPane.YES_OPTION) {
                productController.deleteCategory(tabTitle);
                rebuildTabs();
            }
        });

        menu.add(miRename);
        menu.addSeparator();
        menu.add(miDelete);
        menu.show(categoryTabs, e.getX(), e.getY());
    }

    // ── Remplissage du tableau ───────────────────────────────────────────────
    private void fillTable(DefaultTableModel model, String categoryFilter) {
        model.setRowCount(0);
        List<Product> products = productController.getAllProducts();
        for (Product p : products) {
            if (categoryFilter == null || categoryFilter.equals(p.getCategory())) {
                model.addRow(new Object[]{
                    p.getId(), p.getReference(), p.getName(), p.getCategory(),
                    p.getQuantity(), p.getMinQuantity(), p.getPrice(), p.getStatus()
                });
            }
        }
    }

    // ── Refresh ─────────────────────────────────────────────────────────────
    public void refreshCurrentTab() {
        JPanel panel = getCurrentTabPanel();
        if (panel == null) return;
        DefaultTableModel model = (DefaultTableModel) panel.getClientProperty("model");
        String cat = (String) panel.getClientProperty("category");
        if (model != null) fillTable(model, cat);
        updateStatusBar();
    }

    public void refreshAllTabs() {
        for (int i = 0; i < categoryTabs.getTabCount() - 1; i++) {
            Component c = categoryTabs.getComponentAt(i);
            if (c instanceof JPanel panel) {
                DefaultTableModel model = (DefaultTableModel) panel.getClientProperty("model");
                String cat = (String) panel.getClientProperty("category");
                if (model != null) fillTable(model, cat);
            }
        }
        updateStatusBar();
    }

    private void updateStatusBar() {
        List<Product> all = productController.getAllProducts();
        long low = all.stream().filter(p -> p.getQuantity() <= p.getMinQuantity()).count();
        statusLabel.setText(String.format("  %d produit(s) au total — %d en stock faible", all.size(), low));
        statusLabel.setForeground(low > 0 ? new Color(180, 60, 0) : Color.DARK_GRAY);
    }

    // ── Dialogues ────────────────────────────────────────────────────────────
    private void openProductDialog(Product product, String categoryFilter, JTextField search) {
        Window w = SwingUtilities.getWindowAncestor(this);
        ProductDialog dlg = new ProductDialog((JFrame) w, productController, product);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            rebuildTabs();
        }
    }

    private void openMovementDialog(Product product, JTextField search) {
        Window w = SwingUtilities.getWindowAncestor(this);
        MovementDialog dlg = new MovementDialog((JFrame) w, productController, product);
        dlg.setVisible(true);
        if (dlg.isSaved()) refreshCurrentTab();
    }

    private void deleteSelectedProduct(JTextField search) {
        Product p = getSelectedProductFromCurrentTab();
        if (p == null) return;
        int c = JOptionPane.showConfirmDialog(this,
            "Supprimer « " + p.getName() + " » ?",
            "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            productController.deleteProduct(p.getId());
            refreshCurrentTab();
        }
    }

    // ── Utilitaires ─────────────────────────────────────────────────────────
    private JPanel getCurrentTabPanel() {
        int idx = categoryTabs.getSelectedIndex();
        if (idx < 0 || idx >= categoryTabs.getTabCount() - 1) return null;
        Component c = categoryTabs.getComponentAt(idx);
        return c instanceof JPanel ? (JPanel) c : null;
    }

    private String getCurrentTabTitle() {
        int idx = categoryTabs.getSelectedIndex();
        if (idx < 0 || idx >= categoryTabs.getTabCount()) return TAB_ALL;
        return categoryTabs.getTitleAt(idx);
    }

    private void restoreTab(String title) {
        for (int i = 0; i < categoryTabs.getTabCount(); i++) {
            if (categoryTabs.getTitleAt(i).equals(title)) {
                categoryTabs.setSelectedIndex(i);
                return;
            }
        }
        categoryTabs.setSelectedIndex(0);
    }

    private Product getSelectedProductFromCurrentTab() {
        JPanel panel = getCurrentTabPanel();
        if (panel == null) return null;
        JTable table = (JTable) panel.getClientProperty("table");
        DefaultTableModel model = (DefaultTableModel) panel.getClientProperty("model");
        if (table == null || model == null) return null;

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un produit.",
                "Aucune sélection", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int id = (int) model.getValueAt(table.convertRowIndexToModel(row), 0);
        return productController.getProductById(id);
    }

    private JButton btn(String label, Color bg) {
        JButton b = new JButton(label);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(bg.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }
}
