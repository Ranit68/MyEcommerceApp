package com.example.store1.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private String id, title, category, imageUrl, description;
    private double price;
    private int stock;
    private double rating;
    private int quantity = 1;

    public Product() {}

    public Product(String id, String title, String category, String imageUrl, String description, double price, int stock, double rating) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.imageUrl = imageUrl;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.rating = rating;
    }

    protected Product(Parcel in) {
        id = in.readString();
        title = in.readString();
        category = in.readString();
        imageUrl = in.readString();
        description = in.readString();
        price = in.readDouble();
        stock = in.readInt();
        rating = in.readDouble();
        quantity = in.readInt();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) { return new Product(in); }
        @Override
        public Product[] newArray(int size) { return new Product[size]; }
    };

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public double getRating() { return rating; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(category);
        parcel.writeString(imageUrl);
        parcel.writeString(description);
        parcel.writeDouble(price);
        parcel.writeInt(stock);
        parcel.writeDouble(rating);
        parcel.writeInt(quantity);
    }
}
