package com.example.store1.Models;

public class OrderItem {
    private String id, title, description, imageUrl;
    private double price;

    public OrderItem() {} // required for Firebase

    public OrderItem(String id, String title, String description, double price, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
}
