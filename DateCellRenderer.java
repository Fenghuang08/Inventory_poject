package com.inventaire;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Renderer pour les colonnes de date / horodatage.
 * Accepte LocalDateTime, LocalDate et Date (legacy).
 * Met en évidence en jaune doux les entrées du jour.
 */
public class DateCellRenderer extends DefaultTableCellRenderer {

    private static final DateTimeFormatter FMT_DATETIME =
        DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");
    private static final DateTimeFormatter FMT_DATE =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Color COLOR_TODAY = new Color(255, 243, 205);

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int col) {

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        setHorizontalAlignment(CENTER);

        if (value == null) { setText("—"); return this; }

        boolean isToday = false;
        String display  = "";

        if (value instanceof LocalDateTime ldt) {
            display = ldt.format(FMT_DATETIME);
            isToday = ldt.toLocalDate().equals(LocalDate.now());
        } else if (value instanceof LocalDate ld) {
            display = ld.format(FMT_DATE);
            isToday = ld.equals(LocalDate.now());
        } else if (value instanceof Date d) {
            LocalDateTime ldt = d.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            display = ldt.format(FMT_DATETIME);
            isToday = ldt.toLocalDate().equals(LocalDate.now());
        } else {
            display = value.toString();
        }

        setText(display);
        setToolTipText(display);

        if (!isSelected) {
            setBackground(isToday ? COLOR_TODAY : Color.WHITE);
            setForeground(Color.DARK_GRAY);
        }
        return this;
    }
}
