package com.inventaire;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserController {

    private final List<User> users = new ArrayList<>();
    private int nextId = 1;

    private AppController appController;
    public void setAppController(AppController ac) { this.appController = ac; }
    private void save() { if (appController != null) appController.saveData(); }

    // ── Chargement par défaut ────────────────────────────────────────────────
    public void loadDefaults() {
        User admin = new User(0, "admin", "Administrateur", "admin@inventaire.fr",
                              User.Role.ADMIN, true);
        admin.setRawPassword("admin123");
        addUser(admin);

        User gest = new User(0, "jdupont", "Jean Dupont", "jdupont@inventaire.fr",
                             User.Role.GESTIONNAIRE, true);
        gest.setRawPassword("pass123");
        addUser(gest);
    }

    // ── Méthodes de chargement (sans sauvegarde) ─────────────────────────────
    public void clearAll() {
        users.clear();
        nextId = 1;
    }

    public void loadUser(User u) {
        users.add(u);
        if (u.getId() >= nextId) nextId = u.getId() + 1;
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────
    public List<User> getAllUsers() { return new ArrayList<>(users); }

    public User getUserById(int id) {
        return users.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    public void addUser(User u) {
        u.setId(nextId++);
        users.add(u);
        save();
    }

    public void updateUser(User updated) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == updated.getId()) {
                users.set(i, updated);
                save();
                return;
            }
        }
    }

    public void deleteUser(int id) {
        users.removeIf(u -> u.getId() == id);
        save();
    }

    public void toggleUserActive(int id) {
        User u = getUserById(id);
        if (u != null) { u.setActive(!u.isActive()); save(); }
    }

    public String resetPassword(int id) {
        String tmp = UUID.randomUUID().toString().substring(0, 8);
        User u = getUserById(id);
        if (u != null) { u.setRawPassword(tmp); save(); }
        return tmp;
    }
}
