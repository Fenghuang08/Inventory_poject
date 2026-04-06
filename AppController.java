package com.inventaire;

import java.util.HashMap;
import java.util.Map;

public class AppController {

    private final ProductController  productController;
    private final MovementController movementController;
    private final UserController     userController;
    private final Map<String, String> settings = new HashMap<>();

    public AppController() {
        productController  = new ProductController();
        movementController = new MovementController(productController);
        userController     = new UserController();

        // Paramètres par défaut
        settings.put("company.name",    "Mon Entreprise");
        settings.put("company.address", "");
        settings.put("company.phone",   "");
        settings.put("company.email",   "");
        settings.put("db.host",         "localhost");
        settings.put("db.name",         "inventaire_db");
        settings.put("db.user",         "root");
        settings.put("alert.lowstock",  "5");
        settings.put("alert.email.enabled", "false");
        settings.put("alert.email.address", "");
        settings.put("ui.theme",        "Système");
        settings.put("ui.language",     "Français");
    }

    // ── Accesseurs des contrôleurs ───────────────────────────────────────────
    public ProductController  getProductController()  { return productController; }
    public MovementController getMovementController() { return movementController; }
    public UserController     getUserController()     { return userController; }

    // ── Paramètres ───────────────────────────────────────────────────────────
    public Map<String, String> getSettings() {
        return new HashMap<>(settings);
    }

    public void saveSettings(Map<String, String> newSettings) {
        settings.putAll(newSettings);
    }

    // ── Base de données ──────────────────────────────────────────────────────
    public boolean testDatabaseConnection(String host, int port, String dbName,
                                          String user, String password) {
        // Stub : retourne true si les champs sont remplis
        return host != null && !host.isBlank() && dbName != null && !dbName.isBlank();
    }

    // ── Sauvegarde ───────────────────────────────────────────────────────────
    public boolean backup(String filePath) {
        try {
            java.io.File f = new java.io.File(filePath);
            f.getParentFile().mkdirs();
            try (java.io.PrintWriter pw = new java.io.PrintWriter(f)) {
                pw.println("-- Sauvegarde Inventaire-v2 : " + java.time.LocalDateTime.now());
                pw.println("-- (données en mémoire — connectez une BDD pour une vraie sauvegarde)");
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void restore(String filePath) {
        // Stub : afficher un message ; à implémenter avec une vraie BDD
        javax.swing.JOptionPane.showMessageDialog(null,
            "Restauration depuis : " + filePath + "\n(à implémenter avec la BDD)",
            "Restauration", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
}
