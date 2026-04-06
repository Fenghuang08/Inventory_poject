package com.inventaire;

import javax.swing.*;

/**
 * Point d'entrée de l'application Inventaire-v2
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Impossible de charger le L&F système : " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            AppController controller = new AppController();
            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }
}
