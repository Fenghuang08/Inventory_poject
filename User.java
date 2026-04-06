package com.inventaire;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User {

    public enum Role {
        ADMIN("Admin"),
        GESTIONNAIRE("Gestionnaire"),
        LECTEUR("Lecteur");

        private final String label;
        Role(String label) { this.label = label; }
        public String getLabel() { return label; }

        public static Role fromLabel(String label) {
            for (Role r : values()) if (r.label.equalsIgnoreCase(label)) return r;
            return LECTEUR;
        }

        public static String[] labelArray() {
            Role[] roles = values();
            String[] labels = new String[roles.length];
            for (int i = 0; i < roles.length; i++) labels[i] = roles[i].label;
            return labels;
        }
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int           id;
    private String        username  = "";
    private String        fullName  = "";
    private String        email     = "";
    private String        passwordHash = "";
    private Role          role      = Role.LECTEUR;
    private boolean       active    = true;
    private LocalDateTime lastLogin = null;

    public User() {}

    public User(int id, String username, String fullName, String email, Role role, boolean active) {
        this.id       = id;
        this.username = username;
        this.fullName = fullName;
        this.email    = email;
        this.role     = role;
        this.active   = active;
    }

    // ── Getters ─────────────────────────────────────────────────────────────
    public int    getId()        { return id; }
    public String getUsername()  { return username; }
    public String getFullName()  { return fullName; }
    public String getEmail()     { return email; }
    public Role   getRole()      { return role; }
    public boolean isActive()    { return active; }
    public LocalDateTime getLastLogin() { return lastLogin; }

    public String getLastLoginFormatted() {
        return lastLogin != null ? lastLogin.format(FMT) : "—";
    }

    // ── Setters ─────────────────────────────────────────────────────────────
    public void setId(int id)               { this.id = id; }
    public void setUsername(String u)       { this.username = u; }
    public void setFullName(String f)       { this.fullName = f; }
    public void setEmail(String e)          { this.email = e; }
    public void setRole(Role role)          { this.role = role; }
    public void setActive(boolean active)   { this.active = active; }
    public void setLastLogin(LocalDateTime d) { this.lastLogin = d; }
    public void setRawPassword(String pwd)  { this.passwordHash = pwd; } // simplifié
    public String getPasswordHash()         { return passwordHash; }
}
