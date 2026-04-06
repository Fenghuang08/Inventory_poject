package com.inventaire;

import java.time.LocalDateTime;

public class Movement {

    public enum Type {
        IN("Entrée"),
        OUT("Sortie");

        private final String label;
        Type(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private int           id;
    private int           productId;
    private String        productName      = "";
    private String        productReference = "";
    private Type          type             = Type.IN;
    private int           quantity         = 0;
    private LocalDateTime dateTime         = LocalDateTime.now();
    private String        username         = "";
    private String        note             = "";

    public Movement() {}

    // ── Getters ─────────────────────────────────────────────────────────────
    public int           getId()                 { return id; }
    public int           getProductId()          { return productId; }
    public String        getProductName()        { return productName; }
    public String        getProductReference()   { return productReference; }
    public Type          getType()               { return type; }
    public int           getQuantity()           { return quantity; }
    public LocalDateTime getDateTime()           { return dateTime; }
    public String        getUsername()           { return username; }
    public String        getNote()               { return note; }

    // ── Setters ─────────────────────────────────────────────────────────────
    public void setId(int id)                           { this.id = id; }
    public void setProductId(int productId)             { this.productId = productId; }
    public void setProductName(String productName)      { this.productName = productName; }
    public void setProductReference(String ref)         { this.productReference = ref; }
    public void setType(Type type)                      { this.type = type; }
    public void setQuantity(int quantity)               { this.quantity = quantity; }
    public void setDateTime(LocalDateTime dateTime)     { this.dateTime = dateTime; }
    public void setUsername(String username)            { this.username = username; }
    public void setNote(String note)                    { this.note = note; }
}
