package com.inventaire;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Renderer coloré (badges arrondis) pour les colonnes Statut / Type / Rôle.
 */
public class StatusCellRenderer extends DefaultTableCellRenderer {

    private record Badge(Color bg, Color fg) {}

    private static final Map<String, Badge> PALETTE = new HashMap<>();
    private static final int ARC = 8;

    static {
        PALETTE.put("actif",        new Badge(new Color(212, 237, 218), new Color(21,  87,  36)));
        PALETTE.put("inactif",      new Badge(new Color(248, 215, 218), new Color(114, 28,  36)));
        PALETTE.put("discontinué",  new Badge(new Color(255, 243, 205), new Color(133, 100,  4)));
        PALETTE.put("entrée",       new Badge(new Color(209, 236, 241), new Color(12,  84,  96)));
        PALETTE.put("sortie",       new Badge(new Color(248, 215, 218), new Color(114, 28,  36)));
        PALETTE.put("admin",        new Badge(new Color(220, 210, 240), new Color(68,   1, 120)));
        PALETTE.put("gestionnaire", new Badge(new Color(204, 229, 255), new Color(0,   64, 133)));
        PALETTE.put("lecteur",      new Badge(new Color(230, 230, 230), new Color(60,  60,  60)));
        PALETTE.put("oui",          new Badge(new Color(212, 237, 218), new Color(21,  87,  36)));
        PALETTE.put("non",          new Badge(new Color(248, 215, 218), new Color(114, 28,  36)));
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        String text = value != null ? value.toString() : "";
        if (value instanceof Boolean b) { text = b ? "Oui" : "Non"; }

        setText(text);
        setHorizontalAlignment(CENTER);
        setFont(getFont().deriveFont(Font.BOLD, 11.5f));
        setOpaque(false);

        Badge badge = PALETTE.get(text.toLowerCase().trim());

        if (isSelected) {
            setOpaque(true);
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
            putClientProperty("badge", null);
        } else if (badge != null) {
            setForeground(badge.fg());
            putClientProperty("badge", badge);
        } else {
            setOpaque(true);
            setBackground(table.getBackground());
            setForeground(table.getForeground());
            putClientProperty("badge", null);
        }
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Badge badge = (Badge) getClientProperty("badge");
        if (badge != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            int px = 8, py = 4, pw = getWidth() - 16, ph = getHeight() - 8;
            g2.setColor(badge.bg());
            g2.fillRoundRect(px, py, pw, ph, ARC, ARC);
            g2.setColor(badge.fg().brighter());
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRoundRect(px, py, pw, ph, ARC, ARC);
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
