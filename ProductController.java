package com.inventaire;

import java.util.ArrayList;
import java.util.List;

public class ProductController {

    private final List<Product>  products   = new ArrayList<>();
    private final List<Movement> movements  = new ArrayList<>();
    private final List<String>   categories = new ArrayList<>();

    private int nextProductId  = 1;
    private int nextMovementId = 1;

    private AppController appController;
    public void setAppController(AppController ac) { this.appController = ac; }
    private void save() { if (appController != null) appController.saveData(); }

    // ── Données de démo ──────────────────────────────────────────────────────
    public void loadDefaults() {
        categories.add("Fournitures");
        categories.add("Informatique");
        categories.add("Papeterie");

        addProduct(new Product(0, generateNextReference(), "Stylos Bic",      "Fournitures",  150, 20, "Etagere A1", Product.STATUS_ACTIVE));
        addProduct(new Product(0, generateNextReference(), "Ramette A4",       "Papeterie",     40, 10, "Etagere B2", Product.STATUS_ACTIVE));
        addProduct(new Product(0, generateNextReference(), "Cle USB 32 Go",    "Informatique",   8,  5, "Armoire 1",  Product.STATUS_ACTIVE));
        addProduct(new Product(0, generateNextReference(), "Agrafeuse",        "Fournitures",    3,  5, "Etagere A3", Product.STATUS_ACTIVE));
        addProduct(new Product(0, generateNextReference(), "Toner imprimante", "Informatique",   0,  2, "Reserve",    Product.STATUS_INACTIVE));
    }

    // ── Chargement JSON (sans save) ──────────────────────────────────────────
    public void clearAll() {
        products.clear(); movements.clear(); categories.clear();
        nextProductId = 1; nextMovementId = 1;
    }

    public void loadProduct(Product p) {
        products.add(p);
        if (p.getId() >= nextProductId) nextProductId = p.getId() + 1;
    }

    public void loadMovement(Movement m) {
        movements.add(m);
        if (m.getId() >= nextMovementId) nextMovementId = m.getId() + 1;
    }

    // ── Référence auto unique ────────────────────────────────────────────────
    public String generateNextReference() {
        int max = 0;
        for (Product p : products) {
            String ref = p.getReference();
            if (ref != null && ref.startsWith("REF-")) {
                try { int n = Integer.parseInt(ref.substring(4)); if (n > max) max = n; }
                catch (NumberFormatException ignored) {}
            }
        }
        int next = max + 1;
        while (referenceExists("REF-" + String.format("%03d", next))) next++;
        return "REF-" + String.format("%03d", next);
    }

    public boolean referenceExists(String ref) {
        return products.stream().anyMatch(p -> ref.equalsIgnoreCase(p.getReference()));
    }

    // ── CRUD Produits ────────────────────────────────────────────────────────
    public List<Product> getAllProducts() { return new ArrayList<>(products); }

    public Product getProductById(int id) {
        return products.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public void addProduct(Product p) {
        p.setId(nextProductId++);
        products.add(p);
        if (p.getCategory() != null && !p.getCategory().isBlank()
                && !categories.contains(p.getCategory()))
            categories.add(p.getCategory());
        save();
    }

    public void updateProduct(Product updated) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == updated.getId()) {
                products.set(i, updated);
                if (updated.getCategory() != null && !updated.getCategory().isBlank()
                        && !categories.contains(updated.getCategory()))
                    categories.add(updated.getCategory());
                save(); return;
            }
        }
    }

    public void deleteProduct(int id) {
        products.removeIf(p -> p.getId() == id);
        save();
    }

    // ── Catégories ───────────────────────────────────────────────────────────
    public List<String> getAllCategories() { return new ArrayList<>(categories); }

    public void addCategory(String name) {
        if (!categories.contains(name)) { categories.add(name); save(); }
    }

    public void renameCategory(String oldName, String newName) {
        int idx = categories.indexOf(oldName);
        if (idx >= 0) categories.set(idx, newName);
        products.stream().filter(p -> oldName.equals(p.getCategory()))
                         .forEach(p -> p.setCategory(newName));
        save();
    }

    public void deleteCategory(String name) {
        categories.remove(name);
        products.stream().filter(p -> name.equals(p.getCategory()))
                         .forEach(p -> p.setCategory("Non classe"));
        if (products.stream().anyMatch(p -> "Non classe".equals(p.getCategory()))
                && !categories.contains("Non classe"))
            categories.add(0, "Non classe");
        save();
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
        save();
    }

    public List<Movement> getAllMovements() { return new ArrayList<>(movements); }
}
