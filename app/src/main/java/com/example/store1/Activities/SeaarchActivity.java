package com.example.store1.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.store1.Adapters.ProductAdapter;
import com.example.store1.Models.Product;
import com.example.store1.R;
import com.example.store1.api.ApiClient;
import com.example.store1.api.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeaarchActivity extends AppCompatActivity {

    private RecyclerView rvSearchResults;
    private EditText searchEditText;
    private ProductAdapter adapter;
    private ArrayList<Product> productList = new ArrayList<>();
    private ArrayList<Product> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seaarch);

        rvSearchResults = findViewById(R.id.rvSearchResults);
        searchEditText = findViewById(R.id.searchEditText);
        ImageView back = findViewById(R.id.back2);

        back.setOnClickListener(v -> onBackPressed());

        adapter = new ProductAdapter(this, filteredList);
        rvSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        rvSearchResults.setAdapter(adapter);

        loadAllProducts();

        // 🔍 Add live search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAllProducts() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Product>> call = apiService.getAllProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call,
                                   @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    filteredList.clear();
                    filteredList.addAll(productList);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SeaarchActivity.this, "Failed to fetch products",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                Toast.makeText(SeaarchActivity.this, "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        for (Product product : productList) {
            if (product.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
