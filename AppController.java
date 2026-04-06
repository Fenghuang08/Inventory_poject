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

        // Lier les contrôleurs à AppController pour la sauvegarde auto
        productController.setAppController(this);
        userController.setAppController(this);

        // Paramètres par défaut
        settings.put("company.name",         "Mon Entreprise");
        settings.put("company.address",      "");
        settings.put("company.phone",        "");
        settings.put("company.email",        "");
        settings.put("db.host",              "localhost");
        settings.put("db.name",              "inventaire_db");
        settings.put("db.user",              "root");
        settings.put("alert.lowstock",       "5");
        settings.put("alert.email.enabled",  "false");
        settings.put("alert.email.address",  "");
        settings.put("ui.theme",             "Systeme");
        settings.put("ui.language",          "Francais");

        // ── Chargement des données ───────────────────────────────────────────
        boolean loaded = DataManager.load(productController, userController);
        if (!loaded) {
            // Première utilisation : données de démo
            productController.loadDefaults();
            userController.loadDefaults();
            saveData(); // Créer le fichier JSON initial
            System.out.println("Nouveau fichier de données créé : "
                + DataManager.getDataFilePath());
        } else {
            System.out.println("Données chargées depuis : "
                + DataManager.getDataFilePath());
        }
    }

    // ── Sauvegarde ───────────────────────────────────────────────────────────
    public void saveData() {
        DataManager.save(productController, userController);
    }

    // ── Accesseurs ───────────────────────────────────────────────────────────
    public ProductController  getProductController()  { return productController; }
    public MovementController getMovementController() { return movementController; }
    public UserController     getUserController()     { return userController; }

    // ── Paramètres ───────────────────────────────────────────────────────────
    public Map<String, String> getSettings() { return new HashMap<>(settings); }

    public void saveSettings(Map<String, String> newSettings) {
        settings.putAll(newSettings);
    }

    // ── Base de données ──────────────────────────────────────────────────────
    public boolean testDatabaseConnection(String host, int port, String dbName,
                                          String user, String password) {
        return host != null && !host.isBlank() && dbName != null && !dbName.isBlank();
    }

    // ── Sauvegarde / Restauration ────────────────────────────────────────────
    public boolean backup(String filePath) {
        try {
            // Copie du fichier JSON de données
            java.io.File src = new java.io.File(DataManager.getDataFilePath());
            java.io.File dst = new java.io.File(filePath.endsWith(".json")
                ? filePath : filePath.replace(".sql", ".json"));
            java.nio.file.Files.copy(src.toPath(), dst.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur backup : " + e.getMessage());
            return false;
        }
    }

    public void restore(String filePath) {
        try {
            java.io.File src = new java.io.File(filePath);
            java.io.File dst = new java.io.File(DataManager.getDataFilePath());
            java.nio.file.Files.copy(src.toPath(), dst.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            // Recharger les données
            productController.clearAll();
            userController.clearAll();
            DataManager.load(productController, userController);
            javax.swing.JOptionPane.showMessageDialog(null,
                "Restauration reussie ! Redemarrez l'application.",
                "Restauration", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Erreur de restauration : " + e.getMessage(),
                "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
