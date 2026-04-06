package com.inventaire;

public class Product {

    public static final String STATUS_ACTIVE       = "Actif";
    public static final String STATUS_INACTIVE     = "Inactif";
    public static final String STATUS_DISCONTINUED = "Discontinue";

    private int    id;
    private String reference       = "";
    private String name            = "";
    private String description     = "";
    private String category        = "";
    private int    quantity        = 0;
    private int    minQuantity     = 5;
    private String storageLocation = "";   // ancien : price
    private String status          = STATUS_ACTIVE;

    public Product() {}

    public Product(int id, String reference, String name, String category,
                   int quantity, int minQuantity, String storageLocation, String status) {
        this.id              = id;
        this.reference       = reference;
        this.name            = name;
        this.category        = category;
        this.quantity        = quantity;
        this.minQuantity     = minQuantity;
        this.storageLocation = storageLocation;
        this.status          = status;
    }

    // ── Getters ─────────────────────────────────────────────────────────────
    public int    getId()              { return id; }
    public String getReference()       { return reference; }
    public String getName()            { return name; }
    public String getDescription()     { return description; }
    public String getCategory()        { return category; }
    public int    getQuantity()        { return quantity; }
    public int    getMinQuantity()     { return minQuantity; }
    public String getStorageLocation() { return storageLocation; }
    public String getStatus()          { return status; }

    // ── Setters ─────────────────────────────────────────────────────────────
    public void setId(int id)                         { this.id = id; }
    public void setReference(String reference)        { this.reference = reference; }
    public void setName(String name)                  { this.name = name; }
    public void setDescription(String desc)           { this.description = desc; }
    public void setCategory(String category)          { this.category = category; }
    public void setQuantity(int quantity)             { this.quantity = quantity; }
    public void setMinQuantity(int minQty)            { this.minQuantity = minQty; }
    public void setStorageLocation(String location)   { this.storageLocation = location; }
    public void setStatus(String status)              { this.status = status; }

    @Override
    public String toString() { return name + " [" + reference + "]"; }
}
