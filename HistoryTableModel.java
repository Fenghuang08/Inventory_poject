package com.inventaire;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modèle de tableau pour l'historique des mouvements de stock.
 */
public class HistoryTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {
        "ID", "Date / Heure", "Produit", "Référence", "Type", "Quantité", "Utilisateur", "Note"
    };

    private static final Class<?>[] COLUMN_CLASSES = {
        Integer.class, LocalDateTime.class, String.class, String.class,
        String.class, Integer.class, String.class, String.class
    };

    private List<Movement> movements;

    public HistoryTableModel() {
        this.movements = new ArrayList<>();
    }

    public HistoryTableModel(List<Movement> movements) {
        this.movements = new ArrayList<>(movements);
    }

    @Override public int getRowCount()    { return movements.size(); }
    @Override public int getColumnCount() { return COLUMN_NAMES.length; }
    @Override public String getColumnName(int col)   { return COLUMN_NAMES[col]; }
    @Override public Class<?> getColumnClass(int col){ return COLUMN_CLASSES[col]; }
    @Override public boolean isCellEditable(int r, int c) { return false; }

    @Override
    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= movements.size()) return null;
        Movement m = movements.get(row);
        return switch (col) {
            case 0 -> m.getId();
            case 1 -> m.getDateTime();
            case 2 -> m.getProductName();
            case 3 -> m.getProductReference();
            case 4 -> m.getType().getLabel();
            case 5 -> m.getQuantity();
            case 6 -> m.getUsername();
            case 7 -> m.getNote();
            default -> null;
        };
    }

    public void setMovements(List<Movement> movements) {
        this.movements = new ArrayList<>(movements);
        fireTableDataChanged();
    }

    public void addMovement(Movement movement) {
        movements.add(0, movement);
        fireTableRowsInserted(0, 0);
    }

    public Movement getMovementAt(int row) {
        return movements.get(row);
    }

    public void clear() {
        int size = movements.size();
        movements.clear();
        if (size > 0) fireTableRowsDeleted(0, size - 1);
    }

    public int getMovementCount() { return movements.size(); }

    public int getNetBalance() {
        return movements.stream().mapToInt(m ->
            m.getType() == Movement.Type.IN ? m.getQuantity() : -m.getQuantity()
        ).sum();
    }
}
