package com.inventaire;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final String FILE_NAME = "inventaire_data.json";

    private static File getDataFile() {
        try {
            File jar = new File(DataManager.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
            File dir = jar.isFile() ? jar.getParentFile() : new File(".");
            return new File(dir, FILE_NAME);
        } catch (Exception e) {
            return new File(FILE_NAME);
        }
    }

    // ── Sauvegarde ───────────────────────────────────────────────────────────
    public static void save(ProductController pc, UserController uc) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");

        // Catégories
        json.append("  \"categories\": [\n");
        List<String> cats = pc.getAllCategories();
        for (int i = 0; i < cats.size(); i++) {
            json.append("    ").append(jStr(cats.get(i)));
            if (i < cats.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ],\n");

        // Produits
        json.append("  \"products\": [\n");
        List<Product> products = pc.getAllProducts();
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            json.append("    {\n");
            json.append("      \"id\": ").append(p.getId()).append(",\n");
            json.append("      \"reference\": ").append(jStr(p.getReference())).append(",\n");
            json.append("      \"name\": ").append(jStr(p.getName())).append(",\n");
            json.append("      \"description\": ").append(jStr(p.getDescription())).append(",\n");
            json.append("      \"category\": ").append(jStr(p.getCategory())).append(",\n");
            json.append("      \"quantity\": ").append(p.getQuantity()).append(",\n");
            json.append("      \"minQuantity\": ").append(p.getMinQuantity()).append(",\n");
            json.append("      \"storageLocation\": ").append(jStr(p.getStorageLocation())).append(",\n");
            json.append("      \"status\": ").append(jStr(p.getStatus())).append("\n");
            json.append("    }");
            if (i < products.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ],\n");

        // Mouvements
        json.append("  \"movements\": [\n");
        List<Movement> movements = pc.getAllMovements();
        for (int i = 0; i < movements.size(); i++) {
            Movement m = movements.get(i);
            json.append("    {\n");
            json.append("      \"id\": ").append(m.getId()).append(",\n");
            json.append("      \"productId\": ").append(m.getProductId()).append(",\n");
            json.append("      \"productName\": ").append(jStr(m.getProductName())).append(",\n");
            json.append("      \"productReference\": ").append(jStr(m.getProductReference())).append(",\n");
            json.append("      \"type\": ").append(jStr(m.getType().name())).append(",\n");
            json.append("      \"quantity\": ").append(m.getQuantity()).append(",\n");
            json.append("      \"dateTime\": ").append(jStr(
                m.getDateTime() != null ? m.getDateTime().format(DT_FMT) : "")).append(",\n");
            json.append("      \"username\": ").append(jStr(m.getUsername())).append(",\n");
            json.append("      \"note\": ").append(jStr(m.getNote())).append("\n");
            json.append("    }");
            if (i < movements.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ],\n");

        // Utilisateurs
        json.append("  \"users\": [\n");
        List<User> users = uc.getAllUsers();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            json.append("    {\n");
            json.append("      \"id\": ").append(u.getId()).append(",\n");
            json.append("      \"username\": ").append(jStr(u.getUsername())).append(",\n");
            json.append("      \"fullName\": ").append(jStr(u.getFullName())).append(",\n");
            json.append("      \"email\": ").append(jStr(u.getEmail())).append(",\n");
            json.append("      \"passwordHash\": ").append(jStr(u.getPasswordHash())).append(",\n");
            json.append("      \"role\": ").append(jStr(u.getRole().name())).append(",\n");
            json.append("      \"active\": ").append(u.isActive()).append(",\n");
            json.append("      \"lastLogin\": ").append(jStr(
                u.getLastLogin() != null ? u.getLastLogin().format(DT_FMT) : "")).append("\n");
            json.append("    }");
            if (i < users.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ]\n}\n");

        File dataFile = getDataFile();
        File backupFile = new File(dataFile.getParent(), FILE_NAME + ".bak");
        try {
            if (dataFile.exists())
                Files.copy(dataFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            try (PrintWriter pw = new PrintWriter(new FileWriter(dataFile, StandardCharsets.UTF_8))) {
                pw.print(json);
            }
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde JSON : " + e.getMessage());
        }
    }

    // ── Chargement ───────────────────────────────────────────────────────────
    public static boolean load(ProductController pc, UserController uc) {
        File dataFile = getDataFile();
        if (!dataFile.exists()) return false;
        try {
            String content = new String(Files.readAllBytes(dataFile.toPath()), StandardCharsets.UTF_8);

            List<String> cats = parseStringArray(content, "categories");
            pc.clearAll();
            for (String cat : cats) pc.addCategory(cat);

            for (String block : parseObjectArray(content, "products")) {
                Product p = new Product();
                p.setId(parseInt(block, "id"));
                p.setReference(parseStr(block, "reference"));
                p.setName(parseStr(block, "name"));
                p.setDescription(parseStr(block, "description"));
                p.setCategory(parseStr(block, "category"));
                p.setQuantity(parseInt(block, "quantity"));
                p.setMinQuantity(parseInt(block, "minQuantity"));
                p.setStorageLocation(parseStr(block, "storageLocation"));
                p.setStatus(parseStr(block, "status"));
                pc.loadProduct(p);
            }

            for (String block : parseObjectArray(content, "movements")) {
                Movement m = new Movement();
                m.setId(parseInt(block, "id"));
                m.setProductId(parseInt(block, "productId"));
                m.setProductName(parseStr(block, "productName"));
                m.setProductReference(parseStr(block, "productReference"));
                m.setType("OUT".equals(parseStr(block, "type")) ? Movement.Type.OUT : Movement.Type.IN);
                m.setQuantity(parseInt(block, "quantity"));
                String dt = parseStr(block, "dateTime");
                m.setDateTime(dt.isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(dt, DT_FMT));
                m.setUsername(parseStr(block, "username"));
                m.setNote(parseStr(block, "note"));
                pc.loadMovement(m);
            }

            uc.clearAll();
            for (String block : parseObjectArray(content, "users")) {
                User u = new User();
                u.setId(parseInt(block, "id"));
                u.setUsername(parseStr(block, "username"));
                u.setFullName(parseStr(block, "fullName"));
                u.setEmail(parseStr(block, "email"));
                u.setRawPassword(parseStr(block, "passwordHash"));
                try { u.setRole(User.Role.valueOf(parseStr(block, "role"))); }
                catch (Exception ex) { u.setRole(User.Role.LECTEUR); }
                u.setActive(parseBool(block, "active"));
                String ll = parseStr(block, "lastLogin");
                u.setLastLogin(ll.isEmpty() ? null : LocalDateTime.parse(ll, DT_FMT));
                uc.loadUser(u);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Erreur chargement JSON : " + e.getMessage());
            return false;
        }
    }

    // ── Parseurs ─────────────────────────────────────────────────────────────
    private static List<String> parseStringArray(String json, String key) {
        List<String> result = new ArrayList<>();
        int start = json.indexOf("\"" + key + "\"");
        if (start < 0) return result;
        int arrStart = json.indexOf("[", start);
        int arrEnd   = json.indexOf("]", arrStart);
        if (arrStart < 0 || arrEnd < 0) return result;
        String arr = json.substring(arrStart + 1, arrEnd);
        for (String part : arr.split(",")) {
            String s = part.trim();
            if (s.startsWith("\"") && s.endsWith("\""))
                result.add(jsonUnescape(s.substring(1, s.length() - 1)));
        }
        return result;
    }

    private static List<String> parseObjectArray(String json, String key) {
        List<String> result = new ArrayList<>();
        int start = json.indexOf("\"" + key + "\"");
        if (start < 0) return result;
        int arrStart = json.indexOf("[", start);
        if (arrStart < 0) return result;
        int depth = 0, objStart = -1;
        for (int i = arrStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if (depth == 0) objStart = i; depth++; }
            else if (c == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) { result.add(json.substring(objStart, i + 1)); objStart = -1; }
            } else if (c == ']' && depth == 0) break;
        }
        return result;
    }

    private static String parseStr(String block, String key) {
        String search = "\"" + key + "\"";
        int idx = block.indexOf(search);
        if (idx < 0) return "";
        int colon = block.indexOf(":", idx + search.length());
        if (colon < 0) return "";
        int q1 = block.indexOf("\"", colon + 1);
        if (q1 < 0) return "";
        int q2 = q1 + 1;
        while (q2 < block.length()) {
            if (block.charAt(q2) == '"' && block.charAt(q2 - 1) != '\\') break;
            q2++;
        }
        return jsonUnescape(block.substring(q1 + 1, q2));
    }

    private static int parseInt(String block, String key) {
        String search = "\"" + key + "\"";
        int idx = block.indexOf(search);
        if (idx < 0) return 0;
        int colon = block.indexOf(":", idx + search.length());
        if (colon < 0) return 0;
        StringBuilder sb = new StringBuilder();
        for (int i = colon + 1; i < block.length(); i++) {
            char c = block.charAt(i);
            if (Character.isDigit(c) || c == '-') sb.append(c);
            else if (sb.length() > 0) break;
        }
        try { return Integer.parseInt(sb.toString().trim()); } catch (Exception e) { return 0; }
    }

    private static boolean parseBool(String block, String key) {
        String search = "\"" + key + "\"";
        int idx = block.indexOf(search);
        if (idx < 0) return false;
        int colon = block.indexOf(":", idx + search.length());
        if (colon < 0) return false;
        return block.substring(colon + 1).trim().startsWith("true");
    }

    private static String jStr(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\","\\\\").replace("\"","\\\"")
                       .replace("\n","\\n").replace("\r","\\r") + "\"";
    }

    private static String jsonUnescape(String s) {
        return s.replace("\\\"","\"").replace("\\n","\n")
                .replace("\\r","\r").replace("\\\\","\\");
    }

    public static String getDataFilePath() { return getDataFile().getAbsolutePath(); }
}
