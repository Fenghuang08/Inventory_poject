package com.inventaire;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductController {

    private final List<Product>  products   = new ArrayList<>();
    private final List<Movement> movements  = new ArrayList<>();
    private final List<String>   categories = new ArrayList<>(); // ordre personnalisé

    private int nextProductId  = 1;
    private int nextMovementId = 1;

    public ProductController() {
        // Catégories par défaut
        categories.add("Fournitures");
        categories.add("Informatique");
        categories.add("Papeterie");

        // Produits de démonstration
        addProduct(new Product(0, "REF-001", "Stylos Bic",       "Fournitures", 150, 20, 0.50,  Product.STATUS_ACTIVE));
        addProduct(new Product(0, "REF-002", "Ramette A4",       "Papeterie",    40, 10, 4.99,  Product.STATUS_ACTIVE));
        addProduct(new Product(0, "REF-003", "Clé USB 32 Go",    "Informatique",  8,  5, 12.90, Product.STATUS_ACTIVE));
        addProduct(new Product(0, "REF-004", "Agrafeuse",        "Fournitures",   3,  5, 8.50,  Product.STATUS_ACTIVE));
        addProduct(new Product(0, "REF-005", "Toner imprimante", "Informatique",  0,  2, 45.00, Product.STATUS_INACTIVE));
    }

    // ── Produits ─────────────────────────────────────────────────────────────
    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public Product getProductById(int id) {
        return products.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public void addProduct(Product p) {
        p.setId(nextProductId++);
        products.add(p);
        // Ajouter la catégorie si nouvelle
        if (p.getCategory() != null && !p.getCategory().isBlank()
                && !categories.contains(p.getCategory())) {
            categories.add(p.getCategory());
        }
    }

    public void updateProduct(Product updated) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == updated.getId()) {
                products.set(i, updated);
                // Ajouter la catégorie si nouvelle
                if (updated.getCategory() != null && !updated.getCategory().isBlank()
                        && !categories.contains(updated.getCategory())) {
                    categories.add(updated.getCategory());
                }
                return;
            }
        }
    }

    public void deleteProduct(int id) {
        products.removeIf(p -> p.getId() == id);
    }

    // ── Catégories ───────────────────────────────────────────────────────────
    public List<String> getAllCategories() {
        return new ArrayList<>(categories);
    }

    public void addCategory(String name) {
        if (!categories.contains(name)) {
            categories.add(name);
        }
    }

    public void renameCategory(String oldName, String newName) {
        int idx = categories.indexOf(oldName);
        if (idx >= 0) categories.set(idx, newName);
        // Mettre à jour tous les produits de cette catégorie
        products.stream()
            .filter(p -> oldName.equals(p.getCategory()))
            .forEach(p -> p.setCategory(newName));
    }

    public void deleteCategory(String name) {
        categories.remove(name);
        // Déplacer les produits vers "Non classé"
        products.stream()
            .filter(p -> name.equals(p.getCategory()))
            .forEach(p -> p.setCategory("Non classé"));
        // Ajouter "Non classé" si besoin
        if (products.stream().anyMatch(p -> "Non classé".equals(p.getCategory()))
                && !categories.contains("Non classé")) {
            categories.add(0, "Non classé");
        }
    }

    // ── Mouvements ───────────────────────────────────────────────────────────
    public void addMovement(Movement m) {
        m.setId(nextMovementId++);
        Product p = getProductById(m.getProductId());
        if (p != null) {
            m.setProductName(p.getName());
            m.setProductReference(p.getReference());
            int newQty = m.getType() == Movement.Type.IN
                ? p.getQuantity() + m.getQuantity()
                : p.getQuantity() - m.getQuantity();
            p.setQuantity(Math.max(0, newQty));
        }
        movements.add(0, m);
    }

    public List<Movement> getAllMovements() {
        return new ArrayList<>(movements);
    }
}
