package com.example.store1.api;

import com.example.store1.Models.Product;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    @GET("/api/products")
    Call<List<Product>> getAllProducts();

    @GET("/api/products/{id}")
    Call<Product> getProductById(@Path("id") String id);
}
