package com.inventaire;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class UsersPanel extends JPanel {

    private static final String[] COLUMNS = {
        "ID", "Nom d'utilisateur", "Nom complet", "E-mail", "Rôle", "Actif", "Dernière connexion"
    };

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField searchField;
    private final UserController userController;

    public UsersPanel(UserController userController) {
        this.userController = userController;
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel toolbar = buildToolbar();
        add(toolbar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : c == 5 ? Boolean.class : String.class;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                Object active = tableModel.getValueAt(table.convertRowIndexToModel(row), 5);
                if (Boolean.FALSE.equals(active) && !isRowSelected(row)) {
                    c.setBackground(new Color(245, 245, 245));
                    c.setForeground(Color.GRAY);
                } else if (!isRowSelected(row)) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        table.setRowHeight(26);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        int[] widths = {40, 130, 160, 200, 90, 55, 160};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        add(new JScrollPane(table), BorderLayout.CENTER);
        searchField = (JTextField) findNamed(toolbar, "searchField");
        refreshTable();
    }

    private JPanel buildToolbar() {
        JPanel bar  = new JPanel(new BorderLayout(8, 0));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JPanel right= new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

        JButton btnAdd     = btn("Nouvel utilisateur",  new Color(46, 139, 87));
        JButton btnEdit    = btn("Modifier",            new Color(70, 130, 180));
        JButton btnToggle  = btn("Activer/Désactiver",  new Color(160, 100, 30));
        JButton btnReset   = btn("Réinitialiser MDP",   new Color(100, 60, 160));
        JButton btnDelete  = btn("Supprimer",           new Color(178, 34, 34));
        JButton btnRefresh = btn("Actualiser",          new Color(90, 90, 90));

        left.add(btnAdd); left.add(btnEdit); left.add(btnToggle);
        left.add(btnReset); left.add(btnDelete); left.add(btnRefresh);

        JTextField search = new JTextField(16);
        search.setName("searchField");
        right.add(new JLabel("Rechercher :")); right.add(search);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        btnAdd.addActionListener(e    -> openUserDialog(null));
        btnEdit.addActionListener(e   -> { User u = getSelectedUser(); if (u != null) openUserDialog(u); });
        btnToggle.addActionListener(e -> toggleSelectedUser());
        btnReset.addActionListener(e  -> resetPassword());
        btnDelete.addActionListener(e -> deleteSelectedUser());
        btnRefresh.addActionListener(e -> refreshTable());
        search.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { applyFilter(); }
        });
        return bar;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (User u : userController.getAllUsers()) {
            tableModel.addRow(new Object[]{
                u.getId(), u.getUsername(), u.getFullName(), u.getEmail(),
                u.getRole().getLabel(), u.isActive(), u.getLastLoginFormatted()
            });
        }
    }

    private void applyFilter() {
        String text = searchField != null ? searchField.getText().trim() : "";
        ((javax.swing.table.TableRowSorter<?>) table.getRowSorter()).setRowFilter(
            text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text, 1, 2, 3));
    }

    private void openUserDialog(User user) {
        Window w = SwingUtilities.getWindowAncestor(this);
        UserDialog dlg = new UserDialog((JFrame) w, userController, user);
        dlg.setVisible(true);
        if (dlg.isSaved()) refreshTable();
    }

    private void toggleSelectedUser() {
        User u = getSelectedUser();
        if (u == null) return;
        String action = u.isActive() ? "désactiver" : "activer";
        if (JOptionPane.showConfirmDialog(this, "Voulez-vous " + action + " « " + u.getUsername() + " » ?",
                "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            userController.toggleUserActive(u.getId());
            refreshTable();
        }
    }

    private void resetPassword() {
        User u = getSelectedUser();
        if (u == null) return;
        if (JOptionPane.showConfirmDialog(this,
                "Réinitialiser le mot de passe de « " + u.getUsername() + " » ?",
                "Réinitialisation", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            String tmp = userController.resetPassword(u.getId());
            JOptionPane.showMessageDialog(this, "Mot de passe temporaire : " + tmp,
                "Réinitialisation réussie", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedUser() {
        User u = getSelectedUser();
        if (u == null) return;
        if (JOptionPane.showConfirmDialog(this, "Supprimer définitivement « " + u.getUsername() + " » ?",
                "Suppression", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            userController.deleteUser(u.getId());
            refreshTable();
        }
    }

    private User getSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un utilisateur.",
                "Aucune sélection", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        return userController.getUserById(id);
    }

    @SuppressWarnings("unchecked")
    private <T extends Component> T findNamed(Container c, String name) {
        for (Component child : c.getComponents()) {
            if (name.equals(child.getName())) return (T) child;
            if (child instanceof Container sub) { T f = findNamed(sub, name); if (f != null) return f; }
        }
        return null;
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(bg.brighter()); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }
}
